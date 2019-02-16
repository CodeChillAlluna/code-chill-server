package fr.codechill.spring.rest;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class UnshareRequest implements Serializable {

  @NotNull private Long userId;

  public UnshareRequest() {}

  public UnshareRequest(Long userId) {
    this.userId = userId;
  }

  public Long getUserId() {
    return this.userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
