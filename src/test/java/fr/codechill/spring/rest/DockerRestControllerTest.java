package fr.codechill.spring.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import java.util.ArrayList;
import java.util.Date;
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
public class DockerRestControllerTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mock;
  private static UserHelper userHelper;
  private static DockerHelper dockerHelper;
  private static User testUser;
  private String username = "DockerUserTest";
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
      dockerHelper = new DockerHelper(mock);
      userHelper = new UserHelper(mock);
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
    dockerHelper.createDocker(token, "env_DockerUserTest");
    userJson = userHelper.userInfos(token);
  }

  @After
  public void afterTest() throws Exception {
    JsonNode dockers = userJson.get("dockers");
    for (JsonNode docker : dockers) {
      dockerHelper.removeDocker(token, docker.get("id").asLong());
    }
  }

  @Test
  public void createDockerTest() throws Exception {
    CreateDockerRequest createDockerRequest = new CreateDockerRequest("DockerRestControllerTest");
    this.mock
        .perform(
            post("/containers/create")
                .header("Authorization", String.format("Bearer %s", token))
                .content(JsonHelper.asJsonString(createDockerRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void createDockerWithoutNameTest() throws Exception {
    this.mock
        .perform(
            post("/containers/create")
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void startDockerTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            post(String.format("/containers/%s/start", idDocker))
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void startDockerInvalidIdTest() throws Exception {
    this.mock
        .perform(
            post("/containers/500/start")
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void pauseDockerTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    dockerHelper.startDocker(token, idDocker);
    this.mock
        .perform(
            post(String.format("/containers/%s/pause", idDocker))
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void resumeDockerTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    dockerHelper.startDocker(token, idDocker);
    dockerHelper.pauseDocker(token, idDocker);
    this.mock
        .perform(
            post(String.format("/containers/%s/resume", idDocker))
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void dockerStatsTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    dockerHelper.startDocker(token, idDocker);
    this.mock
        .perform(
            get(String.format("/containers/%s/stats", idDocker))
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void restartDockerTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    dockerHelper.startDocker(token, idDocker);
    this.mock
        .perform(
            post(String.format("/containers/%s/restart", idDocker))
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void stopDockerTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    dockerHelper.startDocker(token, idDocker);
    this.mock
        .perform(
            post(String.format("/containers/%s/stop", idDocker))
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void dockerStatsNoStatsTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    try {
      this.mock.perform(
          get(String.format("/containers/%s/stats", idDocker))
              .header("Authorization", String.format("Bearer %s", token))
              .contentType(MediaType.APPLICATION_JSON));
    } catch (Exception expected) {
    }
  }

  @Test
  public void deleteDockerTest() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            delete(String.format("/containers/%s", idDocker))
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void deleteDockerIncorrectIdTest() throws Exception {
    this.mock
        .perform(
            delete("/containers/500")
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void dockerStatsInvalidTest() throws Exception {
    this.mock
        .perform(
            get("/containers/500/stats")
                .header("Authorization", String.format("Bearer %s", token))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testRenameDocker() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            post("/containers/" + idDocker + "/rename/toto")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void testRenameDockerOtherUser() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            post("/containers/" + 500 + "/rename/toto")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testRenameDockerWithInvalidName() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            post("/containers/" + idDocker + "/rename/******<<>>>")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testExportDocker() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            get(String.format("/containers/%d/export/", idDocker))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void testExportDockerInvalidId() throws Exception {
    this.mock
        .perform(
            get("/containers/500/export/")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testExportImage() throws Exception {
    this.mock
        .perform(
            get("/images/codechillaluna/code-chill-ide/get/")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void testExportImageInvalidId() throws Exception {
    this.mock
        .perform(
            get("/images/500/get/")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testExportFile() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            get(String.format("/containers/%d/archive/home/code/lib/index.html", idDocker))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void testExportFileInvalid() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            get(String.format("/containers/%d/archive/home/code/libb/index.html", idDocker))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testCommitChange() throws Exception {
    Long idDocker = userJson.get("dockers").get(0).get("id").asLong();
    this.mock
        .perform(
            post(String.format("/containers/%s/commit", idDocker))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void testCommitInvalidChange() throws Exception {
    this.mock
        .perform(
            post(String.format("/containers/500/commit"))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }
}
