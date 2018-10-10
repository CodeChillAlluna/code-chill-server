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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestHeader;
import fr.codechill.spring.security.JwtTokenUtil;
import springfox.documentation.spring.web.json.Json;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.model.User;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.repository.DockerRepository;



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

    @PostMapping(value = "/containers/{id}/start", produces = "application/json")
    public ResponseEntity<?> startDocker(@PathVariable("id") String id) {
        return dcontroller.dockerAction(id, "start");
    }

    @PostMapping(value = "/containers/{id}/stop", produces = "application/json")
    public ResponseEntity<?> stopDocker(@PathVariable("id") String id) {
        return dcontroller.dockerAction(id, "stop");
    }

    @PostMapping(value="/dockers/create", produces = "application/json")
    public ResponseEntity<?> createDocker (@RequestHeader(value="Authorization") String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        User user = this.urepo.findByUsername(username);
        String dockerId =  dcontroller.dockerCreation();
        Docker docker = new Docker (dockerId);
        this.drepo.save(docker);
        user.addDocker(docker);
        this.urepo.save(user);
        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        ObjectNode data = mapper.createObjectNode();
        data.put("Message", "Docker created");
        data.put("Id", dockerId);
        return ResponseEntity.ok().headers(headers).body(data);

    }

}