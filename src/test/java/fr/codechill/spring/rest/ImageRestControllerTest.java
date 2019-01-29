package fr.codechill.spring.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class ImageRestControllerTest {

  @Autowired private WebApplicationContext context;

  private MockMvc mock;
  private static UserHelper userHelper;
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
    userJson = userHelper.userInfos(token);
  }

  @Test
  public void getAllImages() throws Exception {
    this.mock
        .perform(get("/images").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isOk());
  }

  @Test
  public void getImageById() throws Exception {
    this.mock
        .perform(get("/images/1").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isOk());
  }

  @Test
  public void getImageByIdInvalid() throws Exception {
    this.mock
        .perform(get("/images/100").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getAllOwnerImages() throws Exception {
    this.mock
        .perform(get("/user/images").header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isOk());
  }

  @Test
  public void updatePrivacyFaillure() throws Exception {
    this.mock
        .perform(
            put("/images/1/privacy/true")
                .header("Authorization", String.format("Bearer %s", token)))
        .andExpect(status().isBadRequest());
  }
}
