package fr.codechill.spring.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DockerTest {

  private Long id;
  private User userDummy1;
  private String name;
  private String containerId;
  private Language language;
  private List<User> users = new ArrayList<>();
  private List<Language> languages = new ArrayList<>();

  @Before
  public void setUp() {
    this.id = 12345L;
    this.name = "testDocker";
    this.containerId = "containerId";
    this.language = new Language("Java");
    this.languages.add(language);
    this.users.add(userDummy1);
  }

  @Test
  public void testSetUsers() {
    this.users.clear();
    Docker dockTest = new Docker(this.name, this.containerId, 1);
    this.users.add(this.userDummy1);
    dockTest.setUsers(this.users);
    assertNotNull(dockTest.getUsers());
  }

  @Test
  public void testGetLanguages() {
    Docker dockTest = new Docker(this.name, this.containerId, 1);
    dockTest.setLanguages(this.languages);
    assertNotNull(dockTest.getLanguages());
  }

  @Test
  public void testGetName() {
    Docker dockTest = new Docker(this.name, this.containerId, 1);
    dockTest.setName(this.name);
    assertEquals(this.name, dockTest.getName());
  }

  @Test
  public void testGetUsers() {
    Docker dockTest = new Docker(this.name, this.containerId, 1);
    dockTest.setUsers(this.users);
    assertNotNull(dockTest.getUsers());
  }

  @Test
  public void testGetContainerId() {
    Docker dockTest = new Docker(this.name, this.containerId, 1);
    assertEquals(this.containerId, dockTest.getContainerId());
  }

  @Test
  public void testSetContainerId() {
    Docker dockTest = new Docker(this.name, "Old Id", 1);
    dockTest.setContainerId(this.containerId);
    assertEquals(this.containerId, dockTest.getContainerId());
  }
}
