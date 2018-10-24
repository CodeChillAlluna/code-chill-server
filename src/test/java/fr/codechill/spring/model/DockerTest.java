package fr.codechill.spring.model;

import java.util.ArrayList;
import java.util.List;

import fr.codechill.spring.model.User;
import fr.codechill.spring.model.Docker;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DockerTest { 
    
    private Long id;
    private User userDummy1;
    private String name;
    private Language language;
    private List<User> users = new ArrayList<>();
    private List<Language> languages = new ArrayList<>();

    @Before
    public void setUp() {
        this.id = 12345L;
        this.name = "testDocker";
        this.language = new Language ("Java");
        this.languages.add(language);
        this.users.add(userDummy1);
    }

    @Test
    public void testSetUsers() {
        this.users.clear();
        Docker dockTest = new Docker(this.name, 1);
        this.users.add(this.userDummy1);
        dockTest.setUsers(this.users);
        assertNotNull(dockTest.getUsers());
    }

    @Test
    public void testGetLanguages() {
        Docker dockTest = new Docker (this.name, 1);
        dockTest.setLanguages(this.languages);
        assertNotNull(dockTest.getLanguages());
    }
    @Test
    public void testGetName() {
        Docker dockTest = new Docker (this.name, 1);
        assertEquals(this.name, dockTest.getName());
    }

    @Test
    public void testGetUsers() {
        Docker dockTest = new Docker(this.name, 1);
        dockTest.setUsers(this.users);
        assertNotNull(dockTest.getUsers());
    }

}