package fr.codechill.spring.repository;

import org.springframework.data.repository.CrudRepository;

import fr.codechill.spring.model.security.Authority;
import fr.codechill.spring.model.security.AuthorityName;

public interface AuthorityRepository extends CrudRepository<Authority, Long>{
	Authority findOne(long id);
	Authority save(Authority authority);
	Authority findByName(AuthorityName name);
}