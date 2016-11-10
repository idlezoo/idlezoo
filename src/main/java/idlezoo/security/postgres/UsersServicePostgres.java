package idlezoo.security.postgres;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.services.ResourcesService;
import idlezoo.security.UsersService;

@Service
@Transactional
@Profile("postgres")
public class UsersServicePostgres implements UsersService {
	@Autowired
	private JdbcTemplate template;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private ResourcesService gameResources;

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
		try {
			template.update("insert into users(username, password) values(?,?)",
					username, passwordEncoder.encode(password));
			template.update("insert into animal(username, animal_type) values(?,?)",
					username, gameResources.startingAnimal().getName());
			return true;
		} catch (DuplicateKeyException duplicate) {
			return false;
		}
	}

}
