package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.core.joran.conditional.ElseAction;
import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.DockerRepository;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.security.JwtTokenUtil;
import fr.codechill.spring.utils.docker.DockerActions;



@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerRestController {
    private final UserRepository urepo;
    private final DockerRepository drepo;
    // private final Log logger =  LogFactory.getLog(this.getClass());
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private DockerController dcontroller;

    @PostMapping(value = "/containers/{id}/start", produces = "application/json")
    public ResponseEntity<?> startDocker(@RequestHeader(value="Authorization") String token, @PathVariable("id") Long id) {
        return this.dockerAction(token, id, DockerActions.START);
    }

    @PostMapping(value = "/containers/{id}/stop", produces = "application/json")
    public ResponseEntity<?> stopDocker(@RequestHeader(value="Authorization") String token, @PathVariable("id") Long id) {
        return this.dockerAction(token, id, DockerActions.STOP);
    }
    
    @PostMapping(value = "/containers/{id}/pause", produces = "application/json")
    public ResponseEntity<?> pauseDocker(@RequestHeader(value="Authorization") String token, @PathVariable("id") Long id) {
        return this.dockerAction(token, id, DockerActions.PAUSE);
    }
    
    @PostMapping(value = "/containers/{id}/resume", produces = "application/json")
    public ResponseEntity<?> resumeDocker(@RequestHeader(value="Authorization") String token, @PathVariable("id") Long id) {
        return this.dockerAction(token, id, DockerActions.RESUME);
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
    @DeleteMapping(value = "/containers/{id}", produces = "application/json")
    public ResponseEntity<?> deleteDocker(@RequestHeader(value="Authorization") String token, @PathVariable("id") Long id) {
        Docker docker = drepo.findOne(id);
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        User user = this.urepo.findByUsername(username);
        if (!user.getDockers().contains(docker)) {
            ObjectMapper mapper = new ObjectMapper();
            HttpHeaders headers = new HttpHeaders();
            ObjectNode body = mapper.createObjectNode();
            body.put("Message", "The docker with id " + id + " doesn't exist or you don't own it!");
            return ResponseEntity.badRequest().headers(headers).body(body);
        }
        ResponseEntity<?> res = dcontroller.deleteDocker(docker.getName());
        if (res.getStatusCode().is2xxSuccessful()) {
            user.deleteDocker(docker);
        }
        return res;
    }

    @PostMapping(value="/containers/create", produces = "application/json")
    public ResponseEntity<?> createDocker (@RequestHeader(value="Authorization") String token) {
        Docker docker =  dcontroller.createDocker();
        HttpHeaders headers = new HttpHeaders();
        if (docker == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            body.put("Message", "Something went wrong while creating a container");
            return ResponseEntity.badRequest().headers(headers).body(body);
        }
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        User user = this.urepo.findByUsername(username);
        this.drepo.save(docker);
        user.addDocker(docker);
        this.urepo.save(user);
        return ResponseEntity.ok().headers(headers).body(docker);
    }

}