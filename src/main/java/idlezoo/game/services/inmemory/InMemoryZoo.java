package idlezoo.game.services.inmemory;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.Assert;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.Perks.Perk;
import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.Zoo.Builder;
import idlezoo.game.domain.ZooBuildings;
import idlezoo.game.domain.ZooInfo;
import idlezoo.game.services.FightService.Outcome;
import idlezoo.game.services.ResourcesService;
import one.util.streamex.StreamEx;

public class InMemoryZoo implements ZooInfo {
  private static final Timer DEFAULT_TIMER = () -> java.time.Clock.systemUTC().instant()
      .getEpochSecond();

  private static final AtomicInteger ID_COUNTER = new AtomicInteger();
  private final Integer id = ID_COUNTER.incrementAndGet();
  private final String name;
  private final String password;
  private final Timer timer;
  private final List<InMemoryZooBuildings> buildings;
  private final Map<String, InMemoryZooBuildings> buildingsMap;
  private final List<Perk> perks = new ArrayList<>();
  private double money;
  private long lastMoneyUpdate;
  private int fightWins;
  private int fightLosses;
  private long championTime;
  private Long waitingForFightStart;
  // cached value - trade memory for CPU
  private double baseIncome;
  private double perkIncome;

  public InMemoryZoo(String name, String password, ResourcesService gameResources) {
    this(name, password, gameResources, DEFAULT_TIMER);
  }

  public Zoo toDTO(ResourcesService resourcesService) {
    Builder builder = new Zoo.Builder();
    builder.setName(name);
    builder.setBuildings(StreamEx.of(buildings).map(InMemoryZooBuildings::toDTO).toList());
    builder.setPerks(perks);
    builder.setBaseIncome(baseIncome);
    builder.setMoney(money);
    builder.setFightWins(fightWins);
    builder.setFightLosses(fightLosses);
    builder.setWaitingForFight(waitingForFightStart != null);
    builder.setChampionTime(championTime);

    builder.setAvailablePerks(resourcesService.availablePerks(builder));
    return builder.build();
  }

  InMemoryZoo(String name, String password, ResourcesService gameResources, Timer timer) {
    this.name = name;
    this.password = password;
    buildings = new ArrayList<>(asList(new InMemoryZooBuildings(gameResources.startingAnimal())));
    buildingsMap = new HashMap<>(singletonMap(gameResources.startingAnimal().getName(), buildings
        .get(0)));
    money = gameResources.startingMoney();

    this.timer = timer;
    lastMoneyUpdate = timer.now();
  }

  public void setMoney(double money) {
    this.money = money;
  }

  @Override
  public double getMoney() {
    return money;
  }

  public Integer getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
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
  public List<Perk> getPerks() {
    return perks;
  }

  @Override
  public double getBaseIncome() {
    return baseIncome;
  }
  
  public double getPerkIncome(){
    return perkIncome;
  }
  
  public double getMoneyIncome(){
    return baseIncome + perkIncome;
  }

  @Override
  public ZooBuildings animal(String animal) {
    return buildingsMap.get(animal).toDTO();
  }

  public long getLastMoneyUpdate() {
    return lastMoneyUpdate;
  }

  public void computeIncome() {
    baseIncome = buildings.stream().mapToDouble(InMemoryZooBuildings::getIncome).sum();
    perkIncome = StreamEx.of(perks).mapToDouble(perk -> perk.perkIncome(this)).sum();
  }

  public boolean isWaitingForFight() {
    return waitingForFightStart != null;
  }

  public void startWaitingForFight() {
    Assert.isNull(waitingForFightStart);
    waitingForFightStart = timer.now();
  }

  public void endWaitingForFight() {
    Assert.notNull(waitingForFightStart);
    long now = timer.now();
    championTime += now - waitingForFightStart;
    waitingForFightStart = null;
  }

  @Override
  public long getChampionTime() {
    if (isWaitingForFight()) {
      long now = timer.now();
      return championTime + (now - waitingForFightStart);
    } else {
      return championTime;
    }
  }

  @Override
  public List<ZooBuildings> getBuildings() {
    return StreamEx.of(buildings).map(InMemoryZooBuildings::toDTO).toList();
  }
  
