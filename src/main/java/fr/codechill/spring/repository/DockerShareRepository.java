package fr.codechill.spring.repository;

import fr.codechill.spring.model.DockerShare;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DockerShareRepository extends JpaRepository<DockerShare, Long> {

  DockerShare findOne(long id);

  DockerShare save(DockerShare dockerShare);

  List<DockerShare> findAll();

  void delete(DockerShare dockerShare);

  void deleteByUserId(Long userId);

  void deleteByDockerId(Long dockerId);

  List<DockerShare> findByDockerId(Long dockerId);

  List<DockerShare> findByUserId(Long userId);

  DockerShare findByDockerIdAndUserId(Long dockerId, Long userId);
}
