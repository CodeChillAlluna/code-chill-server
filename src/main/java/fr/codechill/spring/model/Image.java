package fr.codechill.spring.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(
    name = "image",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "version"})})
public class Image implements Serializable {

  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Long id;

  @NotNull
  @Column(name = "name")
  private String name;

  @NotNull
  @Column(name = "version")
  private String version;

  @NotNull
  @Column(name = "private")
  private Boolean privacy;

  @Column(name = "owner")
  @JoinColumn(nullable = true)
  private User owner;

  public Image() {}

  public Image(String name, String version, Boolean privacy) {
    this.name = name;
    this.version = version;
    this.privacy = privacy;
  }

  public Image(String name, String version, Boolean privacy, User owner) {
    this.name = name;
    this.version = version;
    this.privacy = privacy;
    this.owner = owner;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public User getOwner() {
    return this.owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  @Override
  public String toString() {
    return "{"
        + " id='"
        + id
        + "'"
        + ", name='"
        + name
        + "'"
        + ", version='"
        + version
        + "'"
        + ", privacy='"
        + privacy
        + "'"
        + "}";
  }
}
