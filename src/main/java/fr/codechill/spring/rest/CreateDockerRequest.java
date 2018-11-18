package fr.codechill.spring.rest;

import java.io.Serializable;

public class CreateDockerRequest implements Serializable {

    private String name;

    public CreateDockerRequest() {
        super();
    }

    public CreateDockerRequest(String name) {
        this.name = name;
    }
    
    /**
     * @return String return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}