package fr.codechill.spring.controller;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;

import fr.codechill.spring.model.Docker;
import fr.codechill.spring.repository.DockerRepository;
import fr.codechill.spring.utils.docker.DockerStats;
import fr.codechill.spring.utils.rest.CustomRestTemplate;

@Component
public class DockerController {

    private final DockerRepository drepo;

    @Autowired
    private CustomRestTemplate customRestTemplate;

    @Value("${app.dockerurl}")
    private String BASE_URL;

    @Value("${app.minPort}")
    private int minPort;

    @Value("${app.maxPort}")
    private int maxPort;

    private static final Logger logger = Logger.getLogger(DockerController.class);

    public DockerController(DockerRepository drepo) {
        this.drepo = drepo;
    }

    public Docker createDocker() {
        String dockerCreatetUrl = BASE_URL + "/containers/create";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("Image", "codechillaluna/code-chill-ide");
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

        ResponseEntity<String> res = this.customRestTemplate.exchange(dockerCreatetUrl, HttpMethod.POST, entity, String.class);
        Docker docker;
        try {
            JsonNode id = mapper.readValue(res.getBody(), JsonNode.class);
            docker = new Docker(id.get("Id").textValue(), port);
            this.drepo.save(docker);
        } catch (IOException e) {
            docker = null;
        }
        return docker;
    }

    public ResponseEntity<?> deleteDocker(String id) {
        String dockerDeleteUrl = BASE_URL + "/containers/" + id;  
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ResponseEntity<String> res = this.customRestTemplate.exchange(dockerDeleteUrl, HttpMethod.DELETE, entity, String.class);
        logger.info("Deleting docker " + id + " : " + res.getBody());
        return res;
    }

    public ResponseEntity<?> dockerAction(String id, String action, HttpMethod method) {
        String dockerActionUrl = BASE_URL + "/containers/" + id + "/" + action;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ResponseEntity<String> res = this.customRestTemplate.exchange(dockerActionUrl, method, entity, String.class);
        logger.info("" + action + "ing docker " + id + " with status code : " + res.getStatusCodeValue());
        return res;
    }

    public ResponseEntity<?> getDockerStats(String id) {
        String dockerActionUrl = BASE_URL + "/containers/" + id + "/" + "/stats?stream=False"; 
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ResponseEntity<String> res = this.customRestTemplate.exchange(dockerActionUrl, HttpMethod.GET, entity, String.class);
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
            double total_usage = jsonDocker.get("cpu_stats").get("cpu_usage").get("total_usage").asDouble();
            double system_cpu_usage = jsonDocker.get("cpu_stats").get("system_cpu_usage").asDouble();
            double pre_total_usage = jsonDocker.get("precpu_stats").get("cpu_usage").get("total_usage").asDouble();
            double pre_system_cpu_usage = jsonDocker.get("precpu_stats").get("system_cpu_usage").asDouble();
            double usage = total_usage - pre_total_usage;
            double system_usage = system_cpu_usage - pre_system_cpu_usage;
            dockerStats.setCpuPercent((usage / system_usage) * 100);
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
            logger.info("Cannot retrieve all infos, is the docker on ?");
        }
        return dockerStats;
    }

    public ResponseEntity <?> renameDocker(String containerId, String containerName) {
        String dockerRenameUrl = BASE_URL + "/containers/"+containerId+"/rename";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("name", containerName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(body.toString(), headers);
        ResponseEntity<String> res = this.customRestTemplate.exchange(dockerRenameUrl, HttpMethod.POST, entity, String.class);
        return ResponseEntity.ok(body);
    }
}