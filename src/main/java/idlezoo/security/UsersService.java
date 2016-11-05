package idlezoo.security;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsersService extends UserDetailsService {

  boolean userExists(String username);

  boolean addUser(String username, String password);

}
