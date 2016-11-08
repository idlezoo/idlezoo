package idlezoo.security.postgres;

import java.util.Collections;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.security.UsersService;

@Service
@Transactional
@Profile("postgres")
public class UsersServicePostgres implements UsersService {

  private final JdbcTemplate template;

  public UsersServicePostgres(JdbcTemplate template) {
    this.template = template;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
      return template.queryForObject("select username, password from users where username=?",
          USER_ROW_MAPPER, username);
    } catch (EmptyResultDataAccessException empty) {
      throw new UsernameNotFoundException("User " + username + " not found");
    }
  }

  private static final RowMapper<UserDetails> USER_ROW_MAPPER = (res, rowNum) -> {
    return new User(res.getString("username"), res.getString("password"), Collections.emptyList());
  };

  @Override
  public boolean addUser(String username, String password) {
    template.update("insert into users(username, password) values(?,?)", username, password);
    return true;
  }

}
