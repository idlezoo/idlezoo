package idlezoo.game.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = ImmutableBuilding.Builder.class)
@Value.Immutable
public interface Building {
    // logic start
    default double income(int level) {
        return getBaseIncome() + getBaseIncome() * level * level;
    }

    default double upgradeCost(int level) {
        return getBaseUpgrade() * Math.pow(2, level);
    }

    default double buildCost(int index) {
        // For now taken from Coockie-clicker
        return getBaseCost() * Math.pow(1.15, index);
    }
    // logic end

    double getBaseCost();

    double getBaseIncome();

    double getBaseUpgrade();

    String getName();
}