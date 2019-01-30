package fr.codechill.spring.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CreateDockerRequestTest {

  @Test
  public void createEmptyTest() throws Exception {
    CreateDockerRequest createDockerRequest = new CreateDockerRequest();
    createDockerRequest.setName("name");
    assertEquals("name", createDockerRequest.getName());
  }

  @Test
  public void createTest() throws Exception {
    CreateDockerRequest createDockerRequest = new CreateDockerRequest("name", 1L);
    assertEquals("name", createDockerRequest.getName());
    assertEquals(Long.valueOf(1), createDockerRequest.getImageId());
  }
}
