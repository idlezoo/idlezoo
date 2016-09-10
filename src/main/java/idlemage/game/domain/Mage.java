package idlemage.game.domain;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import idlemage.game.services.ResourcesService;

public class Mage {
  private static final Timer DEFAULT_TIMER = () -> LocalDateTime.now(ZoneOffset.UTC);

  private final Timer timer;
  private final List<MageBuildings> buildings;
  private final Map<String, MageBuildings> buildingsMap;
  private double mana;
  private double income;
  private LocalDateTime lastManaUpdate;

  public Mage(ResourcesService gameResources) {
    this(gameResources, DEFAULT_TIMER);
  }

  Mage(ResourcesService gameResources, Timer timer) {
    buildings = new ArrayList<>(asList(new MageBuildings(gameResources.startingCreature())));
    buildingsMap = new HashMap<>(singletonMap(gameResources.startingCreature().getName(), buildings
        .get(0)));
    mana = gameResources.startingMana();

    this.timer = timer;
    lastManaUpdate = timer.now();
  }

  public double getMana() {
    return mana;
  }

  public double getIncome(){
    return income;
  }
  
  private double computeIncome() {
    return buildings.stream().mapToDouble(MageBuildings::getIncome).sum();
  }

  public List<MageBuildings> getBuildings() {
    return buildings;
  }

  public Map<String, MageBuildings> getBuildingsMap() {
    return buildingsMap;
  }

  // Logic
  public synchronized Mage updateMana() {
    LocalDateTime now = timer.now();
    long dif = Duration.between(lastManaUpdate, now).getSeconds();
    mana += dif * getIncome();
    lastManaUpdate = now;
    return this;
  }

  public synchronized Mage buy(String buildingName, ResourcesService gameResources) {
    updateMana();
    MageBuildings mageBuildings = buildingsMap.get(buildingName);
    if (mageBuildings == null) {
      throw new IllegalStateException("Building " + buildingName + " is not available");
    }
    if (mana < mageBuildings.getNextCost()) {
      throw new InsuffisientFundsException();
    }
    mana -= mageBuildings.getNextCost();
    mageBuildings.buy();
    if (mageBuildings.first()) {
      Building next = gameResources.nextType(buildingName);
      if (next != null) {
        MageBuildings nextBuildings = new MageBuildings(next);
        buildings.add(nextBuildings);
        buildingsMap.put(next.getName(), nextBuildings);
      }
    }
    income = computeIncome();
    return this;
  }

  public synchronized Mage upgrade(String buildingName) {
    updateMana();
    MageBuildings mageBuildings = buildingsMap.get(buildingName);
    if (mageBuildings == null) {
      throw new IllegalStateException("Upgrading " + buildingName + " is not available");
    }
    if (mana < mageBuildings.getUpgradeCost()) {
      throw new InsuffisientFundsException();
    }

    mana -= mageBuildings.getUpgradeCost();
    mageBuildings.upgrade();
    income = computeIncome();
    return this;
  }

  interface Timer {
    LocalDateTime now();
  }

  public static class InsuffisientFundsException extends RuntimeException {
  }

}