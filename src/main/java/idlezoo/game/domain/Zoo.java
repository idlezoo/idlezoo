package idlezoo.game.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idlezoo.game.domain.Perks.Perk;
import one.util.streamex.StreamEx;

public final class Zoo implements ZooInfo {

  private final String name;
  private final List<ZooBuildings> buildings;
  private final Map<String, ZooBuildings> buildingsMap;
  private final List<Perk> perks;
  private final List<Perk> availablePerks;
  private final double baseIncome;
  private final double perkIncome;
  private final double money;
  private final int fightWins;
  private final int fightLosses;
  private final boolean waitingForFight;
  private final long championTime;

  public Zoo(String name, List<ZooBuildings> buildings,
      List<Perk> perks, List<Perk> availablePerks,
      double baseIncome, double money,
      int fightWins, int fightLosses,
      boolean waitingForFight, long championTime) {
    assert baseIncome == buildings.stream().mapToDouble(ZooBuildings::getIncome).sum();

    this.name = name;
    this.buildings = buildings;
    this.buildingsMap = buildingsMap(buildings);
    this.baseIncome = baseIncome;
    this.perks = perks;
    this.availablePerks = availablePerks;
    this.money = money;
    this.fightWins = fightWins;
    this.fightLosses = fightLosses;
    this.waitingForFight = waitingForFight;
    this.championTime = championTime;
    // should be last
    this.perkIncome = StreamEx.of(perks).mapToDouble(perk -> perk.perkIncome(this)).sum();
  }

  private static Map<String, ZooBuildings> buildingsMap(List<ZooBuildings> buildings) {
    Map<String, ZooBuildings> result = new HashMap<>();
    for (ZooBuildings building : buildings) {
      result.put(building.getName(), building);
    }
    return Collections.unmodifiableMap(result);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public double getMoney() {
    return money;
  }

  @Override
  public double getBaseIncome() {
    return baseIncome;
  }

  public double getMoneyIncome() {
    return baseIncome + perkIncome;
  }

  public List<ZooBuildings> getBuildings() {
    return buildings;
  }

  @Override
  public List<Perk> getPerks() {
    return perks;
  }

  public List<Perk> getAvailablePerks() {
    return availablePerks;
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
    return buildingsMap.get(animal);
  }

  public static final class Builder implements ZooInfo {
    private String name;
    private List<ZooBuildings> buildings;
    private Map<String, ZooBuildings> buildingsMap;
    private List<Perk> perks;
    private List<Perk> availablePerks;
    private double baseIncome;
    private double money;
    private int fightWins;
    private int fightLosses;
    private boolean waitingForFight;
    private long championTime;

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setBuildings(List<ZooBuildings> buildings) {
      this.buildings = buildings;
      this.buildingsMap = buildingsMap(buildings);
      return this;
    }

    public Builder setBaseIncome(double baseIncome) {
      this.baseIncome = baseIncome;
      return this;
    }

    public Builder setMoney(double money) {
      this.money = money;
      return this;
    }

    public Builder setFightWins(int fightWins) {
      this.fightWins = fightWins;
      return this;
    }

    public Builder setFightLosses(int fightLosses) {
      this.fightLosses = fightLosses;
      return this;
    }

    public Builder setWaitingForFight(boolean waitingForFight) {
      this.waitingForFight = waitingForFight;
      return this;
    }

    public Builder setChampionTime(long championTime) {
      this.championTime = championTime;
      return this;
    }

    public Builder setPerks(List<Perk> perks) {
      this.perks = perks;
      return this;
    }

    public Builder setAvailablePerks(List<Perk> availablePerks) {
      this.availablePerks = availablePerks;
      return this;
    }

    public Zoo build() {
      return new Zoo(name, buildings,
          perks, availablePerks,
          baseIncome, money,
          fightWins, fightLosses, waitingForFight, championTime);
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
    public double getMoney() {
      return money;
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
      return buildingsMap.get(animal);
    }
    
    

  }

}