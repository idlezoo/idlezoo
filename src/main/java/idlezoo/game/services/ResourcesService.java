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
	private Map<String, Building> animalsTypes = new HashMap<>();
	private Map<String, Building> nextAnimals = new HashMap<>();
	private Building startingAnimal;

	@Override
	public void afterPropertiesSet() throws IOException {
		try (InputStream creatures = ResourcesService.class.getResourceAsStream("/animals/animals.json")) {
			CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, Building.class);
			animalsList = unmodifiableList(mapper.readValue(creatures, type));
			startingAnimal = animalsList.get(0);

			Building prev = null;
			for (Building animal : animalsList) {
				animalsTypes.put(animal.getName(), animal);
				if (prev != null) {
					nextAnimals.put(prev.getName(), animal);
				}
				prev = animal;
			}
		}
		animalsTypes = unmodifiableMap(animalsTypes);
		nextAnimals = unmodifiableMap(nextAnimals);

	}

	public double startingMoney() {
		return startingMoney;
	}

	public Building startingAnimal() {
		return startingAnimal;
	}
	
	public String firstName(){
		return startingAnimal.getName();
	}
	
	public String secondName(){
		return nextType(firstName()).getName();
	}

	public Building nextType(String buildingName) {
		return nextAnimals.get(buildingName);
	}

	public Building type(String typeName) {
		return animalsTypes.get(typeName);
	}

	public List<Building> getAnimalsList() {
		return animalsList;
	}

}
