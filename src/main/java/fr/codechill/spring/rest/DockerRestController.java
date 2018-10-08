package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.mail.iap.Response;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.UserRepository;
import springfox.documentation.spring.web.json.Json;
import fr.codechill.spring.model.Docker;

@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerRestController {
    private final Log logger =  LogFactory.getLog(this.getClass());
    private final String BASE_URL = "http://localhost:2375";
    private final UserRepository urepo;

    @Autowired
    public DockerRestController(UserRepository urepo) {
        this.urepo = urepo;
    }

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

    @GetMapping(value="/dockers/user/{id}", produces ="application/json")
    public ResponseEntity<?> getDockerInfo(@PathVariable("id") Long id) {
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();   
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ObjectNode data = mapper.createObjectNode();
        logger.info("ID récupéré : " + id);
        User user = this.urepo.findOne(id);
        Docker dummyDock = new Docker("test");
        logger.info("ID de dummy dock : " + dummyDock.getId());
        user.addDocker(dummyDock);
        logger.info("Prenom de l'utilisateur traité  : " + user.getFirstname() +" nom de famille " + user.getLastname() );
        logger.info("taille de la liste des dockers récupérés :  " + user.getDockers().size());
        for (Docker dock : user.getDockers()) {
             String dockerStatsUrl = BASE_URL + "/containers/" + "1bf63cd0d020" + "/stats?stream=False";
             ResponseEntity <?> resp = restTemplate.exchange(dockerStatsUrl, HttpMethod.GET, entity, String.class);
            logger.info("CONTENU BODY RESPONSE ENTITY : " + resp.getBody().toString());
        }
        return ResponseEntity.ok().headers(headers).body(data);
    }

}