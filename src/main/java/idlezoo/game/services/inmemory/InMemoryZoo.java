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
import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService.Outcome;
import idlezoo.game.services.ResourcesService;
import one.util.streamex.StreamEx;

public class InMemoryZoo {
	private static final Timer DEFAULT_TIMER = () -> java.time.Clock.systemUTC().instant().getEpochSecond();

	private static final AtomicInteger ID_COUNTER = new AtomicInteger();
	private final Integer id = ID_COUNTER.incrementAndGet();
	private final String name;
	private final String password;
	private final Timer timer;
	private final List<InMemoryZooBuildings> buildings;
	private final Map<String, InMemoryZooBuildings> buildingsMap;
	private double money;
	private long lastMoneyUpdate;
	private int fightWins;
	private long championTime;
	private Long waitingForFightStart;
	// cached value - trade memory for CPU
	private double income;

	public InMemoryZoo(String name, String password, ResourcesService gameResources) {
		this(name, password, gameResources, DEFAULT_TIMER);
	}

	public Zoo toDTO() {
		return new Zoo(name, StreamEx.of(buildings).map(InMemoryZooBuildings::toDTO).toList(), income, money, fightWins,
				waitingForFightStart != null, championTime);
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

	public double getMoney() {
		return money;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
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
		income = buildings.stream().mapToDouble(InMemoryZooBuildings::getIncome).sum();
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

	public List<InMemoryZooBuildings> getBuildings() {
		return buildings;
	}

	public Map<String, InMemoryZooBuildings> getBuildingsMap() {
		return buildingsMap;
	}

	// Logic
	public synchronized InMemoryZoo updateMoney() {
		long now = timer.now();
		money += (now - lastMoneyUpdate) * income;
		lastMoneyUpdate = now;
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
					StreamEx.of(buildings).filter(b -> b.getNumber() != 0).map(InMemoryZooBuildings::getName).toList());
			buildingsSuperSet.addAll(
					StreamEx.of(other.buildings).filter(b -> b.getNumber() != 0).map(InMemoryZooBuildings::getName)
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
				return Outcome.LOSS;
			} else {
				other.fightWins++;
				return Outcome.WIN;
			}
		}
	}

	interface Timer {
		long now();
	}

}