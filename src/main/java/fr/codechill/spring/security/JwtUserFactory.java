package fr.codechill.spring.security;

import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class JwtUserFactory {

  private JwtUserFactory() {}

  public static JwtUser create(User user) {
    return new JwtUser(
        user.getId(),
        user.getUsername(),
        user.getFirstname(),
        user.getLastname(),
        user.getPassword(),
        user.getEmail(),
        mapToGrantedAuthorities(user.getAuthorities()),
        user.getDockers(),
        user.getEnabled(),
        user.getLastPasswordResetDate());
  }

  private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
    return authorities
        .stream()
        .map(authority -> new SimpleGrantedAuthority(authority.getName().name()))
        .collect(Collectors.toList());
  }
}
