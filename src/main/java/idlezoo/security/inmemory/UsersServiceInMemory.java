package idlezoo.security.inmemory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import idlezoo.game.services.ResourcesService;
import idlezoo.game.services.inmemory.InMemoryZoo;
import idlezoo.game.services.inmemory.Storage;
import idlezoo.security.IdUser;
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
		return new IdUser(zoo.getId(), zoo.getName(), zoo.getPassword());
	}

	public boolean addUser(String username, String password) {
		InMemoryZoo newZoo = new InMemoryZoo(username, passwordEncoder.encode(password), resources);
		InMemoryZoo oldZoo = storage.getZoos().put(username, newZoo);
		if (null == oldZoo) {
			storage.getZooIds().put(newZoo.getId(), newZoo);
			return true;
		} else {
			return false;
		}
	}

}
