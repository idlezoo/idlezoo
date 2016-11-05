package idlezoo.game.services;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import idlezoo.game.domain.Zoo;

@Service
public class GameService {

  @Autowired
  private ResourcesService gameResources;

  private final ConcurrentHashMap<String, Zoo> zoos = new ConcurrentHashMap<>();

  public boolean createZoo(String username) {
    return null == zoos.put(username, new Zoo(username, gameResources));
  }

  public Zoo getZoo(String username) {
    return zoos.get(username);
  }

  public ConcurrentHashMap<String, Zoo> getZoos() {
    return zoos;
  }

}
