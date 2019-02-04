package fr.codechill.spring.rest;

import java.io.Serializable;
import java.util.Date;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class ShareRequest implements Serializable {

  @NotNull private Long userId;
  @NotNull private Boolean readOnly;
  @Nullable private Date expirationDate;

  public ShareRequest() {}

  public ShareRequest(Long userId, Date expirationDate, Boolean readOnly) {
    this.userId = userId;
    this.expirationDate = expirationDate;
    this.readOnly = readOnly;
  }

  public Long getUserId() {
    return this.userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Date getExpirationDate() {
    return this.expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Boolean isReadOnly() {
    return this.readOnly;
  }

  public Boolean getReadOnly() {
    return this.readOnly;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }
}
