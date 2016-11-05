package idlezoo.game.services;

import idlezoo.game.domain.ZooDTO;

public interface GameService {
  
  boolean createZoo(String username);  
  
  ZooDTO getZoo(String name);

  ZooDTO buy(String name, String animal);

  ZooDTO upgrade(String name, String animal);
}
