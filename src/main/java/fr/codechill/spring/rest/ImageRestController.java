package fr.codechill.spring.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.codechill.spring.model.Image;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.ImageRepository;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.security.JwtTokenUtil;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class ImageRestController {
  private final UserRepository urepo;
  private final ImageRepository irepo;
  private static final Logger logger = Logger.getLogger(ImageRestController.class);

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired
  public ImageRestController(UserRepository urepo, ImageRepository irepo) {
    this.urepo = urepo;
    this.irepo = irepo;
  }

  @GetMapping("/images")
  public ResponseEntity<?> getImages() {
    ObjectMapper mapper = new ObjectMapper();
    HttpHeaders headers = new HttpHeaders();
    ObjectNode body = mapper.createObjectNode();
    List<Image> images = this.irepo.findAll();
    body.putPOJO("images", images);
    body.put("message", "Successfully getting all images");
    return ResponseEntity.ok().headers(headers).body(body);
  }

  @GetMapping("/images/{id}")
  public ResponseEntity<?> getImage(@PathVariable("id") Long id) {
    ObjectMapper mapper = new ObjectMapper();
    HttpHeaders headers = new HttpHeaders();
    ObjectNode body = mapper.createObjectNode();
    Image image = this.irepo.findOne(id);
    if (image != null) {
      body.putPOJO("image", image);
      body.put("message", "Successfully geting user info");
      return ResponseEntity.ok().headers(headers).body(body);
    }
    body.put("message", "no image found");
    return ResponseEntity.badRequest().headers(headers).body(body);
  }

  @GetMapping("/user/images")
  public ResponseEntity<?> getOwnerImages(@RequestHeader(value = "Authorization") String token) {
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    List<Image> images = this.irepo.findByOwner(user);
    ObjectMapper mapper = new ObjectMapper();
    HttpHeaders headers = new HttpHeaders();
    ObjectNode body = mapper.createObjectNode();
    body.putPOJO("images", images);
    body.put("message", "Successfully getting all your images");
    return ResponseEntity.ok().headers(headers).body(body);
  }

  @PutMapping("/images/{id}/privacy/{isPrivate}")
  public ResponseEntity<?> changePrivacy(
      @RequestHeader(value = "Authorization") String token,
      @PathVariable("id") Long id,
      @PathVariable("isPrivate") Boolean privacy) {
    String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
    User user = this.urepo.findByUsername(username);
    Image image = this.irepo.findOne(id);
    HttpHeaders headers = new HttpHeaders();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode body = mapper.createObjectNode();
    if (image.getOwner() != null && image.getOwner().equals(user)) {
      image.setPrivacy(privacy);
      this.irepo.save(image);
      body.put("message", "Successfully updating your image privacy");
      return ResponseEntity.ok().headers(headers).body(body);
    }
    body.put("message", "Failed updating image privacy you're not the owner of this image");
    return ResponseEntity.badRequest().headers(headers).body(body);
  }
}
