package fr.codechill.spring.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImageTest {

  @Test
  public void testGetName() {
    Image image = new Image("a", "b");
    image.setName("b");
    assertEquals("b", image.getName());
  }

  @Test
  public void testGetVersion() {
    Image image = new Image("a", "b");
    image.setVersion("a");
    assertEquals("a", image.getVersion());
  }

  @Test
  public void testGetId() {
    Image image = new Image("a", "b");
    image.setId(Long.valueOf(2));
    assertEquals(Long.valueOf(2), image.getId());
  }
}
