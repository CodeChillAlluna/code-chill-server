package fr.codechill.spring.repository;

import fr.codechill.spring.model.Docker;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface DockerRepository extends CrudRepository<Docker, Long> {
  List<Docker> findAll();

  Docker findOne(long id);

  List<Docker> findByIdIn(List<Long> ids);

  Docker save(Docker docker);

  Docker findByName(String name);
}
