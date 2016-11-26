package idlezoo.game.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Zoo {

	private final String name;
	private final List<ZooBuildings> buildings;
	private final Map<String, ZooBuildings> buildingsMap;
	private final double income;
	private final double money;
	private final int fightWins;
	private final int fightLosses;
	private final boolean waitingForFight;
	private final long championTime;

	public Zoo(String name, List<ZooBuildings> buildings, double income, double money,
			int fightWins, int fightLosses,
			boolean waitingForFight, long championTime) {
		this.name = name;
		this.buildings = buildings;
		this.buildingsMap = buildingsMap(buildings);
		this.income = income;
		this.money = money;
		this.fightWins = fightWins;
		this.fightLosses = fightLosses;
		this.waitingForFight = waitingForFight;
		this.championTime = championTime;
	}

	private static Map<String, ZooBuildings> buildingsMap(List<ZooBuildings> buildings) {
		Map<String, ZooBuildings> result = new HashMap<>();
		for (ZooBuildings building : buildings) {
			result.put(building.getName(), building);
		}
		return Collections.unmodifiableMap(result);
	}

	public String getName() {
		return name;
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

	public int getFightLosses() {
		return fightLosses;
	}

	public boolean isWaitingForFight() {
		return waitingForFight;
	}

	public long getChampionTime() {
		return championTime;
	}

	public ZooBuildings animal(String animal) {
		return buildingsMap.get(animal);
	}

	public Zoo withIncome(double income) {
		return new Zoo(name, buildings, income, money, fightWins, fightLosses, waitingForFight,
				championTime);
	}

	public static final class Builder {
		private String name;
		private List<ZooBuildings> buildings;
		private double income;
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
			return this;
		}

		public Builder setIncome(double income) {
			this.income = income;
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

		public Zoo build() {
			return new Zoo(name, buildings, income, money, fightWins, fightLosses, waitingForFight,
					championTime);
		}

	}

}