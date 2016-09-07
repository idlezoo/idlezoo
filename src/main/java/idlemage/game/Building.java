package idlemage.game;

public class Building {
	private final String name;
	private final double baseCost;
	private final double baseIncome;
	private final double baseUpgrade;

	public Building(String name,
			double baseCost, double baseIncome, double baseUpgrade) {
		this.name = name;
		this.baseCost = baseCost;
		this.baseIncome = baseIncome;
		this.baseUpgrade = baseUpgrade;
	}

	public String getName() {
		return name;
	}

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

}