package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.DockerRepository;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.security.JwtTokenUtil;
import fr.codechill.spring.utils.docker.DockerActions;
import fr.codechill.spring.utils.docker.DockerStats;



@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerRestController {
    private final UserRepository urepo;
    private final DockerRepository drepo;
    private static final Logger logger = Logger.getLogger(DockerController.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private DockerController dcontroller;

    @Autowired
    public DockerRestController(UserRepository urepo, DockerRepository drepo) {
        this.urepo = urepo;
        this.drepo = drepo;
    }

    private ResponseEntity<?> dockerAction(String userToken, Long dockerId, DockerActions action) {
        Docker docker = drepo.findOne(dockerId);
        String username = jwtTokenUtil.getUsernameFromToken(userToken.substring(7));
        User user = this.urepo.findByUsername(username);
        if (!user.getDockers().contains(docker)) {
            ObjectMapper mapper = new ObjectMapper();
            HttpHeaders headers = new HttpHeaders();
            ObjectNode body = mapper.createObjectNode();
            body.put("Message", "The docker with id " + dockerId + " doesn't exist or you don't own it!");
            return ResponseEntity.badRequest().headers(headers).body(body);
        }
        if (action.equals(DockerActions.STATS))
            return dcontroller.getDockerStats(docker.getName());
        return dcontroller.dockerAction(docker.getName(), action.toString());
    }

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

    @GetMapping(value="/containers/{id}/stats", produces = "application/json")
    public ResponseEntity<?> getDockerStats (@RequestHeader(value="Authorization") String token, @PathVariable("id") Long id) {
        ResponseEntity<?> resp = this.dockerAction(token, id, DockerActions.STATS);
        HttpHeaders headers = new HttpHeaders();
        ObjectMapper mapper = new ObjectMapper();
        DockerStats dockerStats = new DockerStats();
        if (resp.getStatusCodeValue() > 299)
            return resp;
        try {
            JsonNode jsonDocker = mapper.readTree(resp.getBody().toString());
            dockerStats.setDockerId(jsonDocker.get("id").asText());
            dockerStats.setName(jsonDocker.get("name").asText());
            dockerStats.setMemoryLimit(jsonDocker.get("memory_stats").get("limit").asDouble() / 1000000);
            dockerStats.setMemoryUsage(jsonDocker.get("memory_stats").get("usage").asDouble() / 1000000);
            Long total_usage = jsonDocker.get("cpu_stats").get("cpu_usage").get("total_usage").asLong();
            Long system_cpu_usage = jsonDocker.get("cpu_stats").get("system_cpu_usage").asLong();
            logger.info(total_usage);
            logger.info(system_cpu_usage);
            logger.info((double) (total_usage / system_cpu_usage) * 100);
            dockerStats.setCpuPercent((double) (total_usage / system_cpu_usage) * 100);
        }
        catch (Exception e) {
            logger.info("Cannot retrieve stats, is the docker on ?");
        }
        return ResponseEntity.ok().headers(headers).body(dockerStats);
    }
}