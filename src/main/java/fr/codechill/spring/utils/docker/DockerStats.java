package fr.codechill.spring.utils.docker;

import java.io.Serializable;
import java.util.Objects;

public class DockerStats implements Serializable {

    private String dockerId;
    private String name;
    private Double memoryLimit;
    private Double memoryUsage;
    private Double cpuPercent;

    public DockerStats() {
        this.dockerId = "";
        this.name = "";
        this.memoryLimit = 0.0d;
        this.memoryUsage = 0.0d;
        this.cpuPercent = 0.0d;
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

    @Override
    public String toString() {
        return "{" +
            " dockerId='" + getDockerId() + "'" +
            ", name='" + getName() + "'" +
            ", memoryLimit='" + getMemoryLimit() + "'" +
            ", memoryUsage='" + getMemoryUsage() + "'" +
            ", cpuPercent='" + getCpuPercent() + "'" +
            "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DockerStats)) {
            return false;
        }
        DockerStats dockerStats = (DockerStats) o;
        return Objects.equals(dockerId, dockerStats.dockerId) &&
            Objects.equals(name, dockerStats.name) &&
            Objects.equals(memoryLimit, dockerStats.memoryLimit) &&
            Objects.equals(memoryUsage, dockerStats.memoryUsage) &&
            Objects.equals(cpuPercent, dockerStats.cpuPercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dockerId, name, memoryLimit, memoryUsage, cpuPercent);
    }
}