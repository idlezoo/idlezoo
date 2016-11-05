package idlezoo.game.services.inmemory;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
public class Storage {

  private final ConcurrentHashMap<String, InMemoryZoo> zoos = new ConcurrentHashMap<>();

  public ConcurrentHashMap<String, InMemoryZoo> getZoos() {
    return zoos;
  }

  public InMemoryZoo getZoo(String name) {
    return zoos.get(name);
  }

}
