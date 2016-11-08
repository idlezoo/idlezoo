package idlezoo.game.domain;

public class ZooBuildings {
  private final String name;
  private final Building building;
  private final int level;
  private final int number;

  public ZooBuildings(Building building, int level, int number) {
    this.name = building.getName();
    this.building = building;
    this.level = level;
    this.number = number;
  }

  // Getters for JSON

  public String getName() {
    return name;
  }

  public double getIncome() {
    return building.income(level) * number;
  }

  public double getNextCost() {
    return building.buildCost(number);
  }

  public double getUpgradeCost() {
    return building.upgradeCost(level);
  }

  public int getLevel() {
    return level;
  }

  public int getNumber() {
    return number;
  }

  public boolean first() {
    return number == 1;
  }

  public Building getBuilding() {
    return building;
  }
}