package fr.codechill.spring.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

@Entity
public class Docker implements Serializable {
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String name;

  private String containerId;

  private int port;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "dockers")
  private List<User> users = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(
      name = "docker_language",
      joinColumns = @JoinColumn(name = "docker_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "language_id", referencedColumnName = "id"))
  private List<Language> languages = new ArrayList<>();

  // @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn private Image image;

  private Docker() {}

  public Docker(String name, String containerId, int port, Image image) {
    this.name = name;
    this.containerId = containerId;
    this.port = port;
    this.image = image;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return this.name;
  }

  public int getPort() {
    return this.port;
  }

  public String getContainerId() {
    return this.containerId;
  }

  public List<User> getUsers() {
    return this.users;
  }

  public void setUsers(List<User> u) {
    this.users = u;
  }

  public List<Language> getLanguages() {
    return this.languages;
  }

  public void setLanguages(List<Language> l) {
    this.languages = l;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  public Image getImage() {
    return this.image;
  }

  public void setImage(Image image) {
    this.image = image;
  }
}
