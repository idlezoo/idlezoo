package idlezoo.game.services;

import idlezoo.game.domain.Zoo;

public interface GameService {
  
  Zoo getZoo(String name);

  Zoo buy(String name, String animal);

  Zoo upgrade(String name, String animal);
}
