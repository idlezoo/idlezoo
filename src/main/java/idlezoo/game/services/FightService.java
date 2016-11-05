package idlezoo.game.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import idlezoo.game.domain.Zoo;

@Service
public class FightService {

  @Autowired
  private GameService gameService;

  private String waitingFighter;

  public synchronized Zoo fight(String username) {
    if (username.equals(waitingFighter)) {
      return null;
    }
    
    if (waitingFighter == null) {
      waitingFighter = username;
      gameService.getZoo(waitingFighter).startWaitingForFight();
      return null;
    }
    Zoo waiting = gameService.getZoo(waitingFighter);
    Zoo fighter = gameService.getZoo(username);

    waiting.fight(fighter);
    waiting.endWaitingForFight();
    waitingFighter = null;
    return waiting;
  }

}
