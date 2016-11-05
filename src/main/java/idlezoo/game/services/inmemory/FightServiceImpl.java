package idlezoo.game.services.inmemory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService;

@Service
@Profile("default")
public class FightServiceImpl implements FightService {

  private final Storage storage;

  private String waitingFighter;
  
  public FightServiceImpl(Storage storage) {
    super();
    this.storage = storage;
  }

  @Override
  public synchronized Zoo fight(String username) {
    if (username.equals(waitingFighter)) {
      return null;
    }
    
    if (waitingFighter == null) {
      waitingFighter = username;
      storage.getZoo(waitingFighter).startWaitingForFight();
      return null;
    }
    InMemoryZoo waiting = storage.getZoo(waitingFighter);
    InMemoryZoo fighter = storage.getZoo(username);

    waiting.fight(fighter);
    waiting.endWaitingForFight();
    waitingFighter = null;
    return waiting.updateMoney().toDTO();
  }

}
