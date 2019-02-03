package fr.codechill.spring.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
public class DockerShare implements Serializable {
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Long id;

  @Column(name = "userId")
  @JoinColumn
  @NotNull
  private Long userId;

  @Column(name = "dockerId")
  @JoinColumn
  @NotNull
  private Long dockerId;

  @Column(name = "readOnly")
  @NotNull
  private Boolean readOnly;

  @Column(name = "date")
  @Temporal(TemporalType.TIMESTAMP)
  @NotNull
  private Date expiration;

  public DockerShare() {}

  public DockerShare(Long id, Long userId, Long dockerId, Boolean readOnly, Date expiration) {
    this.id = id;
    this.userId = userId;
    this.dockerId = dockerId;
    this.readOnly = readOnly;
    this.expiration = expiration;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return this.userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getDockerId() {
    return this.dockerId;
  }

  public void setDockerId(Long dockerId) {
    this.dockerId = dockerId;
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

  public Date getExpiration() {
    return this.expiration;
  }

  public void setExpiration(Date expiration) {
    this.expiration = expiration;
  }
}
