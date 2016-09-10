package idlemage.game.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import idlemage.game.domain.Mage;

@Service
public class FightService {

  @Autowired
  private GameService gameService;

  private String waitingFighter;

  public synchronized void fight(String username) {
    if (username.equals(waitingFighter)) {
      return;
    }
    if (waitingFighter == null) {
      waitingFighter = username;
      return;
    }
    Mage waiting = gameService.getMage(waitingFighter);
    Mage fighter = gameService.getMage(username);

    waiting.fight(fighter);
    waitingFighter = null;
  }

}
