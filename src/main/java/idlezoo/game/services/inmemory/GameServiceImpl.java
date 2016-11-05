package idlezoo.game.services.inmemory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import idlezoo.game.domain.ZooDTO;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;

@Service
@Profile("default")
public class GameServiceImpl implements GameService {

  private final Storage storage;
  private final ResourcesService resources;

  public GameServiceImpl(Storage storage, ResourcesService resources) {
    this.storage = storage;
    this.resources=resources;
  }

  @Override
  public ZooDTO getZoo(String name) {
    return storage.getZoo(name).updateMoney().toDTO();
  }

  @Override
  public ZooDTO buy(String name, String animal) {
    return storage.getZoo(name).buy(animal, resources).updateMoney().toDTO();
  }

  @Override
  public ZooDTO upgrade(String name, String animal) {
    return storage.getZoo(name).upgrade(animal).updateMoney().toDTO();
  }

  @Override
  public boolean createZoo(String username) {
    return null == storage.getZoos().put(username, new Zoo(username, resources));
  }

}
