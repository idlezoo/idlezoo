package idlemage.game;

import static idlemage.game.GameResources.STARTING_BUILDING;
import static java.util.Arrays.asList;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Mage {
	private final List<MageBuildings> buildings = new ArrayList<>(asList(new MageBuildings(STARTING_BUILDING)));
	private Double mana = 100D;
	private LocalDateTime lastManaUpdate = LocalDateTime.now(ZoneOffset.UTC);

	public Double getMana() {
		return mana;
	}

	public Double getManaIncome() {
		return buildings.stream().mapToDouble(MageBuildings::getIncome).sum();
	}

	public List<MageBuildings> getBuildings() {
		return buildings;
	}

	public void setMana(Double mana) {
		this.mana = mana;
	}

	public synchronized void updateMana() {
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		long dif = Duration.between(lastManaUpdate, now).getSeconds();
		mana += dif * getManaIncome();
		lastManaUpdate = now;
	}
}