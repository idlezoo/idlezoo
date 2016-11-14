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
  public Zoo getZoo(Integer userId) {
    return storage.getZoo(userId).updateMoney().toDTO();
  }

  @Override
  public Zoo buy(Integer userId, String animal) {
    return storage.getZoo(userId).buy(animal, resources).updateMoney().toDTO();
  }

  @Override
  public Zoo upgrade(Integer userId, String animal) {
    return storage.getZoo(userId).upgrade(animal).updateMoney().toDTO();
  }

}
