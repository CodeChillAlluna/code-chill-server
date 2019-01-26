package fr.codechill.spring.repository;

import fr.codechill.spring.model.Image;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ImageRepository extends CrudRepository<Image, Long> {
  List<Image> findAll();

  Image findOne(long id);

  Image save(Image image);

  Image findByName(String name);
}
