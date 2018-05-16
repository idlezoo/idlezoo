package idlezoo.security;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Service
@Transactional
public class UsersService implements UserDetailsService {
    private final JdbcTemplate template;
    private final PasswordEncoder passwordEncoder;

    public UsersService(JdbcTemplate template, PasswordEncoder passwordEncoder) {
        this.template = template;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return template.queryForObject("select id, username, password from users where lower(username)=lower(?)",
                    USER_ROW_MAPPER, username);
        } catch (EmptyResultDataAccessException empty) {
            throw new UsernameNotFoundException("User " + username + " not found");
        }
    }

    private static final RowMapper<UserDetails> USER_ROW_MAPPER =
            (res, rowNum) ->
                    new IdUser(res.getInt("id"),
                            res.getString("username"),
                            res.getString("password")
                    );

    public boolean addUser(String username, String password) {
        try {
            final KeyHolder idHolder = new GeneratedKeyHolder();
            template.update(con -> {
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
