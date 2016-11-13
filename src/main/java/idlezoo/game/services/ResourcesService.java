package idlezoo.game.services;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import idlezoo.game.domain.Building;

@Service
public class ResourcesService implements InitializingBean {

  @Autowired
  private ObjectMapper mapper;

  private double startingMoney = 50D;
  private List<Building> animalsList;
  private Map<String, Integer> animalIndexes = new HashMap<>();
  private Map<String, Building> animalTypes = new HashMap<>();
  private Map<String, Building> nextAnimals = new HashMap<>();
  private Building startingAnimal;

  @Override
  public void afterPropertiesSet() throws IOException {
    try (InputStream creatures = ResourcesService.class.getResourceAsStream(
        "/animals/animals.json")) {
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

  public double startingMoney() {
    return startingMoney;
  }

  public Building startingAnimal() {
    return startingAnimal;
  }

  public String firstName() {
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
  
  public Building byIndex(int index){
    return animalsList.get(index);
  }
  
  public int index(String animalName){
    return animalIndexes.get(animalName);
  }

}
