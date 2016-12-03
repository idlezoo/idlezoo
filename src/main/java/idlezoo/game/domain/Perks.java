package idlezoo.game.domain;

import java.util.List;
import java.util.Objects;

import org.springframework.util.Assert;

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
      @Type(value = AnimalIncomeMultiplier.class, name = "animal-income"),
      @Type(value = AnimalToAnimalPerk.class, name = "animal-to-animal")})
  public static abstract class Perk {
    private final double cost;
    private final String name;
    private final String description;
    private final List<Rule> rules;

    public Perk(double cost, String name, String description, List<Rule> rules) {
      this.cost = cost;
      Assert.isTrue(cost > 0.0);
      this.name = Objects.requireNonNull(name);
      this.description = Objects.requireNonNull(description);
      this.rules = Objects.requireNonNull(rules);
      Assert.isTrue(!rules.isEmpty());
    }

    public double getCost() {
      return cost;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public boolean isAvailable(ZooInfo zoo) {
      return StreamEx.of(rules).allMatch(rule -> rule.isAvailable(zoo));
    }

    public abstract double perkIncome(ZooInfo zoo);

    @Override
    public String toString() {
      return name;
    }
  }

  public static class AllIncomeMultiplier extends Perk {
    private final double multiplier;

    @JsonCreator
    public AllIncomeMultiplier(@JsonProperty("cost") Double cost,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("rules") List<Rule> rules,
        @JsonProperty("multiplier") double multiplier) {
      super(cost, name, description, rules);

      this.multiplier = multiplier;
      Assert.isTrue(multiplier > 0);
    }

    @Override
    public double perkIncome(ZooInfo zoo) {
      return multiplier * zoo.getBaseIncome();
    }
  }

  public static class AnimalToAnimalPerk extends Perk {
    private final String animal;
    private final String perkingAnimal;
    private final double multiplier;

    @JsonCreator
    public AnimalToAnimalPerk(@JsonProperty("cost") double cost,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("rules") List<Rule> rules,
        @JsonProperty("animal") String animal,
        @JsonProperty("perkingAnimal") String perkingAnimal,
        @JsonProperty("multiplier") double multiplier) {
      super(cost, name, description, rules);
      this.animal = Objects.requireNonNull(animal);
      this.perkingAnimal = Objects.requireNonNull(perkingAnimal);
      this.multiplier = multiplier;
      Assert.isTrue(multiplier > 0);
    }

    @Override
    public double perkIncome(ZooInfo zoo) {
      ZooBuildings perkingBuilding = zoo.animal(perkingAnimal);
      ZooBuildings building = zoo.animal(animal);
      if (perkingBuilding == null || building == null) {
        return 0;
      }
      return building.getIncome() * perkingBuilding.getNumber() * multiplier;
    }
  }

  public static class AnimalIncomeMultiplier extends Perk {
    private final String animal;
    private final double multiplier;

    public AnimalIncomeMultiplier(@JsonProperty("cost") Double cost,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("rules") List<Rule> rules,
        @JsonProperty("animal") String animal,
        @JsonProperty("multiplier") Double multiplier) {
      super(cost, name, description, rules);
      this.animal = Objects.requireNonNull(animal);
      this.multiplier = Objects.requireNonNull(multiplier);
    }

    @Override
    public double perkIncome(ZooInfo zoo) {
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
    boolean isAvailable(ZooInfo zoo);
  }

  public static class MoreWins implements Rule {
    private final int wins;

    @JsonCreator
    public MoreWins(@JsonProperty("wins") int wins) {
      this.wins = wins;
      Assert.isTrue(wins > 0);
    }

    @Override
    public boolean isAvailable(ZooInfo zoo) {
      return zoo.getFightWins() >= wins;
    }
  }

  public static class MoreLosses implements Rule {
    private final int losses;

    @JsonCreator
    public MoreLosses(@JsonProperty("losses") int losses) {
      this.losses = losses;
      Assert.isTrue(losses > 0);
    }

    @Override
    public boolean isAvailable(ZooInfo zoo) {
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
      Assert.isTrue(level > 0);
      this.animal = Objects.requireNonNull(animal);
    }

    @Override
    public boolean isAvailable(ZooInfo zoo) {
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
      Assert.isTrue(number > 0);
      this.animal = Objects.requireNonNull(animal);
    }

    @Override
    public boolean isAvailable(ZooInfo zoo) {
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
    public boolean isAvailable(ZooInfo zoo) {
      return number <= StreamEx.of(zoo.getBuildings()).mapToInt(ZooBuildings::getNumber).sum();
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
    public boolean isAvailable(ZooInfo zoo) {
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
    public boolean isAvailable(ZooInfo zoo) {
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
    public boolean isAvailable(ZooInfo zoo) {
      return StreamEx.of(zoo.getBuildings())
          .mapToInt(ZooBuildings::getLost)
          .sum() >= number;

    }
  }

}
