package fr.codechill.spring.utils.docker;

import java.io.Serializable;
import java.util.Objects;

public class DockerStats implements Serializable {

    private String dockerId;
    private String name;
    private Double memoryLimit;
    private Double memoryUsage;
    private Double cpuPercent;
    private String created;
    private String image;
    private String status;

    public DockerStats() {
        this.dockerId = "";
        this.name = "";
        this.memoryLimit = 0.0d;
        this.memoryUsage = 0.0d;
        this.cpuPercent = 0.0d;
        this.created = "";
        this.image = "";
        this.status = "";
    }

    public String getDockerId() {
        return this.dockerId;
    }

    public void setDockerId(String dockerId) {
        this.dockerId = dockerId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMemoryLimit() {
        return this.memoryLimit;
    }

    public void setMemoryLimit(Double memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public Double getMemoryUsage() {
        return this.memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Double getCpuPercent() {
        return this.cpuPercent;
    }

    public void setCpuPercent(Double cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public String getCreated() {
        return this.created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DockerStats)) {
            return false;
        }
        DockerStats dockerStats = (DockerStats) o;
        return Objects.equals(dockerId, dockerStats.dockerId) && Objects.equals(name, dockerStats.name) && Objects.equals(memoryLimit, dockerStats.memoryLimit) && Objects.equals(memoryUsage, dockerStats.memoryUsage) && Objects.equals(cpuPercent, dockerStats.cpuPercent) && Objects.equals(created, dockerStats.created) && Objects.equals(image, dockerStats.image) && Objects.equals(status, dockerStats.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dockerId, name, memoryLimit, memoryUsage, cpuPercent, created, image, status);
    }

    @Override
    public String toString() {
        return "{" +
            " dockerId='" + getDockerId() + "'" +
            ", name='" + getName() + "'" +
            ", memoryLimit='" + getMemoryLimit() + "'" +
            ", memoryUsage='" + getMemoryUsage() + "'" +
            ", cpuPercent='" + getCpuPercent() + "'" +
            ", created='" + getCreated() + "'" +
            ", image='" + getImage() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }

}