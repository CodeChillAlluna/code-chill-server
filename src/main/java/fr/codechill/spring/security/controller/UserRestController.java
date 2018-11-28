package fr.codechill.spring.security.controller;

import fr.codechill.spring.security.JwtTokenUtil;
import fr.codechill.spring.security.JwtUser;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"${app.clienturl}"})
public class UserRestController {

  private final Log logger = LogFactory.getLog(this.getClass());

  @Value("${jwt.header}")
  private String tokenHeader;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Autowired private UserDetailsService userDetailsService;

  @RequestMapping(value = "user", method = RequestMethod.GET)
  public ResponseEntity<?> getAuthenticatedUser(HttpServletRequest request) {
    String token = request.getHeader(tokenHeader).substring(7);
    String username = jwtTokenUtil.getUsernameFromToken(token);
    try {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      JwtUser user = (JwtUser) userDetails;
      return ResponseEntity.ok(user);
    } catch (UsernameNotFoundException e) {
      logger.error(e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("{\"message\": \"" + e.getMessage() + "\"}");
    }
  }
}
