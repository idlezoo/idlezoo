package idlezoo.security.inmemory;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import idlezoo.game.services.ResourcesService;
import idlezoo.game.services.inmemory.InMemoryZoo;
import idlezoo.game.services.inmemory.Storage;
import idlezoo.security.UsersService;

@Service
@Profile("default")
public class UsersServiceInMemory implements UsersService {
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private ResourcesService resources;
	@Autowired
	private Storage storage;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		InMemoryZoo zoo = storage.getZoo(username);
		if (zoo == null) {
			throw new UsernameNotFoundException("User " + username + " not found");
		}
		return new User(zoo.getName(), zoo.getPassword(), Collections.emptyList());
	}

	public boolean addUser(String username, String password) {
		return null == storage.getZoos().put(username,
				new InMemoryZoo(username, passwordEncoder.encode(password), resources));
	}

}
