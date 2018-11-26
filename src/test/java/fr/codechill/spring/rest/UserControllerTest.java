package fr.codechill.spring.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class UserControllerTest {

  @Autowired private WebApplicationContext context;
  private MockMvc mock;
  private static ObjectMapper mapper;
  private static UserHelper userHelper;
  private static User testUser;
  private String username = "Nathou";
  private String password = "123456789";
  private String firstname = "Nathan";
  private String lastname = "Michanol";
  private String email = "nathou@bonjour.com";
  private Boolean enabled = true;
  private String wrongToken =
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJOb1VzZXIiLCJhdWQiOiJ3ZWIiLCJleHAiOjExNTQzMjUyOTk3LCJpYXQiOjE1NDMyNTI5OTh9.3qpbCkt8709a2OVLwaujc8Cv5WItUUQ5J4rj2p0L-niaSqXLnGJRvBC-rh29eZZQGbZOYWfgqPAAKJresGfBQQ";
  private Date lastPasswordResetDate = new Date(1993, 12, 12);

  @Before
  public void setUp() {
    this.mock = MockMvcBuilders.webAppContextSetup(context).build();
    if (userHelper == null) {
      userHelper = new UserHelper(mock);
      mapper = new ObjectMapper();
      testUser =
          userHelper.setUpUser(
              this.username,
              this.password,
              this.firstname,
              this.lastname,
              this.email,
              this.enabled,
              this.lastPasswordResetDate,
              new ArrayList<Authority>());
    }
  }

  @Test
  public void testAddUser() throws Exception {
    this.mock
        .perform(
            post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonHelper.asJsonString(testUser)))
        .andExpect(status().is2xxSuccessful());
    String token = userHelper.authUser(this.username, this.password);
    userHelper.deleteUser(token);
  }

  @Test
  public void testGetUserWrongUser() throws Exception {
    this.mock
        .perform(get("/user/1000000000").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testGetUserWrongToken() throws Exception {
    this.mock
        .perform(
            get("/user/1000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", this.wrongToken)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testGetUserInvalidToken() throws Exception {
    try {
      this.mock
          .perform(
              get("/user/1000000000")
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(
                      "Authorization",
                      String.format(
                          "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJOb1VzZXIiLCJhdWQiOiJ3ZWIiLCJleHAiOjE1NDMyNTI5OTksImlhdCI6MTU0MzI1Mjk5OH0.A0bS4vLZTMYXCVHodzqrAH5nrxfqd12q2YQBl7SJS4f6_Wu7DWW4LMGa_A8wG5Ii0UylwTYRtq0Hd17vCTQelw")))
          .andExpect(status().is4xxClientError());
    } catch (Exception e) {

    }
  }

  @Test
  public void testDeleteUserWrongToken() throws Exception {
    this.mock
        .perform(
            delete("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", this.wrongToken)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testAddUserUsername() throws Exception {
    testUser.setUsername("dummy");
    this.mock
        .perform(
            post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonHelper.asJsonString(testUser)))
        .andExpect(status().is4xxClientError());
    testUser.setUsername(this.username);
  }

  @Test
  public void testAddUserEmail() throws Exception {
    testUser.setEmail("admin@admin.com");
    this.mock
        .perform(
            post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonHelper.asJsonString(testUser)))
        .andExpect(status().is4xxClientError());
    testUser.setEmail(this.email);
  }

  @Test
  public void testEditUserWrongToken() throws Exception {
    userHelper.createUser(testUser);
    this.mock
        .perform(
            put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", this.wrongToken))
                .content(JsonHelper.asJsonString(testUser)))
        .andExpect(status().is4xxClientError());
    String token = userHelper.authUser(this.username, this.password);
    userHelper.deleteUser(token);
  }

  @Test
  public void testEditUser() throws Exception {
    userHelper.createUser(testUser);
    testUser.setFirstname("test");
    testUser.setLastname("test");
    String token = userHelper.authUser(this.username, this.password);
    this.mock
        .perform(
            put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(testUser)))
        .andExpect(status().is2xxSuccessful());
    testUser.setFirstname(this.firstname);
    testUser.setLastname(this.lastname);
    userHelper.deleteUser(token);
  }

  @Test
  public void testAuth() throws Exception {
    userHelper.createUser(testUser);
    ObjectNode body = mapper.createObjectNode();
    body.put("username", username);
    body.put("password", password);
    String res =
        this.mock
            .perform(
                post("/auth")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonHelper.asJsonString(body)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode jsonres = mapper.readValue(res, JsonNode.class);
    String token = jsonres.get("token").textValue();
    userHelper.deleteUser(token);
  }

  @Test
  public void testAuthWrongUser() throws Exception {
    ObjectNode body = mapper.createObjectNode();
    body.put("username", "azerty");
    body.put("password", "azerty");
    this.mock
        .perform(
            post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonHelper.asJsonString(body)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetUser() throws Exception {
    userHelper.createUser(testUser);
    String token = userHelper.authUser(username, password);
    this.mock
        .perform(
            get("/user/1")
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    userHelper.deleteUser(token);
  }

  @Test
  public void testGetBadUser() throws Exception {
    userHelper.createUser(testUser);
    String token = userHelper.authUser(username, password);
    this.mock
        .perform(
            get("/user/500")
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
    userHelper.deleteUser(token);
  }

  /* @Test
  public void testResetPassword() throws Exception{
      this.setJwtToken(this.testUser.getUsername(),this.testUser.getPassword());
      this.mock.perform(get("/reset/"+this.jwtToken)
      .header("Authorization","Bearer "+this.jwtToken))
      .andExpect(status().isOk());
  }
  */

  /*@Test
  public void testEditUser() throws Exception {
      PrintWriter writer = new PrintWriter("/vagrant/test.txt");
      this.setJwtToken(testUser2.getUsername(), testUser2.getPassword());
      writer.println(this.jwtToken);
      writer.println(testUser2.getUsername());
      writer.println(testUser2.getPassword());
      writer.close();
      this.testUser2.setLastname("Simon");
      this.testUser2.setFirstname("Ludwig");
      String token = this.jwtToken;
      String res = this.mock.perform(put("/user")
          .header("Authorization", "Bearer " + token)
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(testUser2)))
          .andExpect(status().is2xxSuccessful())
          .andReturn().getResponse().getContentAsString();
      // writer.println(res);
  }*/

  @Test
  public void testDelete() throws Exception {
    System.out.println("TESTDELETE");
    System.out.println(testUser.getEmail());
    System.out.println(testUser.getUsername());
    JsonNode user = userHelper.createUser(testUser);
    System.out.println(user);
    String token = userHelper.authUser(username, password);
    System.out.println(token);
    this.mock
        .perform(delete("/user").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().is2xxSuccessful());
  }
}
