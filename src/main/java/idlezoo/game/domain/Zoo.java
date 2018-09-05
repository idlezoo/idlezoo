package idlezoo.game.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import idlezoo.game.domain.Perks.Perk;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value.Immutable
@JsonPropertyOrder(alphabetic = true)
public abstract class Zoo {

    private Map<String, ZooBuildings> buildingsMap;

    public double getMoneyIncome() {
        return getBaseIncome() + getPerkIncome();
    }

    public ZooBuildings animal(String animal) {
        if (buildingsMap == null) {
            buildingsMap = buildingsMap(getBuildings());
        }
        return buildingsMap.get(animal);
    }

    public abstract List<ZooBuildings> getBuildings();

    public abstract String getName();

    public abstract double getMoney();

    public abstract double getPerkIncome();

    public abstract List<Perk> getPerks();

    public abstract List<Perk> getAvailablePerks();

    public abstract int getFightWins();

    public abstract int getFightLosses();

    public abstract boolean isWaitingForFight();

    public abstract long getChampionTime();

    public abstract double getBaseIncome();

    private static Map<String, ZooBuildings> buildingsMap(List<ZooBuildings> buildings) {
        Map<String, ZooBuildings> result = new HashMap<>();
        for (ZooBuildings building : buildings) {
            result.put(building.getName(), building);
        }
        return Collections.unmodifiableMap(result);
    }
}