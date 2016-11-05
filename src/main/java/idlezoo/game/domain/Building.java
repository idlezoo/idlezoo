package idlezoo.game.domain;

public class Building {
	private String name;
	private double baseCost;
	private double baseIncome;
	private double baseUpgrade;

	public Building() {
	}

	public Building(String name,
			double baseCost, double baseIncome, double baseUpgrade) {
		this.name = name;
		this.baseCost = baseCost;
		this.baseIncome = baseIncome;
		this.baseUpgrade = baseUpgrade;
	}

	// logic start
	// TODO Lombok all the boilerplate!
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

	public void setName(String name) {
		this.name = name;
	}

	public void setBaseCost(double baseCost) {
		this.baseCost = baseCost;
	}

	public void setBaseIncome(double baseIncome) {
		this.baseIncome = baseIncome;
	}

	public void setBaseUpgrade(double baseUpgrade) {
		this.baseUpgrade = baseUpgrade;
	}

	public String getName() {
		return name;
	}

}