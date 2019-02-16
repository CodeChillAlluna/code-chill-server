package fr.codechill.spring.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CommitImageRequestTest {

  @Test
  public void createTestEmpty() throws Exception {
    CommitImageRequest commitImageRequest = new CommitImageRequest();
    commitImageRequest.setName("toto");
    commitImageRequest.setVersion("1");
    commitImageRequest.setPrivacy(true);
    assertEquals("toto", commitImageRequest.getName());
    assertEquals("1", commitImageRequest.getVersion());
    assertEquals(true, commitImageRequest.getPrivacy());
  }

  @Test
  public void createTest() throws Exception {
    CommitImageRequest commitImageRequest = new CommitImageRequest("toto", "1", true);
    commitImageRequest.setName("toto");
    commitImageRequest.setVersion("1");
    commitImageRequest.setPrivacy(true);
    assertEquals("toto", commitImageRequest.getName());
    assertEquals("1", commitImageRequest.getVersion());
    assertEquals(true, commitImageRequest.getPrivacy());
  }
}
