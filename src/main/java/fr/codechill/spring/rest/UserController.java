package fr.codechill.spring.rest;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.exception.BadRequestException;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.security.JwtTokenUtil;

@CrossOrigin(origins = {"${app.clienturl}"})
@RestController
public class UserController {
    private final UserRepository urepo;
    @Value("${spring.mail.username}")
    private String SENDFROM;

    @Value("${app.clienturl}")
    private String BASE_URL;

    @Autowired
    private DockerController dcontroller;
  
    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserController(UserRepository urepo) {
        this.urepo = urepo;
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {

        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        ObjectNode body = mapper.createObjectNode();
        if (this.urepo.findOne(id)!=null)
        {
            User user = this.urepo.findOne(id);
            body.set("user",this.constructJsonUser(user));
            return ResponseEntity.ok().headers(new HttpHeaders()).body(body);
        }
        body.put("message","no user found");
        return ResponseEntity.badRequest().headers(headers).body(body);
    }


    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(@RequestHeader(value="Authorization") String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        ObjectNode body = mapper.createObjectNode();
        if (this.urepo.findByUsername(username)!=null)
        { 
            User user = this.urepo.findByUsername(username);
            this.urepo.delete(user);
            body.put("message "," the user "+ user.getLastname()+" has been deleted");
            logger.info(body.toString());
            return ResponseEntity.ok().headers(new HttpHeaders()).body(body);
        }
        else
        {
            body.put("message","the user your trying to delete doesn't seems to exist");
            return ResponseEntity.badRequest().headers(headers).body(body);
        }
    }

    @PutMapping("/user")
    public ResponseEntity<?> editUser(@RequestHeader(value="Authorization") String token, @RequestBody User user) {
        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        ObjectNode body = mapper.createObjectNode();
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        User updatedUser = this.urepo.findByUsername(username);

        if (!updatedUser.getLastname().equals(user.getLastname())) {
            logger.info("updating user lastname with : "+updatedUser.getLastname());
            updatedUser.setLastname(user.getLastname());
            body.put("message","user's lastname updated");
            body.set("user",this.constructJsonUser(user));
            logger.info("body "+ body.toString());
            this.urepo.save(updatedUser);
            return ResponseEntity.ok().headers(headers).body(body);
        }

        if (!updatedUser.getFirstname().equals(user.getFirstname())) {
            logger.info("updating user lastname with : "+updatedUser.getFirstname());
            updatedUser.setFirstname(user.getFirstname());
            body.put("message","user's firstname updated");
            body.set("user",this.constructJsonUser(user));
            this.urepo.save(updatedUser);
            return ResponseEntity.ok().headers(headers).body(body);
            
        }

        if (!updatedUser.getEmail().equals(user.getEmail())) {
            updatedUser.setEmail(user.getEmail());
            logger.info("updating user email with : "+ updatedUser.getEmail());
            updateUserEmail(updatedUser.getEmail());
            body.put("message","user's email updated");
            body.set("user",this.constructJsonUser(user));
            this.urepo.save(updatedUser);
            return ResponseEntity.ok().headers(headers).body(body);         
        }
        body.put("message","the user your trying to edit doesn't seem to exist ");
        return ResponseEntity.badRequest().headers(headers).body(body);
    }

    @PostMapping("/user")
    public ResponseEntity<?> addUser(@RequestBody User user) throws BadRequestException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        HttpHeaders responseHeaders = new HttpHeaders();
        if (urepo.findByUsername(user.getUsername()) != null) {
            logger.info("An account with this username already exist ");
            body.put("message","An account with this username already exist,returning a bad request");
            return ResponseEntity.badRequest().headers(responseHeaders).body(body);
        }
        if (urepo.findByEmail(user.getEmail()) != null) {
            logger.info("An account with this email already exist,returning a bad request");
            body.put("message","An account with this email already exist!");
            return ResponseEntity.badRequest().headers(responseHeaders).body(body);
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Docker docker = this.dcontroller.createDocker();
        user.addDocker(docker);
        urepo.save(user);
        final URI location = ServletUriComponentsBuilder
            .fromCurrentServletMapping().path("/user/{id}").build()
            .expand(user.getId()).toUri();
        body.set("user",this.constructJsonUser(user));
        return ResponseEntity.created(location).headers(responseHeaders).body(body);
    }


    //method sending a mail to a new user email
    public boolean updateUserEmail(String email)
    {
        if(!"".equals(email))
        {
            SimpleMailMessage updateEmail = new SimpleMailMessage();
            logger.info("Updating the user email with : "+email);
            updateEmail.setFrom(SENDFROM);
            updateEmail.setTo(email);
            logger.info("email content : "+ updateEmail);
            updateEmail.setSubject("Email adresse changed");
            updateEmail.setText("Your new email adress has been saved by our services");
            mailSender.send(updateEmail);
            return true;
        }
        logger.info("user email update failed due to an empty value");
        return false;
    }
    //Method sending an error message for the former email adress
    public boolean updateEmailError(String email){
        User user = urepo.findByEmail(email);
        if (user!=null){
            SimpleMailMessage infoUpdateFail = new SimpleMailMessage();
            infoUpdateFail.setFrom(SENDFROM);
            infoUpdateFail.setTo(user.getEmail());
            infoUpdateFail.setSubject("Suspicious access to your account");
            infoUpdateFail.setText("We have registered a suspicious activity on your acccount");
            logger.info("email content : "+ infoUpdateFail);
            mailSender.send(infoUpdateFail);
            return true;
        }
        return false;
    }

    // Process form submission from forgotPassword page
	@PostMapping(value = "/user/forgottenpassword")
	public ResponseEntity<?> processForgotPasswordForm(@RequestBody String email) throws BadRequestException {
        User user = urepo.findByEmail(email);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        HttpHeaders responseHeaders = new HttpHeaders();
        if(user != null) {
            user.setLastPasswordResetDate(new Date());
            user.setTokenPassword(UUID.randomUUID().toString());
            user = urepo.save(user);

            SimpleMailMessage passwordResetEmail = new SimpleMailMessage();
            passwordResetEmail.setFrom(SENDFROM);
            passwordResetEmail.setTo(user.getEmail());
            passwordResetEmail.setSubject("Password Reset Request");
            passwordResetEmail.setText("Reset link:\n" + BASE_URL + "/reset/" + user.getTokenPassword());
            mailSender.send(passwordResetEmail);
            body.put("message","an email has been sent to reset the password");
            body.set("user",this.constructJsonUser(user));
            return ResponseEntity.ok().headers(responseHeaders).body(body);
        }
        logger.info("No user email was found with this email");
        body.put("message","No user email was found with this input");
        return ResponseEntity.badRequest().headers(responseHeaders).body(body);  
    }

    @GetMapping(value = "/reset/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable("token") String token) throws BadRequestException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        HttpHeaders responseHeaders = new HttpHeaders();
        User user = urepo.findByTokenPassword(token);
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(user.getLastPasswordResetDate());
        c.add(Calendar.DATE, 1);
        Date currentDatePlusOne = c.getTime();
        if(user != null && currentDate.after(user.getLastPasswordResetDate()) && currentDate.before(currentDatePlusOne)) {
            body.put("message","the password has been reset");
            body.set("user",this.constructJsonUser(user));
            return ResponseEntity.ok().headers(responseHeaders).body(body);
        }
        logger.info("Password reset failed due to an invalid token or an out dated one");
        body.put("message","Your token is invalid or hax expired");
        return ResponseEntity.badRequest().headers(responseHeaders).body(body);  
    }

    @PostMapping(value = "/reset")
    public ResponseEntity<?> setNewPassword(@RequestBody Map<String, String> requestParams) throws BadRequestException {
        User user = urepo.findByTokenPassword(requestParams.get("token"));
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        HttpHeaders responseHeaders = new HttpHeaders();

        if(user != null) {
            user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));
            user.setTokenPassword(null);
            urepo.save(user);
            body.put("message","a new password has been set");
            body.set("user",this.constructJsonUser(user));
            return ResponseEntity.ok().headers(responseHeaders).body(body);
        }
        logger.info("Setting of a new password due to an invalid token or an out dated one");
        body.put("message","Your token is invalid or hax expired");
        return ResponseEntity.badRequest().headers(responseHeaders).body(body);  
    }

    private ObjectNode constructJsonUser(User user) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonUser = mapper.createObjectNode();
        jsonUser.put("id",user.getId());
        jsonUser.put("username",user.getUsername());
        jsonUser.put("password",user.getPassword());
        jsonUser.put("firstname",user.getFirstname());
        jsonUser.put("lastname",user.getLastname());
        jsonUser.put("email",user.getEmail());
        logger.info("number of dockers for the current user : "+user.getDockers().size());
        jsonUser.set("dockers",this.constructJsonDockerUser(user));
        logger.info("constructed user : " + jsonUser.toString());
        return jsonUser;
    }

    private ObjectNode constructJsonDockerUser(User user) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonDocker = mapper.createObjectNode();
        for (Docker docker : user.getDockers()) {
            ObjectNode currentDocker = mapper.createObjectNode();
            currentDocker.put("id",docker.getId());
            currentDocker.put("name",docker.getName());
            currentDocker.put("containerId",docker.getContainerId());
            currentDocker.put("port",docker.getPort());
            jsonDocker.set("docker " + docker.getName(),currentDocker);
        }
        logger.info("constructed dockers json : " + jsonDocker.toString());
        return jsonDocker;

    }
}