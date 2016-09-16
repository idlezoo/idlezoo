package idlemage.game.services;

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

import idlemage.game.domain.Building;

@Service
public class ResourcesService implements InitializingBean {

	@Autowired
	private ObjectMapper mapper;

	private double startingMana = 50D;
	private List<Building> creaturesList;
	private Map<String, Building> creaturesTypes = new HashMap<>();
	private Map<String, Building> nextCreatures = new HashMap<>();
	private Building startingCreature;

	@Override
	public void afterPropertiesSet() throws IOException {
		try (InputStream creatures = ResourcesService.class.getResourceAsStream("/creatures/creatures.json")) {
			CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, Building.class);
			creaturesList = unmodifiableList(mapper.readValue(creatures, type));
			startingCreature = creaturesList.get(0);

			Building previous = null;
			for (Building creature : creaturesList) {
				creaturesTypes.put(creature.getName(), creature);
				if (previous != null) {
					nextCreatures.put(previous.getName(), creature);
				}
				previous = creature;
			}
		}
		creaturesTypes = unmodifiableMap(creaturesTypes);
		nextCreatures = unmodifiableMap(nextCreatures);

	}

	public double startingMana() {
		return startingMana;
	}

	public Building startingCreature() {
		return startingCreature;
	}
	
	public String firstName(){
		return startingCreature.getName();
	}
	
	public String secondName(){
		return nextType(firstName()).getName();
	}

	public Building nextType(String buildingName) {
		return nextCreatures.get(buildingName);
	}

	public Building type(String typeName) {
		return creaturesTypes.get(typeName);
	}

	public List<Building> getCreaturesList() {
		return creaturesList;
	}

}
