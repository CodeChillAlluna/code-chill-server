package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import ch.qos.logback.core.joran.conditional.ElseAction;
import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.UserRepository;

@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerRestController {
    private final Log logger =  LogFactory.getLog(this.getClass());
    private final UserRepository urepo;

    @Value("${app.dockerurl}")
    private String BASE_URL;

    @Autowired
    private DockerController dcontroller;

    @Autowired
    public DockerRestController(UserRepository urepo) {
        this.urepo = urepo;
    }

    @PostMapping(value = "/containers/{id}/start", produces = "application/json")
    public ResponseEntity<?> startDocker(@PathVariable("id") String id) {
        return dcontroller.dockerAction(id, "start");
    }

    @PostMapping(value = "/containers/{id}/stop", produces = "application/json")
    public ResponseEntity<?> stopDocker(@PathVariable("id") String id) {
        return dcontroller.dockerAction(id, "stop");
    }

    @GetMapping(value="/dockers/user/{id}", produces ="application/json")
    public ResponseEntity<?> getDockersInfo(@PathVariable("id") Long id) {
        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        ObjectNode data = mapper.createObjectNode();
        ObjectNode dockersUser = mapper.createObjectNode();
        logger.info("ID récupéré : " + id);
        User user = this.urepo.findOne(id);
        logger.info("Prenom de l'utilisateur traité  : " + user.getFirstname() +" nom de famille : " + user.getLastname());
        logger.info("taille de la liste des dockers récupérés :  " + user.getDockers().size());
        for (Docker dock : user.getDockers()) {        
            dockersUser.set(dock.getId().toString(),this.getDockerInfo(dock));
            logger.info("dockers utilisateur récupérés : " + dockersUser);
        }
        return ResponseEntity.ok().headers(headers).body(data);
    }

    public JsonNode getDockerInfo (Docker docker) {
        JsonNode jsonDocker;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode statDocker = mapper.createObjectNode();
        RestTemplate restTemplate = new RestTemplate();
        String dockerStatUrl = BASE_URL + "/containers/" + docker.getName() + "/stats?stream=False";
        ResponseEntity <?> resp = restTemplate.exchange(dockerStatUrl, HttpMethod.GET, entity, String.class);
        try {
            jsonDocker = mapper.readTree(resp.getBody().toString());
            logger.info(" Docker content : " + jsonDocker.toString());

            ObjectNode precpu_stats = mapper.createObjectNode();
            Double preCpuTotalUsage = (double) jsonDocker.get("precpu_stats").get("cpu_usage").get("total_usage").asLong();
            precpu_stats.put("total_usage",preCpuTotalUsage);
            Double kernel = (double) jsonDocker.get("cpu_stats").get("cpu_usage").get("usage_in_kernelmode").asLong();
            precpu_stats.put("usage_in_kernelmode",kernel);
            Double usageInUsermode = (double) jsonDocker.get("cpu_stats").get("cpu_usage").get("usage_in_usermode").asLong();
            precpu_stats.put("usage_in_usermode",usageInUsermode);

            statDocker.set("precpu_stats",precpu_stats);      
            logger.info("stat docker  content : " + statDocker.toString());

            ObjectNode memory_stats = mapper.createObjectNode();

            try {
                Double max_usage = (double) jsonDocker.get("memory_stats").get("max_usage").asLong();
                memory_stats.put("max_usage",max_usage);
                Double usage = (double) jsonDocker.get("memory_stats").get("usage").asLong();
                memory_stats.put("usage",usage);
                Double limit = (double) jsonDocker.get("memory_stats").get("limit").asLong();
                memory_stats.put("limit",limit);
                statDocker.set("memory_stats",memory_stats);
                logger.info("stat docker content with memory : " + statDocker.toString());
            }
            catch (Exception e) {
                memory_stats.put("memory_stats","docker offline");
                statDocker.set("memory_usage",memory_stats);
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return statDocker;
    }
}