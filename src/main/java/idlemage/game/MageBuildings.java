package idlemage.game;

public class MageBuildings {
	private final String name;
	private final Building building;
	private int level = 0;
	private int number = 0;

	public MageBuildings(String name) {
		this.name = name;
		this.building = GameResources.BUILDING_TYPES.get(name);
	}

	public MageBuildings(Building building) {
		this.name = building.getName();
		this.building = building;
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

	// Game logic
	public void upgrade() {
		this.level++;
	}

	public void buy() {
		this.number++;
	}

	public boolean first() {
		return number == 1;
	}

}