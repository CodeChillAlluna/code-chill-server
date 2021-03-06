package fr.codechill.spring.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import fr.codechill.spring.model.Docker;
import fr.codechill.spring.model.Image;
import fr.codechill.spring.utils.docker.DockerActions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerControllerTest {

  @Autowired private DockerController dockerController;

  private static String nomDocker = "DockerControllerTest";
  private static Image image = new Image("codechillaluna/code-chill-ide", "latest", false);

  @Test
  public void createDockerTest() {
    Docker docker = this.dockerController.createDocker(nomDocker, image);
    assertNotNull(docker);
    String dockerId = docker.getContainerId();
    String action = DockerActions.STOP.toString();
    this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    this.dockerController.deleteDocker(dockerId);
  }

  @Test
  public void dockerActionTest() {
    Docker docker = this.dockerController.createDocker(nomDocker, image);
    String dockerId = docker.getContainerId();
    String action = DockerActions.START.toString();
    assertEquals(
        this.dockerController.dockerAction(dockerId, action, HttpMethod.POST).getStatusCodeValue(),
        204);
    action = DockerActions.STOP.toString();
    this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    this.dockerController.deleteDocker(dockerId);
  }

  @Test
  public void dockerStatsTest() {
    Docker docker = this.dockerController.createDocker(nomDocker, image);
    String dockerId = docker.getContainerId();
    String action = DockerActions.START.toString();
    this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    assertEquals(this.dockerController.getDockerStats(dockerId).getStatusCodeValue(), 200);
    action = DockerActions.STOP.toString();
    this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    this.dockerController.deleteDocker(dockerId);
  }

  @Test
  public void dockerInspectNoInfoTest() throws Exception {
    Docker docker = this.dockerController.createDocker(nomDocker, image);
    String dockerId = docker.getContainerId();
    try {
      String action = DockerActions.INSPECT.toString();
      this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    } catch (Exception expected) {
    }
    String action = DockerActions.STOP.toString();
    this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    this.dockerController.deleteDocker(dockerId);
  }

  @Test
  public void deleteDockerTest() {
    Docker docker = this.dockerController.createDocker(nomDocker, image);
    String dockerId = docker.getContainerId();
    String action = DockerActions.START.toString();
    this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    action = DockerActions.STOP.toString();
    this.dockerController.dockerAction(dockerId, action, HttpMethod.POST);
    assertEquals(this.dockerController.deleteDocker(dockerId).getStatusCodeValue(), 204);
    this.dockerController.deleteDocker(dockerId);
  }
}
