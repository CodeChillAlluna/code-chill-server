package fr.codechill.spring.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

import fr.codechill.spring.exception.BadRequestException;
import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.User;
import fr.codechill.spring.repository.DockerRepository;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.security.JwtTokenUtil;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"})
@RestController
public class UserController {
    private final UserRepository urepo;
    private final DockerController dcontroller;
    private final String SENDFROM = "codechill@hotmail.com";
    private final String BASE_URL = "http://localhost:3000";
    private final Log logger = LogFactory.getLog(this.getClass());;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserController(UserRepository urepo, DockerRepository drepo) {
        this.urepo = urepo;
        this.dcontroller = new DockerController(drepo);
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable("id") Long id) {
        User user = this.urepo.findOne(id);
        logger.info("getting user informations");
        return user;
    }


    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(@RequestHeader(value="Authorization") String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        User user = this.urepo.findByUsername(username);
        this.urepo.delete(user);
        logger.info("the user"+ user.getLastname()+"has been deleted");
        return ResponseEntity.ok().headers(new HttpHeaders()).body(null);
    }

    @PutMapping("/user")
    public User editUser(@RequestHeader(value="Authorization") String token, @RequestBody User user) {

        String username = jwtTokenUtil.getUsernameFromToken(token.substring(7));
        User updatedUser = this.urepo.findByUsername(username);
        if (!updatedUser.getLastname().equals(user.getLastname())) {
            logger.info("updating user lastname with : "+updatedUser.getLastname());
            updatedUser.setLastname(user.getLastname());
        }
        if (!updatedUser.getFirstname().equals(user.getFirstname())) {
            logger.info("updating user lastname with : "+updatedUser.getFirstname());
            updatedUser.setFirstname(user.getFirstname());
            
        }
        if (!updatedUser.getEmail().equals(user.getEmail())) {
            updatedUser.setEmail(user.getEmail());
            logger.info("updating user email with : "+ updatedUser.getEmail());
            updateUserEmail(updatedUser.getEmail());         
        }
        this.urepo.save(updatedUser);
        return user;
    }

    @PostMapping("/user")
    public ResponseEntity<?> addUser(@RequestBody User user) throws BadRequestException {
        HttpHeaders responseHeaders = new HttpHeaders();
        if (urepo.findByUsername(user.getUsername()) != null) {
            logger.info("An account with this username already exist ");
            throw new BadRequestException("An account with this username already exist!");
        }
        if (urepo.findByEmail(user.getEmail()) != null) {
            logger.info("An account with this email already exist!");
            throw new BadRequestException("An account with this email already exist!");
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Docker docker = this.dcontroller.createDocker();
        user.addDocker(docker);
        urepo.save(user);
        final URI location = ServletUriComponentsBuilder
            .fromCurrentServletMapping().path("/user/{id}").build()
            .expand(user.getId()).toUri();
        return ResponseEntity.created(location).headers(responseHeaders).body(urepo.findByUsername(user.getUsername()));
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
            return ResponseEntity.ok().headers(responseHeaders).body(user);
        }
        logger.info("No user email was found with this email");
        throw new BadRequestException("No user found with this email");  
    }

    @GetMapping(value = "/reset/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable("token") String token) throws BadRequestException {
        HttpHeaders responseHeaders = new HttpHeaders();
        User user = urepo.findByTokenPassword(token);
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(user.getLastPasswordResetDate());
        c.add(Calendar.DATE, 1);
        Date currentDatePlusOne = c.getTime();
        if(user != null && currentDate.after(user.getLastPasswordResetDate()) && currentDate.before(currentDatePlusOne)) {
            return ResponseEntity.ok().headers(responseHeaders).body(user);
        }
        logger.info("Password reset failed due to an invalid token or an out dated one");
        throw new BadRequestException("Your token is invalid or hax expired");
    }

    @PostMapping(value = "/reset")
    public ResponseEntity<?> setNewPassword(@RequestBody Map<String, String> requestParams) throws BadRequestException {
        User user = urepo.findByTokenPassword(requestParams.get("token"));
        HttpHeaders responseHeaders = new HttpHeaders();

        if(user != null) {
            user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));
            user.setTokenPassword(null);
            urepo.save(user);
            return ResponseEntity.ok().headers(responseHeaders).body(user);
        }
        logger.info("Setting of a new password due to an invalid token or an out dated one");
        throw new BadRequestException("Your token is invalid or hax expired");
    }
}