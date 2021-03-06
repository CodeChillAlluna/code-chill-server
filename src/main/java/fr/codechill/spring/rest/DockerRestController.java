package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.exception.BadRequestException;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.Image;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.DockerRepository;
import fr.codechill.spring.repository.DockerShareRepository;
import fr.codechill.spring.repository.ImageRepository;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.security.JwtTokenUtil;
import fr.codechill.spring.utils.docker.DockerActions;
import fr.codechill.spring.utils.docker.DockerStats;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class DockerRestController {
  private final UserRepository urepo;
  private final DockerRepository drepo;
  private final ImageRepository irepo;
  private final DockerShareRepository dsrepo;
  private static final Logger logger = Logger.getLogger(DockerController.class);

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired private DockerController dcontroller;

  @Autowired
  public DockerRestController(
      UserRepository urepo,
      DockerRepository drepo,
      ImageRepository irepo,
      DockerShareRepository dsrepo) {
    this.urepo = urepo;
    this.drepo = drepo;
    this.irepo = irepo;
    this.dsrepo = dsrepo;
  }

  private void checkUserOwnContainer(User user, Docker docker) throws BadRequestException {
    String message;
    if (docker == null) {
      message = "There's no docker with this id!";
      logger.error(message);
      throw new BadRequestException(message);
    } else if (!user.getDockers().contains(docker)) {
      message = String.format("You don't own the docker with id %s!", docker.getContainerId());
      logger.error(String.format("You don't own the docker with id %s!", docker.getContainerId()));
      throw new BadRequestException(
          String.format("You don't own the docker with id %s!", docker.getContainerId()));
    }
  }

  private ResponseEntity<?> dockerAction(String userToken, Long dockerId, DockerActions action)
      throws Exception {
    Docker docker = drepo.findOne(dockerId);
    String username = jwtTokenUtil.getUsernameFromToken(userToken.substring(7));
    User user = this.urepo.findByUsername(username);
    this.checkUserOwnContainer(user, docker);
    if (action.equals(DockerActions.STATS))
      return dcontroller.getDockerStats(docker.getContainerId());
    ResponseEntity<?> res;
    if (action.equals(DockerActions.INSPECT))
      res = dcontroller.dockerAction(docker.getContainerId(), action.toString(), HttpMethod.GET);
    else
      res = dcontroller.dockerAction(docker.getContainerId(), action.toString(), HttpMethod.POST);
    return res;
  }

  @PostMapping(value = "/containers/{id}/start", produces = "application/json")
  public ResponseEntity<?> startDocker(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    return this.dockerAction(token, id, DockerActions.START);
  }

  @PostMapping(value = "/containers/{id}/stop", produces = "application/json")
  public ResponseEntity<?> stopDocker(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    return this.dockerAction(token, id, DockerActions.STOP);
  }

  @PostMapping(value = "/containers/{id}/pause", produces = "application/json")
  public ResponseEntity<?> pauseDocker(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    return this.dockerAction(token, id, DockerActions.PAUSE);
  }

  @PostMapping(value = "/containers/{id}/resume", produces = "application/json")
  public ResponseEntity<?> resumeDocker(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    return this.dockerAction(token, id, DockerActions.RESUME);
  }

  @PostMapping(value = "/containers/{id}/restart", produces = "application/json")
  public ResponseEntity<?> restartDocker(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    return this.dockerAction(token, id, DockerActions.RESTART);
  }

  @DeleteMapping(value = "/containers/{id}", produces = "application/json")
  public ResponseEntity<?> deleteDocker(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    Docker docker = drepo.findOne(id);
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    if (!user.getDockers().contains(docker)) {
      ObjectMapper mapper = new ObjectMapper();
      HttpHeaders headers = new HttpHeaders();
      ObjectNode body = mapper.createObjectNode();
      body.put("message", "The docker with id " + id + " doesn't exist or you don't own it!");
      return ResponseEntity.badRequest().headers(headers).body(body);
    }
    ResponseEntity<?> res = dcontroller.deleteDocker(docker.getContainerId());
    if (res.getStatusCode().is2xxSuccessful()) {
      this.dsrepo.deleteByUserId(docker.getId());
      user.deleteDocker(docker);
      this.urepo.save(user);
    }
    return res;
  }

  @PostMapping(value = "/containers/create", produces = "application/json")
  public ResponseEntity<?> createDocker(
      @RequestHeader(value = "Authorization") String token,
      @RequestBody CreateDockerRequest createDockerRequest) {
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    Image image = this.irepo.findOne(createDockerRequest.getImageId());
    if (image == null) {
      body.put("message", "This image doesn't exists.");
      return ResponseEntity.badRequest().headers(headers).body(body);
    }
    Docker docker = dcontroller.createDocker(createDockerRequest.getName(), image);
    if (docker == null) {
      body.put("message", "Something went wrong while creating a container");
      return ResponseEntity.badRequest().headers(headers).body(body);
    }
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    this.drepo.save(docker);
    user.addDocker(docker);
    this.urepo.save(user);
    return ResponseEntity.ok().headers(headers).body(docker);
  }

  @GetMapping(value = "/containers/{id}/stats", produces = "application/json")
  public ResponseEntity<?> getDockerStats(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    ResponseEntity<?> resp = this.dockerAction(token, id, DockerActions.STATS);
    ResponseEntity<?> respInspect = this.dockerAction(token, id, DockerActions.INSPECT);
    HttpHeaders headers = new HttpHeaders();
    if (resp.getStatusCodeValue() > 299) return resp;
    DockerStats dockerStats = new DockerStats();
    dockerStats = dcontroller.parseDockerStatsResponse(dockerStats, resp);
    dockerStats = dcontroller.parseDockerInspectResponse(dockerStats, respInspect);
    Docker docker = drepo.findOne(id);
    if (docker != null && !docker.getName().equals(dockerStats.getName())) {
      docker.setName(dockerStats.getName());
      this.drepo.save(docker);
    }
    return ResponseEntity.ok().headers(headers).body(dockerStats);
  }

  @PostMapping(value = "/containers/{id}/rename/{name}", produces = "application/json")
  public ResponseEntity<?> renameDocker(
      @RequestHeader(value = "Authorization") String token,
      @PathVariable("id") Long id,
      @PathVariable("name") String name)
      throws Exception {
    Docker docker = drepo.findOne(id);
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    this.checkUserOwnContainer(user, docker);
    ResponseEntity<?> resp = this.dcontroller.renameDocker(docker.getContainerId(), name);
    if (resp.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
      logger.info("Successfully renamed your docker with name " + name);
      docker.setName(name);
      this.drepo.save(docker);
    }
    return resp;
  }

  @GetMapping(value = "/containers/{id}/export")
  public ResponseEntity<StreamingResponseBody> exportContainer(
      @RequestHeader(value = "Authorization") String token, @PathVariable("id") Long id)
      throws Exception {
    Docker docker = drepo.findOne(id);
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    this.checkUserOwnContainer(user, docker);
    return dcontroller.exportContainer(docker.getContainerId(), docker.getName());
  }

  @GetMapping(value = "/images/**/get")
  public ResponseEntity<StreamingResponseBody> exportContainer(
      @RequestHeader(value = "Authorization") String token, HttpServletRequest request)
      throws Exception {
    String requestURL = request.getRequestURL().toString();
    String name = requestURL.split("/images/")[1].split("/get")[0];
    return dcontroller.exportImage(name);
  }

  @GetMapping(value = "/containers/{id}/archive/**")
  public ResponseEntity<StreamingResponseBody> exportFile(
      @RequestHeader(value = "Authorization") String token,
      @PathVariable("id") Long id,
      HttpServletRequest request)
      throws Exception {
    String requestURL = request.getRequestURL().toString();
    String filePath = requestURL.split("/archive/")[1];
    Docker docker = drepo.findOne(id);
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    this.checkUserOwnContainer(user, docker);
    return dcontroller.exportFile(filePath, docker.getContainerId());
  }

  @PostMapping(value = "/containers/{id}/commit")
  public ResponseEntity<?> commitChange(
      @RequestHeader(value = "Authorization") String token,
      @PathVariable("id") Long id,
      @RequestBody CommitImageRequest commitImageRequest)
      throws Exception {
    Docker docker = drepo.findOne(id);
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    this.checkUserOwnContainer(user, docker);
    return dcontroller.sendCommit(docker, commitImageRequest, user);
  }
}
