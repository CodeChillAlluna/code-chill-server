package fr.codechill.spring.rest;

import java.io.Serializable;

public class CreateDockerRequest implements Serializable {

  private String name;
  private Long imageId;

  public CreateDockerRequest() {
    super();
  }

  public CreateDockerRequest(String name, Long imageId) {
    this.name = name;
    this.imageId = imageId;
  }

  /** @return String return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  public Long getImageId() {
    return this.imageId;
  }

  public void setImageId(Long imageId) {
    this.imageId = imageId;
  }
}
