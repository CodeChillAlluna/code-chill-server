package fr.codechill.spring.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.Image;
import fr.codechill.spring.repository.DockerRepository;
import fr.codechill.spring.repository.ImageRepository;
import fr.codechill.spring.rest.CommitImageRequest;
import fr.codechill.spring.utils.docker.DockerStats;
import fr.codechill.spring.utils.rest.CustomRestTemplate;
import fr.codechill.spring.utils.rest.HttpClientHelper;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class DockerController {

  private final DockerRepository drepo;

  private final ImageRepository irepo;

  @Autowired private CustomRestTemplate customRestTemplate;

  @Value("${app.dockerurl}")
  private String BASE_URL;

  @Value("${app.minPort}")
  private int minPort;

  @Value("${app.maxPort}")
  private int maxPort;

  private HttpClientHelper httpClient;

  private static final Logger logger = Logger.getLogger(DockerController.class);

  public DockerController(DockerRepository drepo, ImageRepository irepo) {
    this.drepo = drepo;
    this.irepo = irepo;
    this.httpClient = new HttpClientHelper();
  }

  public Docker createDocker(String name, Image image) {
    String dockerCreatetUrl = BASE_URL + "/containers/create?name=" + name;
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    body.put("Image", image.getName() + ":" + image.getVersion());
    body.put("Hostname", "chill");
    body.put("tty", true);
    body.put("OpenStdin", true);
    body.put("AttachStdin", true);
    body.put("StdinOnce", true);
    ObjectNode exposedPorts = mapper.createObjectNode();
    ObjectNode portsOptions = mapper.createObjectNode();
    exposedPorts.set("3000/tcp", portsOptions);
    body.set("ExposedPorts", exposedPorts);

    int port = SocketUtils.findAvailableTcpPort(minPort, maxPort);
    ObjectNode hostPort = mapper.createObjectNode();
    ObjectNode dockerPorts = mapper.createObjectNode();
    ObjectNode portBindings = mapper.createObjectNode();
    ArrayNode ports = mapper.createArrayNode();
    hostPort.put("HostPort", String.valueOf(port));
    ports.add(hostPort);
    dockerPorts.set("3000/tcp", ports);
    portBindings.set("PortBindings", dockerPorts);
    body.set("HostConfig", portBindings);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(body.toString(), headers);
    logger.info("body content : " + body.toString());
    ResponseEntity<String> res =
        this.customRestTemplate.exchange(dockerCreatetUrl, HttpMethod.POST, entity, String.class);
    Docker docker;
    try {
      JsonNode id = mapper.readValue(res.getBody(), JsonNode.class);
      logger.info("id content : " + id.toString());
      System.out.println("BEFORE IMAGE");
      System.out.println(image);
      System.out.println("AFTER IMAGE");
      System.out.println(image.getId());
      System.out.println(id);
      System.out.println(id.get("Id").asText());
      docker = new Docker(name, id.get("Id").asText(), port, image);
      System.out.println("BEFORE SAVE");
      System.out.println(docker);
      this.drepo.save(docker);
      System.out.println("AFTER SAVE");
      logger.info("name of the saved docker : " + docker.getName());
    } catch (Exception e) {
      e.printStackTrace();
      docker = null;
    }
    return docker;
  }

  public ResponseEntity<?> deleteDocker(String id) {
    String dockerDeleteUrl = BASE_URL + "/containers/" + id;
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> entity = new HttpEntity<Object>(headers);
    ResponseEntity<String> res =
        this.customRestTemplate.exchange(dockerDeleteUrl, HttpMethod.DELETE, entity, String.class);
    logger.info("Deleting docker " + id + " : " + res.getBody());
    return res;
  }

  public ResponseEntity<?> dockerAction(String id, String action, HttpMethod method) {
    String dockerActionUrl = BASE_URL + "/containers/" + id + "/" + action;
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> entity = new HttpEntity<Object>(headers);
    ResponseEntity<String> res =
        this.customRestTemplate.exchange(dockerActionUrl, method, entity, String.class);
    logger.info(
        "" + action + "ing docker " + id + " with status code : " + res.getStatusCodeValue());
    return res;
  }

  public ResponseEntity<?> getDockerStats(String id) {
    String dockerActionUrl = BASE_URL + "/containers/" + id + "/" + "/stats?stream=False";
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> entity = new HttpEntity<Object>(headers);
    ResponseEntity<String> res =
        this.customRestTemplate.exchange(dockerActionUrl, HttpMethod.GET, entity, String.class);
    logger.info("Get stats for docker " + id + " with status code : " + res.getStatusCodeValue());
    return res;
  }

  public DockerStats parseDockerStatsResponse(DockerStats dockerStats, ResponseEntity<?> resp) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      JsonNode jsonDocker = mapper.readTree(resp.getBody().toString());
      dockerStats.setDockerId(jsonDocker.get("id").asText());
      dockerStats.setName(jsonDocker.get("name").asText());
      dockerStats.setMemoryLimit(jsonDocker.get("memory_stats").get("limit").asDouble() / 1000000);
      dockerStats.setMemoryUsage(jsonDocker.get("memory_stats").get("usage").asDouble() / 1000000);
      double total_usage =
          jsonDocker.get("cpu_stats").get("cpu_usage").get("total_usage").asDouble();
      double system_cpu_usage = jsonDocker.get("cpu_stats").get("system_cpu_usage").asDouble();
      double pre_total_usage =
          jsonDocker.get("precpu_stats").get("cpu_usage").get("total_usage").asDouble();
      double pre_system_cpu_usage =
          jsonDocker.get("precpu_stats").get("system_cpu_usage").asDouble();
      double usage = total_usage - pre_total_usage;
      double system_usage = system_cpu_usage - pre_system_cpu_usage;
      dockerStats.setCpuPercent((usage / system_usage) * 100);
    } catch (Exception e) {
      logger.info("Cannot retrieve all stats, is the docker on ?");
    }
    return dockerStats;
  }

  public DockerStats parseDockerInspectResponse(DockerStats dockerStats, ResponseEntity<?> resp) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      JsonNode jsonDocker = mapper.readTree(resp.getBody().toString());
      dockerStats.setCreated(jsonDocker.get("Created").asText());
      dockerStats.setImage(jsonDocker.get("Config").get("Image").asText());
      dockerStats.setStatus(jsonDocker.get("State").get("Status").asText());
    } catch (Exception e) {
      logger.info("Cannot retrieve all infos, is the docker on ?");
    }
    return dockerStats;
  }

  public ResponseEntity<?> renameDocker(String containerId, String containerName) {
    String dockerRenameUrl =
        BASE_URL + "/containers/" + containerId + "/rename?name=" + containerName;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(headers);
    ResponseEntity<String> res =
        this.customRestTemplate.exchange(dockerRenameUrl, HttpMethod.POST, entity, String.class);
    return res;
  }

  public ResponseEntity<StreamingResponseBody> exportContainer(
      String containerId, String containerName) throws Exception {
    String exportContainerUrl = String.format("%s/containers/%s/export", BASE_URL, containerId);

    HttpResponse response = this.httpClient.get(exportContainerUrl, null);
    StreamingResponseBody streamingResponseBody =
        this.httpClient.contentToStreamingResponse(response.getEntity().getContent());

    int status = response.getStatusLine().getStatusCode();
    if (status != 200) {
      return new ResponseEntity<StreamingResponseBody>(
          streamingResponseBody, HttpStatus.valueOf(status));
    }
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/octet-stream");
    headers.add(
        "Content-Disposition",
        String.format("attachment; filename=\"%s.tar\"", containerName.replace("/", "")));
    headers.add("Access-Control-Expose-Headers", "Content-Disposition");
    return ResponseEntity.ok().headers(headers).body(streamingResponseBody);
  }

  public ResponseEntity<StreamingResponseBody> exportImage(String imageName) throws Exception {
    String exportImageUrl = String.format("%s/images/%s/get", BASE_URL, imageName);

    HttpResponse response = this.httpClient.get(exportImageUrl, null);
    StreamingResponseBody streamingResponseBody =
        this.httpClient.contentToStreamingResponse(response.getEntity().getContent());

    int status = response.getStatusLine().getStatusCode();
    if (status != 200) {
      return new ResponseEntity<StreamingResponseBody>(
          streamingResponseBody, HttpStatus.valueOf(status));
    }
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/octet-stream");
    headers.add("Content-Disposition", String.format("attachment; filename=\"%s.tar\"", imageName));
    headers.add("Access-Control-Expose-Headers", "Content-Disposition");
    return ResponseEntity.ok().headers(headers).body(streamingResponseBody);
  }

  public ResponseEntity<StreamingResponseBody> exportFile(String path, String containerId)
      throws Exception {
    String exportFileUrl =
        String.format("%s/containers/%s/archive?path=%s", BASE_URL, containerId, path);
    HttpResponse response = this.httpClient.get(exportFileUrl, null);
    StreamingResponseBody streamingResponseBody =
        this.httpClient.contentToStreamingResponse(response.getEntity().getContent());

    int status = response.getStatusLine().getStatusCode();
    if (status != 200) {
      return new ResponseEntity<StreamingResponseBody>(
          streamingResponseBody, HttpStatus.valueOf(status));
    }
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/octet-stream");
    headers.add("Content-Disposition", String.format("attachment; filename=\"%s.tar\"", path));
    headers.add("Access-Control-Expose-Headers", "Content-Disposition");
    return ResponseEntity.ok().headers(headers).body(streamingResponseBody);
  }

  public ResponseEntity<?> sendCommit(Docker docker, CommitImageRequest commitImageRequest)
      throws Exception {
    String commitChangeUrl =
        BASE_URL
            + "/commit?container="
            + docker.getContainerId()
            + "&repo="
            + commitImageRequest.getName()
            + "&tag="
            + commitImageRequest.getVersion();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(headers);
    ResponseEntity<String> res =
        this.customRestTemplate.exchange(commitChangeUrl, HttpMethod.POST, entity, String.class);
    System.out.println(res);
    if (res.getStatusCodeValue() == 201) {
      logger.info(
          "Commiting changes to the docker having for container ID : " + docker.getContainerId());
      Image image =
          new Image(
              commitImageRequest.getName(),
              commitImageRequest.getVersion(),
              commitImageRequest.getPrivacy());
      this.irepo.save(image);
    }
    return res;
  }
}
