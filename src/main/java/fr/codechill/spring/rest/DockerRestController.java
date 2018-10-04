package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerRestController {
    private final Log logger =  LogFactory.getLog(this.getClass());
    private final String BASE_URL = "http://localhost:2375";

    @PostMapping(value = "/containers/{id}/start", produces = "application/json")
    public ResponseEntity<?> startDocker(@PathVariable("id") String id) {
        String dockerStartUrl = BASE_URL + "/containers/" + id + "/start";
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();   
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        restTemplate.exchange(dockerStartUrl, HttpMethod.POST, entity, String.class);
        ObjectNode data = mapper.createObjectNode();
        logger.info("docker with the ID : " + id + "starting");
        data.put("data", "Docker started");
        logger.info("docker with the ID : " + id + "started");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @PostMapping(value = "/containers/{id}/stop", produces = "application/json")
    public ResponseEntity<?> stopDocker(@PathVariable("id") String id) {
        String dockerStoptUrl = BASE_URL + "/containers/" + id + "/stop";
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();   
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        restTemplate.exchange(dockerStoptUrl, HttpMethod.POST, entity, String.class);
        ObjectNode data = mapper.createObjectNode();
        logger.info("Stopping docker with the ID : " + id);
        data.put("data", "Docker stoped");
        logger.info("docker with the ID : " + id + "stopped");
        return ResponseEntity.ok().headers(headers).body(data);
    }

}