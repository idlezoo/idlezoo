package idlezoo.game.domain;

import org.immutables.value.Value;

@Value.Immutable
public interface ZooBuildings {
    // Getters for JSON
    String getName();

    default double getIncome() {
        return getBuilding().income(getLevel()) * getNumber();
    }

    default double getNextCost() {
        return getBuilding().buildCost(getNumber());
    }

    default double getUpgradeCost() {
        return getBuilding().upgradeCost(getNumber());
    }

    default boolean first() {
        return getNumber() == 1;
    }

    int getLevel();

    int getNumber();

    int getLost();

    Building getBuilding();
}