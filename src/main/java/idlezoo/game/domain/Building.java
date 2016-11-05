package idlezoo.game.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Building {
  private final String name;
  private final double baseCost;
  private final double baseIncome;
  private final double baseUpgrade;

  @JsonCreator
  public Building(@JsonProperty("name") String name,
      @JsonProperty("baseCost") double baseCost,
      @JsonProperty("baseIncome") double baseIncome,
      @JsonProperty("baseUpgrade") double baseUpgrade) {
    this.name = name;
    this.baseCost = baseCost;
    this.baseIncome = baseIncome;
    this.baseUpgrade = baseUpgrade;
  }

  // logic start
  public double income(int level) {
    return baseIncome + baseIncome * level * level;
  }

  public double upgradeCost(int level) {
    return baseUpgrade * Math.pow(2, level);
  }

  public double buildCost(int index) {
    // For now taken from Coockie-clicker
    return baseCost * Math.pow(1.15, index);
  }
  // logic end

  public double getBaseCost() {
    return baseCost;
  }

  public double getBaseIncome() {
    return baseIncome;
  }

  public double getBaseUpgrade() {
    return baseUpgrade;
  }

  public String getName() {
    return name;
  }

}