package idlezoo.game.services.inmemory;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
public class Storage {

  private final ConcurrentHashMap<String, Zoo> zoos = new ConcurrentHashMap<>();

  public ConcurrentHashMap<String, Zoo> getZoos() {
    return zoos;
  }

  public Zoo getZoo(String name) {
    return zoos.get(name);
  }

}
