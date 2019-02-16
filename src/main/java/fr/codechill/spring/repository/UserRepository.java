package fr.codechill.spring.repository;

import fr.codechill.spring.model.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Created by stephan on 20.03.16. */
public interface UserRepository extends JpaRepository<User, Long> {

  User findByUsername(String username);

  User findOne(long id);

  List<User> findByIdIn(List<Long> ids);

  User save(User user);

  List<User> findAll();

  void delete(User user);

  User findByTokenPassword(String tokenPassword);

  User findByEmail(String email);

  List<User> findByEnabled(Boolean enabled);
}
