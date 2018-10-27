package fr.codechill.spring.Controller;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.repository.DockerRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerControllerTest {
    private DockerRepository drepo;

    @Autowired
    private DockerController dockerController;

    @Test
    public void createDockerTest () {
        assertNotEquals(this.dockerController.createDocker(), null);
    }
}
