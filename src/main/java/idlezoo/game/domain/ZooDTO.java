package idlezoo.game.domain;

import java.util.List;

public class ZooDTO {

    private final List<ZooBuildings> buildings;
    private final double income;
    private final double money;
    private final int fightWins;
    private final boolean waitingForFight;
    private final long championTime;

    public ZooDTO(Zoo zoo) {
      this.buildings = zoo.getBuildings();
      this.income = zoo.getIncome();
      this.money = zoo.getMoney();
      this.fightWins = zoo.getFightWins();
      this.waitingForFight = zoo.isWaitingForFight();
      this.championTime = zoo.getChampionTime();
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