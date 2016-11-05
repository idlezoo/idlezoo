package idlezoo.game.domain;

import java.util.List;

public class Zoo {

  private final String name;
  private final List<ZooBuildings> buildings;
  private final double income;
  private final double money;
  private final int fightWins;
  private final boolean waitingForFight;
  private final long championTime;

  public Zoo(String name, List<ZooBuildings> buildings, double income, double money,
      int fightWins,
      boolean waitingForFight, long championTime) {
    this.name = name;
    this.buildings = buildings;
    this.income = income;
    this.money = money;
    this.fightWins = fightWins;
    this.waitingForFight = waitingForFight;
    this.championTime = championTime;
  }

  public String getName() {
    return name;
  }

  public double getIncome() {
    return income;
  }

  public double getMoney() {
    return money;
  }

  public double getMoneyIncome() {
    return income;
  }

  public List<ZooBuildings> getBuildings() {
    return buildings;
  }

  public int getFightWins() {
    return fightWins;
  }

  public boolean isWaitingForFight() {
    return waitingForFight;
  }

  public long getChampionTime() {
    return championTime;
  }
}