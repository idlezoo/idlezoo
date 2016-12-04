package idlezoo.game.domain;

import java.util.List;

import idlezoo.game.domain.Perks.Perk;

public interface ZooInfo {

  String getName();

  double getMoneyIncome();

  double getPerkIncome();

  List<ZooBuildings> getBuildings();

  List<Perk> getPerks();

  int getFightWins();

  int getFightLosses();

  boolean isWaitingForFight();

  long getChampionTime();

  double getBaseIncome();

  ZooBuildings animal(String animal);

  public static class ZooInfoSimple implements ZooInfo {
    private final String name;
    private final List<ZooBuildings> buildings;
    private final List<Perk> perks;
    private final double baseIncome;
    private final double perkIncome;
    private final int fightWins;
    private final int fightLosses;
    private final boolean waitingForFight;
    private final long championTime;

    public ZooInfoSimple(String name, List<ZooBuildings> buildings, List<Perk> perks,
        double baseIncome, double perkIncome, int fightWins, int fightLosses,
        boolean waitingForFight, long championTime) {
      this.name = name;
      this.buildings = buildings;
      this.perks = perks;
      this.baseIncome = baseIncome;
      this.perkIncome = perkIncome;
      this.fightWins = fightWins;
      this.fightLosses = fightLosses;
      this.waitingForFight = waitingForFight;
      this.championTime = championTime;
    }

    @Override
    public String getName() {
      return name;
    }
    @Override
    public List<ZooBuildings> getBuildings() {
      return buildings;
    }
    @Override
    public List<Perk> getPerks() {
      return perks;
    }
    @Override
    public double getBaseIncome() {
      return baseIncome;
    }
    @Override
    public double getPerkIncome() {
      return perkIncome;
    }
    @Override
    public double getMoneyIncome(){
      return baseIncome + perkIncome;
    }
    @Override
    public int getFightWins() {
      return fightWins;
    }
    @Override
    public int getFightLosses() {
      return fightLosses;
    }
    @Override
    public boolean isWaitingForFight() {
      return waitingForFight;
    }
    @Override
    public long getChampionTime() {
      return championTime;
    }

    @Override
    public ZooBuildings animal(String animal) {
      throw new IllegalStateException("Not implemented");
    }
  }

}
