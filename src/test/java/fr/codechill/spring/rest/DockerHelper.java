package fr.codechill.spring.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class DockerHelper {

  private MockMvc mock;

  DockerHelper(MockMvc mock) {
    this.mock = mock;
  }

  public void createDocker(String userJwtToken, String dockerName) throws Exception {
    CreateDockerRequest createDockerRequest = new CreateDockerRequest(dockerName);
    this.mock
        .perform(
            post("/containers/create")
                .header("Authorization", String.format("Bearer %s", userJwtToken))
                .content(JsonHelper.asJsonString(createDockerRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  public void startDocker(String userJwtToken, Long idDocker) throws Exception {
    this.mock
        .perform(
            post(String.format("/containers/%d/start", idDocker))
                .header("Authorization", String.format("Bearer %s", userJwtToken))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  public void pauseDocker(String userJwtToken, Long idDocker) throws Exception {
    this.mock
        .perform(
            post(String.format("/containers/%d/pause", idDocker))
                .header("Authorization", String.format("Bearer %s", userJwtToken))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  public void stopDocker(String userJwtToken, Long idDocker) throws Exception {
    this.mock
        .perform(
            post(String.format("/containers/%d/stop", idDocker))
                .header("Authorization", String.format("Bearer %s", userJwtToken))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  public void removeDocker(String userJwtToken, Long idDocker) throws Exception {
    this.stopDocker(userJwtToken, idDocker);
    this.mock
        .perform(
            delete(String.format("/containers/%d", idDocker))
                .header("Authorization", String.format("Bearer %s", userJwtToken))
                .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }
}
