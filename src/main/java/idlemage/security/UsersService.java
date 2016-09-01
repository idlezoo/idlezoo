package idlemage.security;

import static java.util.Collections.emptyList;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersService implements UserDetailsService, InitializingBean {
	private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return users.get(username);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO - remove this shit
		users.put("user", new User("user", passwordEncoder.encode("foobar"), emptyList()));
	}

	public void addUser(String username, String password) {
		users.put(username, new User(username, passwordEncoder.encode(password), emptyList()));
	}

}
