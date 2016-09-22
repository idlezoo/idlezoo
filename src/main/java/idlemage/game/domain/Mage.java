package idlemage.game.domain;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

import idlemage.game.services.ResourcesService;
import one.util.streamex.StreamEx;

public class Mage {
	private static final Timer DEFAULT_TIMER = () -> java.time.Clock.systemUTC().instant().getEpochSecond();

	private final String name;
	private final Timer timer;
	private final List<MageBuildings> buildings;
	private final Map<String, MageBuildings> buildingsMap;
	private double mana;
	private long lastManaUpdate;
	private int fightWins;
	private long championTime;
	private Long waitingForFightStart;
	// private boolean waitingForFight;
	// cached value - trade memory for CPU
	private double income;

	public Mage(String name, ResourcesService gameResources) {
		this(name, gameResources, DEFAULT_TIMER);
	}

	Mage(String name, ResourcesService gameResources, Timer timer) {
		this.name = name;
		buildings = new ArrayList<>(asList(new MageBuildings(gameResources.startingCreature())));
		buildingsMap = new HashMap<>(singletonMap(gameResources.startingCreature().getName(), buildings
				.get(0)));
		mana = gameResources.startingMana();

		this.timer = timer;
		lastManaUpdate = timer.now();
	}

	public void setMana(double mana) {
		this.mana = mana;
	}

	public double getMana() {
		return mana;
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

	public long getLastManaUpdate() {
		return lastManaUpdate;
	}

	private void computeIncome() {
		income = buildings.stream().mapToDouble(MageBuildings::getIncome).sum();
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

	public List<MageBuildings> getBuildings() {
		return buildings;
	}

	public Map<String, MageBuildings> getBuildingsMap() {
		return buildingsMap;
	}

	// Logic
	public synchronized Mage updateMana() {
		long now = timer.now();
		mana += (now - lastManaUpdate) * income;
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
			return this;
		}
		mana -= mageBuildings.getNextCost();
		mageBuildings.buy();
		if (mageBuildings.first()) {
			Building next = gameResources.nextType(buildingName);
			if (next != null && !buildingsMap.containsKey(next.getName())) {
				MageBuildings nextBuildings = new MageBuildings(next);
				buildings.add(nextBuildings);
				buildingsMap.put(next.getName(), nextBuildings);
			}
		}
		computeIncome();
		return this;
	}

	public synchronized Mage upgrade(String buildingName) {
		updateMana();
		MageBuildings mageBuildings = buildingsMap.get(buildingName);
		if (mageBuildings == null) {
			throw new IllegalStateException("Upgrading " + buildingName + " is not available");
		}
		if (mana < mageBuildings.getUpgradeCost()) {
			return this;
		}

		mana -= mageBuildings.getUpgradeCost();
		mageBuildings.upgrade();
		computeIncome();
		return this;
	}

	public synchronized void fight(Mage other) {
		synchronized (other) {
			Set<String> buildingsSuperSet = new HashSet<>();
			buildingsSuperSet.addAll(
					StreamEx.of(buildings).filter(b -> b.getNumber() != 0).map(MageBuildings::getName).toList());
			buildingsSuperSet.addAll(
					StreamEx.of(other.buildings).filter(b -> b.getNumber() != 0).map(MageBuildings::getName).toList());
			int thisWins = 0, otherWins = 0;
			for (String building : buildingsSuperSet) {
				MageBuildings thisBuildings = buildingsMap.get(building);
				MageBuildings otherBuildings = other.buildingsMap.get(building);

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