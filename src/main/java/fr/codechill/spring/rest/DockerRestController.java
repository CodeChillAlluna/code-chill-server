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
    public ResponseEntity<?> getDockerInfo(@PathVariable("id") Long id) {
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();   
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        ObjectNode data = mapper.createObjectNode();
        logger.info("ID récupéré : " + id);
        User user = this.urepo.findOne(id);
        logger.info("Prenom de l'utilisateur traité  : " + user.getFirstname() +" nom de famille " + user.getLastname() );
        logger.info("taille de la liste des dockers récupérés :  " + user.getDockers().size());
        for (Docker dock : user.getDockers()) {
            String dockerStatsUrl = BASE_URL + "/containers/" + dock.getName() + "/stats?stream=False";
            ResponseEntity <?> resp = restTemplate.exchange(dockerStatsUrl, HttpMethod.GET, entity, String.class);
            try {
                JsonNode dockerJson = mapper.readTree(resp.getBody().toString());
                logger.info("CONTENU BODY RESPONSE ENTITY : " + dockerJson);
                String dName = dockerJson.get("name").asText();
                Long dId = dockerJson.get("id").asLong();
                Long dMemLimit = dockerJson.get("memory_stats").get("limit").asLong() / 1048576;
                Long dMemUsage = dockerJson.get("memory_stats").get("usage").asLong() / 1048576;
                // logger.info("" + dName + " - " + dId + " - " + dMemLimit + " - " + dMemUsage);

                Double cpuTotalUsage = (double) dockerJson.get("cpu_stats").get("cpu_usage").get("total_usage").asLong();
                Double preCpuTotalUsage = (double) dockerJson.get("precpu_stats").get("cpu_usage").get("total_usage").asLong();
                Double usageInUsermode = (double) dockerJson.get("cpu_stats").get("cpu_usage").get("usage_in_usermode").asLong();
                Double systemCpuUsage = (double) dockerJson.get("cpu_stats").get("system_cpu_usage").asLong();
                Double systemPreCpuUsage = (double) dockerJson.get("precpu_stats").get("system_cpu_usage").asLong();
                
                Double res = (cpuTotalUsage / systemCpuUsage);
                logger.info(res);
                //Double cpuPercent = (double) ((cpuTotalUsage - preCpuTotalUsage) / (systemCpuUsage- systemPreCpuUsage)) * 100;
                // logger.info("" + cpuTotalUsage + " - " + preCpuTotalUsage + " - " + systemCpuUsage + " - " + systemPreCpuUsage);
                //logger.info("" + cpuPercent);
            } catch(Exception e) {

            }
        }
        return ResponseEntity.ok().headers(headers).body(data);
    }

}