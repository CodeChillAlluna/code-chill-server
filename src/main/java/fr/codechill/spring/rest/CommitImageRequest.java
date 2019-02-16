package fr.codechill.spring.rest;

import java.io.Serializable;

public class CommitImageRequest implements Serializable {
  private String name;
  private String version;
  private Boolean privacy;

  public CommitImageRequest() {}

  public CommitImageRequest(String name, String version, Boolean privacy) {
    this.name = name;
    this.version = version;
    this.privacy = privacy;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return this.version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean isPrivacy() {
    return this.privacy;
  }

  public Boolean getPrivacy() {
    return this.privacy;
  }

  public void setPrivacy(Boolean privacy) {
    this.privacy = privacy;
  }
}
