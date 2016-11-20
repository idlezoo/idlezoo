package idlezoo.game.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import idlezoo.game.services.ResourcesService;

public class Perks {

  public static abstract class Perk {
    private List<Rule> rules;

    public void setRules(List<Rule> rules) {
      this.rules = rules;
    }

    public List<Rule> getRules() {
      return rules;
    }
  }

  public interface Rule {
    boolean isAvailable(ResourcesService resService, Zoo zoo);
  }

  public static class MoreWins implements Rule {
    private final int wins;

    @JsonCreator
    public MoreWins(@JsonProperty("wins") int wins) {
      this.wins = wins;
    }

    @Override
    public boolean isAvailable(ResourcesService resService, Zoo zoo) {
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
    public boolean isAvailable(ResourcesService resService, Zoo zoo) {
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
    public boolean isAvailable(ResourcesService resService, Zoo zoo) {
      Integer index = resService.index(animal);
      return zoo.animalLevelByIndex(index) >= level;
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
    public boolean isAvailable(ResourcesService resService, Zoo zoo) {
      Integer index = resService.index(animal);
      return zoo.animalNumberByIndex(index) <= number;
    }
  }

  public static class MoreAnimals implements Rule {
    private final int number;
    private final String animal;

    @JsonCreator
    public MoreAnimals(@JsonProperty("number") int number,
        @JsonProperty("animal") String animal) {
      this.number = number;
      this.animal = animal;
    }

    @Override
    public boolean isAvailable(ResourcesService resService, Zoo zoo) {
      Integer index = resService.index(animal);
      return zoo.animalNumberByIndex(index) >= number;
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
    public boolean isAvailable(ResourcesService resService, Zoo zoo) {
      Integer index = resService.index(animal);
      return zoo.animalLostByIndex(index) >= number;
    }
  }
  

}
