package idlezoo.game.domain;

import java.util.*;

import idlezoo.game.domain.Perks.Perk;
import org.immutables.value.Value;

@Value.Immutable
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