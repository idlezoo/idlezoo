package idlezoo.game.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.immutables.value.Value;

@Value.Immutable
@JsonPropertyOrder(alphabetic = true)
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
        return getBuilding().upgradeCost(getLevel());
    }

    int getLevel();

    int getNumber();

    int getLost();

    Building getBuilding();
}