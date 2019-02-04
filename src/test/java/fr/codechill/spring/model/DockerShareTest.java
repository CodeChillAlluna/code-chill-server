package fr.codechill.spring.model;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import org.junit.Test;

public class DockerShareTest {
  @Test
  public void createEmpty() {
    Date date = new Date();
    DockerShare dockerShare = new DockerShare();
    dockerShare.setDockerId(1L);
    dockerShare.setExpiration(date);
    dockerShare.setId(1L);
    dockerShare.setReadOnly(true);
    dockerShare.setUserId(1L);
    assertEquals(Long.valueOf(1), dockerShare.getDockerId());
    assertEquals(date, dockerShare.getExpiration());
    assertEquals(Long.valueOf(1), dockerShare.getUserId());
    assertEquals(true, dockerShare.getReadOnly());
    assertEquals(Long.valueOf(1), dockerShare.getId());
  }

  @Test
  public void createFullConstructor() {
    Date date = new Date();
    DockerShare dockerShare = new DockerShare(1L, 1L, true, date);
    assertEquals(Long.valueOf(1), dockerShare.getDockerId());
    assertEquals(date, dockerShare.getExpiration());
    assertEquals(Long.valueOf(1), dockerShare.getUserId());
    assertEquals(true, dockerShare.isReadOnly());
  }

  @Test
  public void createMinimalConstructor() {
    Date date = new Date();
    DockerShare dockerShare = new DockerShare(1L, 1L);
    dockerShare.setExpiration(date);
    dockerShare.setReadOnly(true);
    assertEquals(Long.valueOf(1), dockerShare.getDockerId());
    assertEquals(date, dockerShare.getExpiration());
    assertEquals(Long.valueOf(1), dockerShare.getUserId());
    assertEquals(true, dockerShare.isReadOnly());
  }

  @Test
  public void createConstructor() {
    Date date = new Date();
    DockerShare dockerShare = new DockerShare(1L, 1L, true);
    dockerShare.setExpiration(date);
    assertEquals(Long.valueOf(1), dockerShare.getDockerId());
    assertEquals(date, dockerShare.getExpiration());
    assertEquals(Long.valueOf(1), dockerShare.getUserId());
    assertEquals(true, dockerShare.isReadOnly());
  }
}
