package fr.codechill.spring.security;

import static org.junit.Assert.assertEquals;

import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuthorityTest {

  private List<User> users;

  @Before
  public void setUp() {
    User user = new User("lastname", "firstname");
    this.users = new ArrayList<User>();
    this.users.add(user);
  }

  @Test
  public void testGetSetUsers() {
    Authority authority = new Authority();
    authority.setUsers(users);
    assertEquals(authority.getUsers(), this.users);
  }
}
