package idlezoo.game.services.inmemory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;

@Service
@Profile("default")
public class GameServiceInMemory implements GameService {

  private final Storage storage;
  private final ResourcesService resources;

  public GameServiceInMemory(Storage storage, ResourcesService resources) {
    this.storage = storage;
    this.resources=resources;
  }

  @Override
  public Zoo getZoo(String name) {
    return storage.getZoo(name).updateMoney().toDTO();
  }

  @Override
  public Zoo buy(String name, String animal) {
    return storage.getZoo(name).buy(animal, resources).updateMoney().toDTO();
  }

  @Override
  public Zoo upgrade(String name, String animal) {
    return storage.getZoo(name).upgrade(animal).updateMoney().toDTO();
  }

  @Override
  public boolean createZoo(String username) {
    return null == storage.getZoos().put(username, new InMemoryZoo(username, resources));
  }

}
