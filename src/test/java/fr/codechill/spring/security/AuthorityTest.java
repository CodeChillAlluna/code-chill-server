package fr.codechill.spring.security;

import org.springframework.boot.test.context.SpringBootTest;

import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class AuthorityTest {

    private User user;
    private List <User> users;
    private Authority authority;

    @Before 
    public void setUp() {
        this.user = new User("lastname", "firstname");
        this.users = new ArrayList<User>();
        users.add(user);
    }

    @Test
    public void testGetSetUsers() {
        this.authority = new Authority();
        this.authority.setUsers(users);
        assertEquals(this.authority.getUsers(), this.users);
    }
}