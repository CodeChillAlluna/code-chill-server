package fr.codechill.spring.model;

import static org.junit.Assert.assertEquals;

import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {

  private String nom;
  private String prenom;
  private String nomDocker;
  private String containerId;

  @Before
  public void setUp() {
    this.nom = "michanol";
    this.prenom = "nathan";
    this.containerId = "containerId";
    this.nomDocker = "testNomDocker";
  }

  @Test
  public void testAddDocker() {
    User userTest = new User(this.nom, this.prenom);
    Docker dockerTest = new Docker(this.nomDocker, this.containerId, 1, new Image("a", "b", false));
    userTest.addDocker(dockerTest);
    assertEquals(true, userTest.getDockers().contains(dockerTest));
  }

  @Test
  public void testHash() {
    User userTest = new User(this.nom, this.prenom);
    userTest.setEmail("test@gmail.com");
    userTest.setId(1L);
    userTest.setUsername("azerty");
    assertEquals(Objects.hash(1L, "azerty", "test@gmail.com"), userTest.hashCode());
  }
}
