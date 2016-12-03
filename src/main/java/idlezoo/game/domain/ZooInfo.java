package idlezoo.game.domain;

import java.util.List;

import idlezoo.game.domain.Perks.Perk;

public interface ZooInfo {

  String getName();

  List<ZooBuildings> getBuildings();

  List<Perk> getPerks();

  int getFightWins();

  int getFightLosses();

  boolean isWaitingForFight();

  long getChampionTime();

  double getMoney();

  double getBaseIncome();

  ZooBuildings animal(String animal);

}
