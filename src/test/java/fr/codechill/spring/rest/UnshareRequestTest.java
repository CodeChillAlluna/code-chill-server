package fr.codechill.spring.rest;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UnshareRequestTest {
  @Test
  public void createEmptyTest() throws Exception {
    UnshareRequest unshareRequest = new UnshareRequest();
    unshareRequest.setUserId(1L);
    assertEquals(Long.valueOf(1), unshareRequest.getUserId());
  }

  @Test
  public void createTest() throws Exception {
    UnshareRequest unshareRequest = new UnshareRequest(1L);
    assertEquals(Long.valueOf(1), unshareRequest.getUserId());
  }
}