package idlemage.game.domain;

import java.util.List;

public class MageDTO {

    private final List<MageBuildings> buildings;
    private final double income;
    private final double mana;
    private final int fightWins;
    private final boolean waitingForFight;
    private final long championTime;

    public MageDTO(Mage mage) {
      this.buildings = mage.getBuildings();
      this.income = mage.getIncome();
      this.mana = mage.getMana();
      this.fightWins = mage.getFightWins();
      this.waitingForFight = mage.isWaitingForFight();
      this.championTime = mage.getChampionTime();
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

    public int getFightWins() {
      return fightWins;
    }

    public boolean isWaitingForFight() {
      return waitingForFight;
    }

    public long getChampionTime() {
      return championTime;
    }
  }