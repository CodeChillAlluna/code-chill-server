package fr.codechill.spring.controller;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;

import fr.codechill.spring.model.User;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.repository.DockerRepository;

@Component
public class DockerController {

    private final DockerRepository drepo;

    @Value("${app.dockerurl}")
    private String BASE_URL;

    private static final Logger logger = Logger.getLogger(DockerController.class);

    public DockerController(DockerRepository drepo) {
        this.drepo = drepo;
    }

    public Docker createDocker() {
        String dockerCreatetUrl = BASE_URL + "/containers/create";
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("Image", "codechill/ubuntu-base-user");
        body.put("Hostname", "chill");
        body.put("tty", true);
        body.put("OpenStdin", true);
        body.put("AttachStdin", true);
        body.put("StdinOnce", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(body.toString(), headers);

        ResponseEntity<String> res = restTemplate.exchange(dockerCreatetUrl, HttpMethod.POST, entity, String.class);
        Docker docker;
        try {
            JsonNode id = mapper.readValue(res.getBody(), JsonNode.class);
            docker = new Docker(id.get("Id").textValue());
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
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        restTemplate.exchange(dockerStoptUrl, HttpMethod.POST, entity, String.class);
        ObjectNode data = mapper.createObjectNode();
        logger.info("" + action + "ing docker with the ID : " + id);
        data.put("data", "Docker " + action + "ed");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    public String dockerCreation() {
        String dockerCreateUrl = BASE_URL +"/containers/create";
        logger.info("URL USED : " + dockerCreateUrl);
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode data = mapper.createObjectNode();
        data.put("Image", "codechill/ubuntu-base-user");
        data.put("Hostname", "chill");
        data.put("tty", true);
        data.put("OpenStdin", true);
        data.put("AttachStdin", true);
        data.put("StdinOnce", true);
        
        HttpEntity<String> entity = new HttpEntity<String>(data.toString(), headers);
        ResponseEntity <?> resp = restTemplate.exchange(dockerCreateUrl, HttpMethod.POST, entity, String.class);

        logger.info("Creation du docker");
        String dockerId = "-1";
        try {
            JsonNode response = mapper.readTree(resp.getBody().toString());
            dockerId = response.get("Id").asText();
            logger.info("Id : " + dockerId);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return dockerId;
    }

}