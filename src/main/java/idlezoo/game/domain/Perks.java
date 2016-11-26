package idlezoo.game.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import one.util.streamex.StreamEx;

public class Perks {

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@Type(value = AllIncomeMultiplier.class, name = "all-income"),
			@Type(value = AnimalIncomeMultiplier.class, name = "animal-income") })
	public static abstract class Perk {
		private final List<Rule> rules;

		public Perk(List<Rule> rules) {
			this.rules = rules;
		}

		boolean isAvailable(Zoo zoo) {
			return StreamEx.of(rules).allMatch(rule -> rule.isAvailable(zoo));
		}

		public abstract double perkIncome(Zoo zoo);
	}

	public static class AllIncomeMultiplier extends Perk {
		private final double multiplier;

		@JsonCreator
		public AllIncomeMultiplier(@JsonProperty("rules") List<Rule> rules,
				@JsonProperty("multiplier") double multiplier) {
			super(rules);
			this.multiplier = multiplier;
		}

		@Override
		public double perkIncome(Zoo zoo) {
			return multiplier * zoo.getMoneyIncome();
		}
	}
	
//	public static class AnimalToAnimalPerk extends Perk {
//		private final String animal;
//		private final String perkingAnimal;
//		private final double multiplier;
//
//		@JsonCreator
//		public AnimalToAnimalPerk(@JsonProperty("rules") List<Rule> rules,
//				@JsonProperty("animal") String animal,
//				@JsonProperty("perkingAnimal") String perkingAnimal,
//				@JsonProperty("multiplier") double multiplier) {
//			super(rules);
//			this.animal=animal;
//			this.perkingAnimal=perkingAnimal;
//			this.multiplier = multiplier;
//		}
//
//		@Override
//		public double perkIncome(Zoo zoo) {
//			
//			
//			ZooBuildings perkingBuilding = zoo.animal(perkingAnimal);
//			if(perkingBuilding)
//			zoo.animal(perkingAnimal).getNumber() * multiplier;
//			
//			return multiplier * zoo.getMoneyIncome();
//		}
//	}
//	

	public static class AnimalIncomeMultiplier extends Perk {
		private final String animal;
		private final double multiplier;

		public AnimalIncomeMultiplier(@JsonProperty("rules") List<Rule> rules,
				@JsonProperty("animal") String animal,
				@JsonProperty("multiplier") double multiplier) {
			super(rules);
			this.animal = animal;
			this.multiplier = multiplier;
		}

		@Override
		public double perkIncome(Zoo zoo) {
			ZooBuildings building = zoo.animal(animal);
			if (building != null) {
				return building.getIncome() * multiplier;
			}
			return 0;
		}

	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@Type(value = MoreAllAnimals.class, name = "more-all-animals"),
			@Type(value = MoreLosses.class, name = "more-losses"),
			@Type(value = MoreWins.class, name = "more-wins"),
			@Type(value = AnimalLevel.class, name = "animal-level"),
			@Type(value = MoreAnimals.class, name = "more-animals"),
			@Type(value = MoreLostAnimals.class, name = "more-lost-animals"),
			@Type(value = MoreAllLostAnimals.class, name = "more-all-lost-animals"),
	})
	public interface Rule {
		boolean isAvailable(Zoo zoo);
	}

	public static class MoreWins implements Rule {
		private final int wins;

		@JsonCreator
		public MoreWins(@JsonProperty("wins") int wins) {
			this.wins = wins;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			return zoo.getFightWins() >= wins;
		}
	}

	public static class MoreLosses implements Rule {
		private final int losses;

		@JsonCreator
		public MoreLosses(@JsonProperty("losses") int losses) {
			this.losses = losses;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			return zoo.getFightLosses() >= losses;
		}
	}

	public static class AnimalLevel implements Rule {
		private final int level;
		private final String animal;

		@JsonCreator
		public AnimalLevel(@JsonProperty("level") int level,
				@JsonProperty("animal") String animal) {
			this.level = level;
			this.animal = animal;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			ZooBuildings building = zoo.animal(animal);
			return building != null && building.getLevel() >= level;
		}
	}

	public static class LessAnimals implements Rule {
		private final int number;
		private final String animal;

		@JsonCreator
		public LessAnimals(@JsonProperty("number") int number,
				@JsonProperty("animal") String animal) {
			this.number = number;
			this.animal = animal;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			ZooBuildings building = zoo.animal(animal);
			return building == null || building.getNumber() <= number;
		}
	}

	public static class MoreAllAnimals implements Rule {
		private final int number;

		@JsonCreator
		public MoreAllAnimals(@JsonProperty("number") int number) {
			this.number = number;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			return number < StreamEx.of(zoo.getBuildings()).mapToInt(ZooBuildings::getNumber).sum();
		}
	}

	public static class MoreAnimals implements Rule {
		private final int number;
		private final String animal;

		@JsonCreator
		public MoreAnimals(@JsonProperty("number") int number, @JsonProperty("animal") String animal) {
			this.number = number;
			this.animal = animal;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			ZooBuildings building = zoo.animal(animal);
			return building != null && building.getNumber() >= number;
		}
	}

	public static class MoreLostAnimals implements Rule {
		private final int number;
		private final String animal;

		@JsonCreator
		public MoreLostAnimals(@JsonProperty("number") int number,
				@JsonProperty("animal") String animal) {
			this.number = number;
			this.animal = animal;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			ZooBuildings building = zoo.animal(animal);
			return building != null && building.getLost() >= number;
		}
	}

	public static class MoreAllLostAnimals implements Rule {
		private final int number;

		@JsonCreator
		public MoreAllLostAnimals(@JsonProperty("number") int number) {
			this.number = number;
		}

		@Override
		public boolean isAvailable(Zoo zoo) {
			return StreamEx.of(zoo.getBuildings())
					.mapToInt(ZooBuildings::getLost)
					.sum() >= number;

		}
	}

}
