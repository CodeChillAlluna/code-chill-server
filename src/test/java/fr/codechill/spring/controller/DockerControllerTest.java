package fr.codechill.spring.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import fr.codechill.spring.controller.DockerController;
import fr.codechill.spring.model.Docker;
import fr.codechill.spring.utils.docker.DockerActions;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DockerControllerTest {

    @Autowired
    private DockerController dockerController;

    private static String dockerId;

    @Test
    public void aCreateDockerTest () {
        Docker docker = this.dockerController.createDocker();
        dockerId = docker.getName();
        assertNotNull(docker);
    }

    @Test
    public void bDockerActionTest () {
        String action = DockerActions.START.toString();
        assertEquals(this.dockerController.dockerAction(dockerId, action).getStatusCodeValue(), 204);
    }

    @Test
    public void cDockerStatsTest () {
        assertEquals(this.dockerController.getDockerStats(dockerId).getStatusCodeValue(), 200);
    }

    /* @Test
    public void dDockerStatsInvalidTest () {
        assertEquals(this.dockerController.getDockerStats("1").getStatusCodeValue(), 404);
    } */

    @Test
    public void eDeleteDockerTest () {
        String action = DockerActions.STOP.toString();
        this.dockerController.dockerAction(dockerId, action);
        assertEquals(this.dockerController.deleteDocker(dockerId).getStatusCodeValue(), 204);
    }
}