  public List<InMemoryZooBuildings> getInMemoryBuildings() {
    return buildings;
  }
  

  public Map<String, InMemoryZooBuildings> getBuildingsMap() {
    return buildingsMap;
  }

  // Logic
  public synchronized InMemoryZoo updateMoney() {
    long now = timer.now();
    money += (now - lastMoneyUpdate) * baseIncome;
    money += (now - lastMoneyUpdate) * perkIncome;
    lastMoneyUpdate = now;
    return this;
  }

  public synchronized InMemoryZoo buyPerk(String perkName, ResourcesService resourcesService) {
    Perk perk = resourcesService.perk(perkName);
    if (money < perk.getCost()
        || !perk.isAvailable(this)
        || perks.contains(perk)) {
      return this;
    }
    money -= perk.getCost();
    perks.add(perk);
    computeIncome();
    return this;
  }

  public synchronized InMemoryZoo buy(String buildingName, ResourcesService gameResources) {
    updateMoney();
    InMemoryZooBuildings zooBuildings = buildingsMap.get(buildingName);
    if (zooBuildings == null) {
      throw new IllegalStateException("Building " + buildingName + " is not available");
    }
    if (money < zooBuildings.getNextCost()) {
      return this;
    }
    money -= zooBuildings.getNextCost();
    zooBuildings.buy();
    if (zooBuildings.first()) {
      Building next = gameResources.nextType(buildingName);
      if (next != null && !buildingsMap.containsKey(next.getName())) {
        InMemoryZooBuildings nextBuildings = new InMemoryZooBuildings(next);
        buildings.add(nextBuildings);
        buildingsMap.put(next.getName(), nextBuildings);
      }
    }
    computeIncome();
    return this;
  }

  public synchronized InMemoryZoo upgrade(String buildingName) {
    updateMoney();
    InMemoryZooBuildings zooBuildings = buildingsMap.get(buildingName);
    if (zooBuildings == null) {
      throw new IllegalStateException("Upgrading " + buildingName + " is not available");
    }
    if (money < zooBuildings.getUpgradeCost()) {
      return this;
    }

    money -= zooBuildings.getUpgradeCost();
    zooBuildings.upgrade();
    computeIncome();
    return this;
  }

  public synchronized Outcome fight(InMemoryZoo other) {
    synchronized (other) {
      Set<String> buildingsSuperSet = new HashSet<>();
      buildingsSuperSet.addAll(
          StreamEx.of(buildings).filter(b -> b.getNumber() != 0).map(InMemoryZooBuildings::getName)
              .toList());
      buildingsSuperSet.addAll(
          StreamEx.of(other.buildings).filter(b -> b.getNumber() != 0).map(
              InMemoryZooBuildings::getName)
              .toList());
      int thisWins = 0, otherWins = 0;
      for (String building : buildingsSuperSet) {
        InMemoryZooBuildings thisBuildings = buildingsMap.get(building);
        InMemoryZooBuildings otherBuildings = other.buildingsMap.get(building);

        if (thisBuildings == null) {
          otherWins++;
          continue;
        }

        if (otherBuildings == null) {
          thisWins++;
          continue;
        }

        if (thisBuildings.getNumber() >= otherBuildings.getNumber()) {
          thisWins++;
          thisBuildings.setNumber(thisBuildings.getNumber() - otherBuildings.getNumber());
          thisBuildings.lost(otherBuildings.getNumber());
          otherBuildings.lost(otherBuildings.getNumber());
          otherBuildings.setNumber(0);
        } else {
          otherWins++;
          otherBuildings.setNumber(otherBuildings.getNumber() - thisBuildings.getNumber());
          thisBuildings.lost(thisBuildings.getNumber());
          otherBuildings.lost(thisBuildings.getNumber());
          thisBuildings.setNumber(0);
        }
      }
      computeIncome();
      other.computeIncome();
      if (thisWins >= otherWins) {
        fightWins++;
        other.fightLosses++;
        return Outcome.LOSS;
      } else {
        other.fightWins++;
        fightLosses++;
        return Outcome.WIN;
      }
    }
  }

  interface Timer {
    long now();
  }

}