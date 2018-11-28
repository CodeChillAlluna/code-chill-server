package fr.codechill.spring.repository;

import fr.codechill.spring.model.Language;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface LanguageRepository extends CrudRepository<Language, Long> {
  List<Language> findAll();
}
