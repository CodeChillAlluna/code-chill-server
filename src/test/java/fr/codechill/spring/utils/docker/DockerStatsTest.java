package fr.codechill.spring.utils.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DockerStatsTest {

    private String dockerId;
    private String name;
    private Double memoryLimit;
    private Double memoryUsage;
    private Double cpuPercent;
    
    @Before 
    public void setUp() {
        this.dockerId = "A";
        this.name = "A";
        this.memoryLimit = 1.0d;
        this.memoryUsage = 1.0d;
        this.cpuPercent = 1.0d;
    }

    @Test
    public void testConstructor() {
        DockerStats dockerStats = new DockerStats();
        DockerStats dockerStats2 = new DockerStats();
        dockerStats2.setDockerId("");
        dockerStats2.setName("");
        dockerStats2.setMemoryLimit(0.0d);
        dockerStats2.setMemoryUsage(0.0d);
        dockerStats2.setCpuPercent(0.0d);
        assertEquals(dockerStats, dockerStats2);
    }

    @Test
    public void testGetSet() {
        DockerStats dockerStats = new DockerStats();
        dockerStats.setDockerId(this.dockerId);
        dockerStats.setName(this.name);
        dockerStats.setMemoryLimit(this.memoryLimit);
        dockerStats.setMemoryUsage(this.memoryUsage);
        dockerStats.setCpuPercent(this.cpuPercent);
        assertEquals(dockerStats.getDockerId(), this.dockerId);
        assertEquals(dockerStats.getName(), this.name);
        assertEquals(dockerStats.getMemoryLimit(), this.memoryLimit);
        assertEquals(dockerStats.getMemoryUsage(), this.memoryUsage);
        assertEquals(dockerStats.getCpuPercent(), this.cpuPercent);
    }

    @Test
    public void testToString() {
        DockerStats dockerStats = new DockerStats();
        assertEquals(dockerStats.toString(), "{" +
        " dockerId='" + "" + "'" +
        ", name='" + "" + "'" +
        ", memoryLimit='" + 0.0d + "'" +
        ", memoryUsage='" + 0.0d + "'" +
        ", cpuPercent='" + 0.0d + "'" +
        "}");
    }

    @Test
    public void testHashCode() {
        DockerStats dockerStats = new DockerStats();
        assertEquals(dockerStats.hashCode(), Objects.hash("", "", 0.0d, 0.0d, 0.0d));
    }

    @Test
    public void testEqualsTrue() {
        DockerStats dockerStats = new DockerStats();
        assertTrue(dockerStats.equals(dockerStats));
    }

    @Test
    public void testEqualsFalse() {
        DockerStats dockerStats = new DockerStats();
        assertFalse(dockerStats.equals(new Object()));
    }
}