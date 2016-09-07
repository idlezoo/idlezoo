package idlemage.game;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class GameResources {

	public static final Map<String, Building> BUILDING_TYPES;
	public static final Map<String, Building> NEXT_TYPES;
	public static final Building STARTING_BUILDING;

	static {
		Map<String, Building> buildingTypes = new HashMap<>();
		Map<String, Building> nextTypes = new HashMap<>();

		Building rats = new Building("Rats", 10, 1, 100);
		buildingTypes.put(rats.getName(), rats);
		
		STARTING_BUILDING = rats;

		Building spiders = new Building("Spiders", 50, 3, 600);
		buildingTypes.put(spiders.getName(), spiders);
		nextTypes.put(rats.getName(), spiders);

		Building wolves = new Building("Wolves", 100, 5, 1000);
		buildingTypes.put(wolves.getName(), wolves);
		nextTypes.put(spiders.getName(), wolves);

		Building lizards = new Building("Lizards", 1000, 10, 10000);
		buildingTypes.put(lizards.getName(), lizards);
		nextTypes.put(wolves.getName(), lizards);

		Building golems = new Building("Golems", 5000, 30, 600000);
		buildingTypes.put(golems.getName(), golems);
		nextTypes.put(lizards.getName(), golems);

		Building medusas = new Building("Medusas", 10000, 50, 1000000);
		buildingTypes.put(medusas.getName(), medusas);
		nextTypes.put(golems.getName(), medusas);

		Building dragons = new Building("Dragons", 100000, 100, 10000000);
		buildingTypes.put(dragons.getName(), dragons);
		nextTypes.put(medusas.getName(), dragons);

		NEXT_TYPES = unmodifiableMap(nextTypes);
		BUILDING_TYPES = unmodifiableMap(buildingTypes);
	}

}
