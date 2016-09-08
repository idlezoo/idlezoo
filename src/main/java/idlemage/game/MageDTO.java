package idlemage.game;

import java.util.List;

public class MageDTO {

	private final List<MageBuildings> buildings;
	private final double income;
	private final double mana;

	public MageDTO(Mage mage) {
		this.buildings = mage.getBuildings();
		this.income = mage.getIncome();
		this.mana = mage.getMana();
	}

	public double getMana() {
		return mana;
	}

	public double getManaIncome() {
		return income;
	}

	public List<MageBuildings> getBuildings() {
		return buildings;
	}

}
