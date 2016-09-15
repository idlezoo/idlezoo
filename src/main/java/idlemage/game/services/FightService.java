package idlemage.game.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import idlemage.game.domain.Mage;

@Service
public class FightService {

  @Autowired
  private GameService gameService;

  private String waitingFighter;

  public synchronized Mage fight(String username) {
    if (username.equals(waitingFighter)) {
      return null;
    }
    
    if (waitingFighter == null) {
      waitingFighter = username;
      gameService.getMage(waitingFighter).startWaitingForFight();
      return null;
    }
    Mage waiting = gameService.getMage(waitingFighter);
    Mage fighter = gameService.getMage(username);

    waiting.fight(fighter);
    waiting.endWaitingForFight();
    waitingFighter = null;
    return waiting;
  }

}
