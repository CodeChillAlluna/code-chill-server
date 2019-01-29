package fr.codechill.spring.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LanguageTest {

  private Long id;
  private String name;
  private Docker docker;
  private Language language;
  private List<Docker> dockers;

  @Before
  public void setUp() {
    this.id = 1L;
    this.name = "testLanguage";
    this.docker = new Docker(this.name, "test", 1, new Image("a", "b", false));
    this.dockers = new ArrayList<>();
    dockers.add(docker);
  }

  @Test
  public void testGetId() {
    this.language = new Language("test");
    this.language.setId(this.id);
    assertEquals(this.id, language.getId());
  }

  @Test
  public void testGetName() {
    this.language = new Language("test");
    this.language.setName(this.name);
    assertEquals(this.name, language.getName());
  }

  @Test
  public void testSetName() {
    this.language = new Language("test");
    this.language.setName("testLanguage");
    assertEquals(this.name, this.language.getName());
  }

  @Test
  public void testgetDockers() {
    this.language = new Language("test");
    this.language.setDockers(dockers);
    assertNotNull(this.language.getDockers());
  }

  @Test
  public void testSetDockers() {
    this.dockers.clear();
    this.dockers.add(this.docker);
    this.language = new Language("Test");
    this.language.setDockers(dockers);
    assertNotNull(this.language.getDockers());
  }
}
