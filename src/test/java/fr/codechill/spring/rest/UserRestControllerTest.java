package fr.codechill.spring.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.codechill.spring.model.User;
import fr.codechill.spring.model.security.Authority;
import fr.codechill.spring.model.security.AuthorityName;
import fr.codechill.spring.security.JwtTokenUtil;
import fr.codechill.spring.security.JwtUser;
import fr.codechill.spring.security.JwtUserFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRestControllerTest {

  private MockMvc mvc;

  @Autowired private WebApplicationContext context;

  @MockBean private JwtTokenUtil jwtTokenUtil;

  @MockBean private UserDetailsService userDetailsService;

  @Before
  public void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  public void shouldGetUnauthorizedWithoutRole() throws Exception {

    this.mvc.perform(get("/user")).andExpect(status().isUnauthorized());
  }

  @Test
  public void testGetProfileWrongToken() throws Exception {
    this.mvc
        .perform(
            get("/user")
                .header("Authorization", "Bearer cebiebobvezfz")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testGetProfileWrongToken2() throws Exception {
    this.mvc
        .perform(
            get("/user")
                .header(
                    "Authorization",
                    "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJMdWx1MzAwIiwiYXVkIjoid2ViIiwiZXhwIjoxNTQyNDczNzQ0LCJpYXQiOjE1NDE4Njg5NDR9.McH8Rla0RU5z1V-OPYGpriPVZ_Xne0x8IbmBeSngjR5Wyd6pZuVeb5UvofH8XN8PgphGfJHG3bB1jVCgv4Zr_Q")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(roles = "USER")
  public void getPersonsSuccessfullyWithUserRole() throws Exception {

    Authority authority = new Authority();
    authority.setId(1L);
    authority.setName(AuthorityName.ROLE_ADMIN);
    List<Authority> authorities = Arrays.asList(authority);

    User user = new User();
    user.setUsername("username");
    user.setAuthorities(authorities);
    user.setEnabled(Boolean.TRUE);
    user.setLastPasswordResetDate(new Date(System.currentTimeMillis() + 1000 * 1000));

    JwtUser jwtUser = JwtUserFactory.create(user);

    when(this.jwtTokenUtil.getUsernameFromToken(any())).thenReturn(user.getUsername());

    when(this.userDetailsService.loadUserByUsername(eq(user.getUsername()))).thenReturn(jwtUser);

    this.mvc
        .perform(get("/user").header("Authorization", "Bearer nsodunsodiuv"))
        .andExpect(status().is2xxSuccessful());
  }
}
