package fr.codechill.spring.rest;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fr.codechill.spring.CodeChillApplication;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import fr.codechill.spring.model.security.AuthorityName;
import fr.codechill.spring.repository.UserRepository;
import fr.codechill.spring.utils.rest.CustomRestTemplate;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=CodeChillApplication.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DockerRestControllerTest{

    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mock;
    private static String jwtToken;
    private static Long dockerId;
    private ObjectMapper mapper;
    private User testUser;
    private String username = "Nathou";
    private String password = "123456789";
    private String firstname = "Nathan";
    private String lastname = "Michanol";
    private String email = "nathou@bonjour.com";
    private Boolean enabled = true;
    private Date lastPasswordResetDate = new Date(1993, 12, 12);
    private final Log logger = LogFactory.getLog(this.getClass());

    @Before
    public void setUp() {
        this.mock = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        this.mapper = new ObjectMapper();
        this.setJwtToken("dummy","admin");
        this.testUser = setUpUser(username, password, firstname, lastname, email, enabled, lastPasswordResetDate);
    }

    public String setJwtToken(String username,String password) {
        ObjectNode body = this.mapper.createObjectNode();
        body.put("username", username);
        body.put("password", password);
        try {
            String res = this.mock.perform(post("/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(body)))
            .andReturn().getResponse().getContentAsString();
            JsonNode jsonres = mapper.readValue(res, JsonNode.class);
            jwtToken = jsonres.get("token").textValue();
            return jsonres.get("token").textValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    public User setUpUser(String username, String password, String firstname,
    String lastname, String email, Boolean enabled,
    Date lastPasswordResetDate) {
        User user = new User(lastname, firstname);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setLastPasswordResetDate(lastPasswordResetDate);
        user.setEnabled(enabled);
        return user;
    }
    @Test
    public void aCreateDockerTest() throws Exception {
        String res = this.mock.perform(post("/containers/create")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        try {
            JsonNode jsonres = mapper.readValue(res, JsonNode.class);
            dockerId = jsonres.get("id").asLong();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void bStartDockerTest() throws Exception {
        this.mock.perform(post("/containers/" + dockerId + "/start")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void cStartDockerInvalidIdTest() throws Exception {
        this.mock.perform(post("/containers/" + 500 + "/start")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void dPpauseDockerTest() throws Exception {
        this.mock.perform(post("/containers/" + dockerId + "/pause")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void eResumeDockerTest() throws Exception {
        this.mock.perform(post("/containers/" + dockerId + "/resume")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void fDockerStatsTest() throws Exception {
        this.mock.perform(get("/containers/" + dockerId + "/stats")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void gRestartDockerTest() throws Exception {
        this.mock.perform(post("/containers/" + dockerId + "/restart")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void hStopDockerTest() throws Exception {
        this.mock.perform(post("/containers/" + dockerId + "/stop")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void iDockerStatsNoStatsTest() throws Exception {
        try {
            this.mock.perform(get("/containers/" + dockerId + "/stats")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception expected) {}
    }

    @Test
    public void jDeleteDockerTest() throws Exception {
        this.mock.perform(delete("/containers/" + dockerId)
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void kDeleteDockerIncorrectIdTest() throws Exception {
        this.mock.perform(delete("/containers/" + 500)
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void lDockerStatsInvalidTest() throws Exception {
        this.mock.perform(get("/containers/" + 500 + "/stats")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void mTestRenameDocker() throws Exception {
        String res = this.mock.perform(post("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(testUser)))
        .andReturn().getResponse().getContentAsString();
        JsonNode jsonres = this.mapper.readValue(res, JsonNode.class);
        Long idDocker = jsonres.get("user").get("dockers").get(0).get("id").asLong();
        logger.info("Content res " + res.toString());
        logger.info("idDocker content : " + idDocker);
        String token = this.setJwtToken("Nathou", "123456789");
        logger.info("Token value : " + token);
        
        this.mock.perform(post("/containers/" + idDocker + "/rename/toto")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
    }

}