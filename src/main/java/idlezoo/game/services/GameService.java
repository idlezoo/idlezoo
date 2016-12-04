package idlezoo.game.services;

import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.ZooInfo;

public interface GameService {
  
  Zoo getZoo(Integer userId);
  
  ZooInfo showZoo(String userName);

  Zoo buy(Integer userId, String animal);

  Zoo upgrade(Integer userId, String animal);
  
  Zoo buyPerk(Integer userId, String perk);
}
