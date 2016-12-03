package idlezoo.game.services;

import idlezoo.game.domain.Zoo;

public interface GameService {
  
  Zoo getZoo(Integer userId);

  Zoo buy(Integer userId, String animal);

  Zoo upgrade(Integer userId, String animal);
  
  Zoo buyPerk(Integer userId, String perk);
}
