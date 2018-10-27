package fr.codechill.spring.controller;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import fr.codechill.spring.model.Docker;
import fr.codechill.spring.repository.DockerRepository;

@Component
public class DockerController {

    private final DockerRepository drepo;

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
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("Image", "theiaide/theia-full:next");
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

        ResponseEntity<String> res = restTemplate.exchange(dockerCreatetUrl, HttpMethod.POST, entity, String.class);
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

    public ResponseEntity<?> dockerAction(String id, String action) {
        String dockerStoptUrl = BASE_URL + "/containers/" + id + "/" + action;
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();   
        HttpHeaders headers = new HttpHeaders();
        ObjectNode body = mapper.createObjectNode();
        ObjectNode data = mapper.createObjectNode();
        HttpEntity<String> entity = new HttpEntity<String>(body.toString(), headers);
        HttpEntity<String> test = restTemplate.exchange(dockerStoptUrl, HttpMethod.POST, entity, String.class);
        logger.info(test.toString());
        logger.info("" + action + "ing docker with the ID : " + id);
        data.put("data", "Docker " + action + "ed");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}