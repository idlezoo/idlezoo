package idlezoo.game.services;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.Perks.Perk;
import one.util.streamex.StreamEx;

@Service
public class ResourcesService implements InitializingBean {

    private final ObjectMapper mapper;

    private double startingMoney = 50D;
    private List<Building> animalsList;
    private List<Perk> perkList;
    private Map<String, Integer> animalIndexes = new HashMap<>();
    private Map<String, Building> animalTypes = new HashMap<>();
    private Map<String, Building> nextAnimals = new HashMap<>();
    private Map<String, Integer> perkIndexes = new HashMap<>();
    private Map<String, Perk> perkNames = new HashMap<>();
    private Building startingAnimal;

    public ResourcesService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        initAnimals();
        initPerks();
    }

    private void initPerks() throws IOException {
        try (InputStream perks = ResourcesService.class.getResourceAsStream(
                "/mechanics/perks.json")) {
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, Perk.class);
            perkList = unmodifiableList(mapper.readValue(perks, type));
            for (int i = 0; i < perkList.size(); i++) {
                Perk perk = perkList.get(i);
                perkIndexes.put(perk.getName(), i);
                perkNames.put(perk.getName(), perk);
            }
            perkIndexes = unmodifiableMap(perkIndexes);
            perkNames = unmodifiableMap(perkNames);
        }
    }

    private void initAnimals() throws IOException {
        try (InputStream creatures = ResourcesService.class.getResourceAsStream(
                "/mechanics/animals.json")) {
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class,
                    Building.class);
            animalsList = unmodifiableList(mapper.readValue(creatures, type));
            startingAnimal = animalsList.get(0);

            Building prev = null;
            for (int i = 0; i < animalsList.size(); i++) {
                Building animal = animalsList.get(i);
                animalIndexes.put(animal.getName(), i);
                animalTypes.put(animal.getName(), animal);
                if (prev != null) {
                    nextAnimals.put(prev.getName(), animal);
                }
                prev = animal;
            }
        }
        animalTypes = unmodifiableMap(animalTypes);
        nextAnimals = unmodifiableMap(nextAnimals);
        animalIndexes = unmodifiableMap(animalIndexes);
    }

    double startingMoney() {
        return startingMoney;
    }

    Building startingAnimal() {
        return startingAnimal;
    }

    String firstName() {
        return startingAnimal.getName();
    }

    public String secondName() {
        return nextType(firstName()).getName();
    }

    public Building nextType(String buildingName) {
        return nextAnimals.get(buildingName);
    }

    public Building type(String typeName) {
        return animalTypes.get(typeName);
    }

    public List<Building> getAnimalsList() {
        return animalsList;
    }

    public List<Perk> getPerkList() {
        return perkList;
    }

    public Perk perkByIndex(int index) {
        return perkList.get(index);
    }

    public Perk perk(String name) {
        return perkNames.get(name);
    }

    public Integer perkIndex(String perkName) {
        return perkIndexes.get(perkName);
    }

    public Building animalByIndex(int index) {
        return animalsList.get(index);
    }

    public Integer animalIndex(String animalName) {
        return animalIndexes.get(animalName);
    }

    public List<Perk> availablePerks(Zoo zoo) {
        List<Perk> result = new ArrayList<>(perkList);
        result.removeAll(zoo.getPerks());
        return StreamEx.of(result)
                .filter(perk -> perk.isAvailable(zoo))
                .toList();
    }

}
