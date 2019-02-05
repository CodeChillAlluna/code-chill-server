package fr.codechill.spring.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
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
public class DockerShareRestControllerTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mock;
  private static UserHelper userHelper;
  private static DockerHelper dockerHelper;
  private static User testUser;
  private String username = "DockerShareRestTest";
  private String password = "123456789";
  private String firstname = "Docker";
  private String lastname = "User";
  private String email = "DockerUserTest@bonjour.com";
  private Boolean enabled = true;
  private Date lastPasswordResetDate = new Date(1993, 12, 12);
  private static JsonNode userJson;
  private static String token;

  @Before
  public void setUp() throws Exception {
    this.mock = MockMvcBuilders.webAppContextSetup(context).build();
    if (userJson == null) {
      userHelper = new UserHelper(mock);
      dockerHelper = new DockerHelper(mock);
      testUser =
          userHelper.setUpUser(
              username,
              password,
              firstname,
              lastname,
              email,
              enabled,
              lastPasswordResetDate,
              new ArrayList<Authority>());
      userJson = userHelper.createUser(testUser);
      token = userHelper.authUser(this.username, this.password);
    }
    userJson = userHelper.userInfos(token);
  }

  @After
  public void afterTest() throws Exception {
    userJson = userHelper.userInfos(token);
    JsonNode dockers = userJson.get("dockers");
    for (JsonNode docker : dockers) {
      dockerHelper.removeDocker(token, docker.get("id").asLong());
    }
    UnshareRequest unshareRequest = new UnshareRequest(4L);
    this.mock
        .perform(
            delete("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(unshareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  public void getSharedEnvTest() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(4L);
    this.mock
        .perform(
            post("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(shareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    this.mock
        .perform(get("/user/env/shared").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isOk());
  }

  @Test
  public void getUsersSharedEnvTest() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(4L);
    this.mock
        .perform(
            post("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(shareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    this.mock
        .perform(
            get("/user/env/1/shared").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isOk());
  }

  @Test
  public void shareEnv() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(4L);
    this.mock
        .perform(
            post("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(shareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void shareEnvInvalid() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(400L);
    this.mock
        .perform(
            post("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(shareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void checkAccessSuccess() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(4L);
    this.mock
        .perform(
            post("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(shareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    this.mock
        .perform(
            get("/user/env/1/check").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isOk());
  }

  @Test
  public void checkAccessExpired() throws Exception {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
    c.setTimeInMillis(1328435650000L);
    Date date = c.getTime();
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(4L);
    shareRequest.setExpirationDate(date);
    this.mock
        .perform(
            post("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(shareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    this.mock
        .perform(
            get("/user/env/1/check").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void checkAccessFailed() throws Exception {
    this.mock
        .perform(
            get("/user/env/1/check").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void unshareEnvNoUser() throws Exception {
    UnshareRequest unshareRequest = new UnshareRequest(4L);
    this.mock
        .perform(
            delete("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(unshareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void unshareEnv() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(4L);
    this.mock
        .perform(
            post("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(shareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    UnshareRequest unshareRequest = new UnshareRequest(4L);
    this.mock
        .perform(
            delete("/user/env/1/share")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(unshareRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
