package fr.codechill.spring.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;

@Entity
public class Language implements Serializable {
  @Id
  @NotNull
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String name;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "languages")
  @JsonIgnoreProperties("dockers")
  private List<Docker> dockers = new ArrayList<>();

  private Language() {}

  public Language(String n) {
    this.name = n;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public List<Docker> getDockers() {
    return this.dockers;
  }

  public void setName(String n) {
    this.name = n;
  }

  public void setDockers(List<Docker> d) {
    this.dockers = d;
  }
}
