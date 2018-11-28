package fr.codechill.spring.repository;

import fr.codechill.spring.model.security.Authority;
import fr.codechill.spring.model.security.AuthorityName;
import org.springframework.data.repository.CrudRepository;

public interface AuthorityRepository extends CrudRepository<Authority, Long> {
  Authority findOne(long id);

  Authority save(Authority authority);

  Authority findByName(AuthorityName name);
}
