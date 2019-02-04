package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.DockerShare;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.DockerRepository;
import fr.codechill.spring.repository.DockerShareRepository;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.security.JwtTokenUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerShareRestController {
  private final UserRepository urepo;
  private final DockerRepository drepo;
  private final DockerShareRepository dsrepo;
  private static final Logger logger = Logger.getLogger(DockerShareRestController.class);

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired
  public DockerShareRestController(
      UserRepository urepo, DockerRepository drepo, DockerShareRepository dsrepo) {
    this.urepo = urepo;
    this.drepo = drepo;
    this.dsrepo = dsrepo;
  }

  @GetMapping(value = "/user/env/shared", produces = "application/json")
  public ResponseEntity<?> getAllSharedDockerForUser(
      @RequestHeader(value = "Authorization") String token) {
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    List<DockerShare> dockersShare = this.dsrepo.findByUserId(user.getId());
    List<Long> dockerIds =
        dockersShare
            .stream()
            .map(dockerShare -> dockerShare.getDockerId())
            .collect(Collectors.toList());
    List<Docker> dockers = this.drepo.findByIdIn(dockerIds);
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    body.putPOJO("dockers", dockers);
    body.put("message", "Successfully getting all your dockers");
    return ResponseEntity.ok().headers(headers).body(body);
  }

  @GetMapping(value = "/user/env/{id}/shared", produces = "application/json")
  public ResponseEntity<?> getAllUserDockerShared(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id) {
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    List<DockerShare> dockersShare = this.dsrepo.findByDockerId(id);
    List<Long> userIds =
        dockersShare
            .stream()
            .map(dockerShare -> dockerShare.getUserId())
            .collect(Collectors.toList());
    List<User> users = this.urepo.findByIdIn(userIds);
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    body.putPOJO("users", users);
    body.put("message", "Successfully getting all users with access to your environment");
    return ResponseEntity.ok().headers(headers).body(body);
  }

  @GetMapping(value = "/user/env/{id}/check", produces = "application/json")
  public ResponseEntity<?> checkUserHaveAccess(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id) {
    HttpHeaders headers = new HttpHeaders();
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    DockerShare dockerShare = this.dsrepo.findByDockerIdAndUserId(id, user.getId());
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
    Date currentDate = c.getTime();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    body.put("message", "Successfully getting all your images");
    if (dockerShare == null) {
      body.put("message", "You don't have access to this environment");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).headers(headers).body(body);
    }
    if (dockerShare.getExpiration() != null && currentDate.after(dockerShare.getExpiration())) {
      body.put("message", "You don't have access to this environment anymore");
      this.dsrepo.delete(dockerShare);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).headers(headers).body(body);
    }
    body.put("message", "You have access to this environment");
    return ResponseEntity.ok().headers(headers).body(body);
  }

  @PostMapping(value = "/user/env/{id}/share")
  public ResponseEntity<?> shareEnv(
      @RequestHeader(value = "Authorization") String token,
      @PathVariable("id") Long id,
      @RequestBody ShareRequest shareRequest) {
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    User userToShare = this.urepo.findOne(shareRequest.getUserId());
    Docker docker = this.drepo.findOne(id);
    if (userToShare == null || docker == null) {
      body.put("message", "This user or environment doesn't exist");
      return ResponseEntity.badRequest().headers(headers).body(body);
    }
    DockerShare dockerShare = this.dsrepo.findByDockerIdAndUserId(id, userToShare.getId());
    if (dockerShare == null) {
      dockerShare = new DockerShare(userToShare.getId(), docker.getId());
    }
    dockerShare.setReadOnly(shareRequest.getReadOnly());
    dockerShare.setExpiration(shareRequest.getExpirationDate());
    this.dsrepo.save(dockerShare);
    body.put("message", "Successfully share your environment");
    return ResponseEntity.ok().headers(headers).body(body);
  }

  @DeleteMapping(value = "/user/env/{id}/share")
  public ResponseEntity<?> unshareEnv(
      @RequestHeader(value = "Authorization") String token,
      @PathVariable("id") Long id,
      @RequestBody UnshareRequest unshareRequest) {
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    DockerShare dockerShare = this.dsrepo.findByDockerIdAndUserId(id, unshareRequest.getUserId());
    if (dockerShare != null) {
      this.dsrepo.delete(dockerShare);
      body.put("message", "Successfully disable access for this user to your environment");
    } else {
      body.put("message", "This user doesn't have access to your env, no need to do anything");
    }
    return ResponseEntity.ok().headers(headers).body(body);
  }
}
