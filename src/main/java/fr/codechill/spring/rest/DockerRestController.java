package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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



@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerRestController {
    private final UserRepository urepo;
    private final DockerRepository drepo;
    private final Log logger =  LogFactory.getLog(this.getClass());
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public DockerRestController(UserRepository urepo, DockerRepository drepo) {
        this.urepo = urepo;
        this.drepo = drepo;
    }

    @Value("${app.dockerurl}")
    private String BASE_URL;

    @Autowired
    private DockerController dcontroller;

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
        if (docker.equals(null)) {
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