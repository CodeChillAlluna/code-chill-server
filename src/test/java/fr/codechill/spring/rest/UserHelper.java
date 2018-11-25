package fr.codechill.spring.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import fr.codechill.spring.model.security.AuthorityName;
import java.util.Date;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class UserHelper {

  private MockMvc mock;
  private final ObjectMapper mapper = new ObjectMapper();

  UserHelper(MockMvc mock) {
    this.mock = mock;
  }

  public User setUpUser(
      String username,
      String password,
      String firstname,
      String lastname,
      String email,
      Boolean enabled,
      Date lastPasswordResetDate,
      List<Authority> authorities) {
    User user = new User(lastname, firstname);
    user.setUsername(username);
    user.setPassword(password);
    user.setEmail(email);
    user.setLastPasswordResetDate(lastPasswordResetDate);
    user.setEnabled(enabled);
    if (authorities.isEmpty()) {
      authorities.add(this.createAuthority(1L, AuthorityName.ROLE_USER));
      user.setAuthorities(authorities);
    } else {
      user.setAuthorities(authorities);
    }
    return user;
  }

  public Authority createAuthority(Long id, AuthorityName name) {
    Authority authority = new Authority();
    authority.setId(id);
    authority.setName(name);
    return authority;
  }

  public JsonNode createUser(User user) throws Exception {
    String res =
        this.mock
            .perform(
                post("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonHelper.asJsonString(user)))
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode json = mapper.readTree(res);
    return json.get("user");
  }

  public String authUser(String userUsername, String userPassword) {
    ObjectNode body = this.mapper.createObjectNode();
    body.put("username", userUsername);
    body.put("password", userPassword);
    String jwtToken;
    try {
      String res =
          this.mock
              .perform(
                  post("/auth")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(JsonHelper.asJsonString(body)))
              .andReturn()
              .getResponse()
              .getContentAsString();
      JsonNode jsonres = this.mapper.readValue(res, JsonNode.class);
      jwtToken = jsonres.get("token").textValue();
    } catch (Exception e) {
      jwtToken = null;
      e.printStackTrace();
    }
    return jwtToken;
  }

  public JsonNode userInfos(String userJwtToken) throws Exception {
    String res =
        this.mock
            .perform(get("/user").header("Authorization", String.format("Bearer %s", userJwtToken)))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return mapper.readTree(res);
  }

  public void deleteUser(String userJwtToken) throws Exception {
    this.mock
        .perform(delete("/user").header("Authorization", String.format("Bearer %s", userJwtToken)))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }
}
