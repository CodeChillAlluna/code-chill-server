package fr.codechill.spring.Controller;

import org.springframework.beans.factory.annotation.Value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.ActiveProfiles;


import fr.codechill.spring.CodeChillApplication;
import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.repository.DockerRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerControllerTest {
    private DockerRepository drepo;
    private DockerController dockerController;

    @Before
    public void setUp() {
        this.dockerController = new DockerController(this.drepo);
    }

    @Test
    public void dockerCreationTest () {
        assertNotEquals(this.dockerController.dockerCreation(),"-1");
    }
}
