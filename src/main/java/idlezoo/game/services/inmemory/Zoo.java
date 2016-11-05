package idlezoo.game.services.inmemory;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.ZooBuildings;
import idlezoo.game.domain.ZooDTO;
import idlezoo.game.services.ResourcesService;
import one.util.streamex.StreamEx;

public class Zoo {
	private static final Timer DEFAULT_TIMER = () -> java.time.Clock.systemUTC().instant().getEpochSecond();

	private final String name;
	private final Timer timer;
	private final List<ZooBuildings> buildings;
	private final Map<String, ZooBuildings> buildingsMap;
	private double money;
	private long lastMoneyUpdate;
	private int fightWins;
	private long championTime;
	private Long waitingForFightStart;
	// private boolean waitingForFight;
	// cached value - trade memory for CPU
	private double income;

	public Zoo(String name, ResourcesService gameResources) {
		this(name, gameResources, DEFAULT_TIMER);
	}
	
	public ZooDTO toDTO(){
	  return new ZooDTO(name, buildings, income, money, fightWins, waitingForFightStart != null, championTime);
	}
	

	Zoo(String name, ResourcesService gameResources, Timer timer) {
		this.name = name;
		buildings = new ArrayList<>(asList(new ZooBuildings(gameResources.startingAnimal())));
		buildingsMap = new HashMap<>(singletonMap(gameResources.startingAnimal().getName(), buildings
				.get(0)));
		money = gameResources.startingMoney();

		this.timer = timer;
		lastMoneyUpdate = timer.now();
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public double getMoney() {
		return money;
	}

	public String getName() {
		return name;
	}

	public double getIncome() {
		return income;
	}

	public int getFightWins() {
		return fightWins;
	}

	public long getLastMoneyUpdate() {
		return lastMoneyUpdate;
	}

	private void computeIncome() {
		income = buildings.stream().mapToDouble(ZooBuildings::getIncome).sum();
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

	public long getChampionTime() {
		if (isWaitingForFight()) {
			long now = timer.now();
			return championTime + (now - waitingForFightStart);
		} else {
			return championTime;
		}
	}

	public List<ZooBuildings> getBuildings() {
		return buildings;
	}

	public Map<String, ZooBuildings> getBuildingsMap() {
		return buildingsMap;
	}

	// Logic
	public synchronized Zoo updateMoney() {
		long now = timer.now();
		money += (now - lastMoneyUpdate) * income;
		lastMoneyUpdate = now;
		return this;
	}

	public synchronized Zoo buy(String buildingName, ResourcesService gameResources) {
		updateMoney();
		ZooBuildings zooBuildings = buildingsMap.get(buildingName);
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
				ZooBuildings nextBuildings = new ZooBuildings(next);
				buildings.add(nextBuildings);
				buildingsMap.put(next.getName(), nextBuildings);
			}
		}
		computeIncome();
		return this;
	}

	public synchronized Zoo upgrade(String buildingName) {
		updateMoney();
		ZooBuildings zooBuildings = buildingsMap.get(buildingName);
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

	public synchronized void fight(Zoo other) {
		synchronized (other) {
			Set<String> buildingsSuperSet = new HashSet<>();
			buildingsSuperSet.addAll(
					StreamEx.of(buildings).filter(b -> b.getNumber() != 0).map(ZooBuildings::getName).toList());
			buildingsSuperSet.addAll(
					StreamEx.of(other.buildings).filter(b -> b.getNumber() != 0).map(ZooBuildings::getName).toList());
			int thisWins = 0, otherWins = 0;
			for (String building : buildingsSuperSet) {
				ZooBuildings thisBuildings = buildingsMap.get(building);
				ZooBuildings otherBuildings = other.buildingsMap.get(building);

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
					otherBuildings.setNumber(0);
				} else {
					otherWins++;
					otherBuildings.setNumber(otherBuildings.getNumber() - thisBuildings.getNumber());
					thisBuildings.setNumber(0);
				}
			}
			computeIncome();
			other.computeIncome();
			if (thisWins >= otherWins) {
				fightWins++;
			} else {
				other.fightWins++;
			}
		}
	}

	interface Timer {
		long now();
	}

}