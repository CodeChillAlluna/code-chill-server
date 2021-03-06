package fr.codechill.spring.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImageTest {

  @Test
  public void testGetName() {
    Image image = new Image("a", "b", false);
    image.setName("b");
    assertEquals("b", image.getName());
  }

  @Test
  public void testGetVersion() {
    Image image = new Image("a", "b", false);
    image.setVersion("a");
    assertEquals("a", image.getVersion());
  }

  @Test
  public void testGetId() {
    Image image = new Image("a", "b", false);
    image.setId(Long.valueOf(2));
    assertEquals(Long.valueOf(2), image.getId());
  }

  @Test
  public void testGetPrivacy() {
    Image image = new Image("a", "b", false);
    image.setPrivacy(true);
    assertEquals(true, image.getPrivacy());
    assertEquals(true, image.isPrivacy());
  }

  @Test
  public void testGetOwner() {
    User user = new User("toto", "tata");
    Image image = new Image("a", "b", false, user);
    image.setOwner(user);
    assertEquals(user, image.getOwner());
  }

  @Test
  public void print() {
    Image image = new Image("a", "b", false);
    image.setId(1L);
    String str =
        "{"
            + " id='"
            + 1L
            + "'"
            + ", name='"
            + "a"
            + "'"
            + ", version='"
            + "b"
            + "'"
            + ", privacy='"
            + false
            + "'"
            + "}";
    assertEquals(str, image.toString());
  }
}
