package idlezoo.security.postgres;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.security.IdUser;
import idlezoo.security.UsersService;

@Service
@Transactional
@Profile("postgres")
public class UsersServicePostgres implements UsersService {
	@Autowired
	private JdbcTemplate template;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			return template.queryForObject("select id, username, password from users where lower(username)=lower(?)",
					USER_ROW_MAPPER, username);
		} catch (EmptyResultDataAccessException empty) {
			throw new UsernameNotFoundException("User " + username + " not found");
		}
	}

	private static final RowMapper<UserDetails> USER_ROW_MAPPER = (res, rowNum) -> {
		return new IdUser(res.getInt("id"), res.getString("username"), res.getString("password"));
	};

	@Override
	public boolean addUser(String username, String password) {
		try {
			final KeyHolder idHolder = new GeneratedKeyHolder();
			template.update((PreparedStatementCreator) con -> {
				PreparedStatement pst = con.prepareStatement("insert into users(username, password) values(?,?) returning id", Statement.RETURN_GENERATED_KEYS);
				pst.setString(1, username);
				pst.setString(2, passwordEncoder.encode(password));
				return pst;
			}, idHolder);
			template.update("insert into animal(user_id, animal_type) values(?,?)",
					idHolder.getKey().intValue(), 0);
			return true;
		} catch (DuplicateKeyException duplicate) {
			return false;
		}
	}

}
