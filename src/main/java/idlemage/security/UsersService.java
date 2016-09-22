package idlemage.security;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersService implements UserDetailsService, InitializingBean {
  private final ConcurrentHashMap<String, MageUser> users = new ConcurrentHashMap<>();

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    MageUser user = users.get(username);
    if (user == null) {
      throw new UsernameNotFoundException("User " + username + " not found");
    }
    return new User(user.name, user.password, user.getAuthorities());
  }

  public MageUser getUser(String username) {
    return users.get(username);
  }

  public boolean addUser(String username, String password) {
    return null == users.putIfAbsent(username, new MageUser(username, passwordEncoder.encode(
        password)));
  }

  public static final class MageUser {
    private final String name;
    private final String password;
    private final List<String> groups;

    public MageUser(String name, String password) {
      this(name, password, Collections.emptyList());
    }

    public MageUser(String name, String password, List<String> groups) {
      this.name = name;
      this.password = password;
      this.groups = groups;
    }

    public String getName() {
      return name;
    }

    public String getPassword() {
      return password;
    }

    public List<String> getGroups() {
      return groups;
    }

    public List<GrantedAuthority> getAuthorities() {
      if (groups.isEmpty()) {
        return Collections.emptyList();
      } else {
        return groups.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
      }
    }

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // just to be safe - this is not my usual password))
    users.put("admin", new MageUser("admin",
        "$2a$10$zvgMiULuB5SW7zMVzd0Jae6h7NC53oll50lbG6hs5rjWT4bGQVPBO", Collections.singletonList(
            "ADMIN")));
  }

}
