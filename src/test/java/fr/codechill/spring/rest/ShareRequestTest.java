package fr.codechill.spring.rest;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ShareRequestTest {
  @Test
  public void createEmptyTest() throws Exception {
    ShareRequest shareRequest = new ShareRequest();
    Date date = new Date();
    shareRequest.setExpirationDate(date);
    shareRequest.setReadOnly(true);
    shareRequest.setUserId(1L);
    assertEquals(Long.valueOf(1), shareRequest.getUserId());
    assertEquals(true, shareRequest.isReadOnly());
    assertEquals(date, shareRequest.getExpirationDate());
  }

  @Test
  public void createTest() throws Exception {
    Date date = new Date();
    ShareRequest shareRequest = new ShareRequest(1L, date, true);
    assertEquals(Long.valueOf(1), shareRequest.getUserId());
    assertEquals(true, shareRequest.getReadOnly());
    assertEquals(date, shareRequest.getExpirationDate());
  }
}
