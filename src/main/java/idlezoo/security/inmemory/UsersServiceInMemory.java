package idlezoo.security.inmemory;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import idlezoo.security.UsersService;

@Service
@Profile("default")
public class UsersServiceInMemory implements UsersService {
  private final ConcurrentHashMap<String, ZooUser> users = new ConcurrentHashMap<>();

  private final PasswordEncoder passwordEncoder;

  public UsersServiceInMemory(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    ZooUser user = users.get(username);
    if (user == null) {
      throw new UsernameNotFoundException("User " + username + " not found");
    }
    return new User(user.name, user.password, Collections.emptyList());
  }

  public boolean addUser(String username, String password) {
    return null == users.putIfAbsent(username,
        new ZooUser(username, passwordEncoder.encode(password)));
  }

  private static final class ZooUser {
    private final String name;
    private final String password;

    public ZooUser(String name, String password) {
      this.name = name;
      this.password = password;

    }
  }

}
