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
public class UsersService implements UserDetailsService {
	private final ConcurrentHashMap<String, MageUser> users = new ConcurrentHashMap<>();

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MageUser user = users.get(username);
		if (user == null) {
			throw new UsernameNotFoundException("User " + username + " not found");
		}
		return new User(user.name, user.password, emptyList());
	}

	public MageUser getUser(String username) {
		return users.get(username);
	}

	public boolean addUser(String username, String password) {
		return null == users.putIfAbsent(username, new MageUser(username, passwordEncoder.encode(password)));
	}

	public static final class MageUser {
		private final String name;
		private final String password;

		public MageUser(String name, String password) {
			this.name = name;
			this.password = password;
		}

		public String getName() {
			return name;
		}

		public String getPassword() {
			return password;
		}
	}

}
