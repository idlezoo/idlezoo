package idlezoo.game.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import one.util.streamex.StreamEx
import org.springframework.util.Assert
import java.util.*

class Perks {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
            Type(value = AllIncomeMultiplier::class, name = "all-income"),
            Type(value = AnimalIncomeMultiplier::class, name = "animal-income"),
            Type(value = AnimalToAnimalPerk::class, name = "animal-to-animal")
    )
    abstract class Perk(val cost: Double, val name: String, val description: String, val rules: List<Rule>) {
        init {
            Assert.isTrue(cost > 0.0, "Perk cost must be > 0")
            Objects.requireNonNull(name)
            Objects.requireNonNull(description)
            Objects.requireNonNull(rules)
            Assert.isTrue(!rules.isEmpty(), "Perk must have some rules")
        }

        fun isAvailable(zoo: Zoo): Boolean {
            return StreamEx.of(rules).allMatch { rule -> rule.isAvailable(zoo) }
        }

        abstract fun perkIncome(zoo: Zoo): Double

        override fun toString(): String {
            return name
        }
    }

    class AllIncomeMultiplier @JsonCreator
    internal constructor(@JsonProperty("cost") cost: Double,
                         @JsonProperty("name") name: String,
                         @JsonProperty("description") description: String,
                         @JsonProperty("rules") rules: List<Rule>,
                         @param:JsonProperty("multiplier") private val multiplier: Double) : Perk(cost, name, description, rules) {

        init {
            Assert.isTrue(multiplier > 0, "Perk multiplier must be > 0")
        }

        override fun perkIncome(zoo: Zoo): Double {
            return multiplier * zoo.baseIncome
        }
    }

    class AnimalToAnimalPerk @JsonCreator
    internal constructor(@JsonProperty("cost") cost: Double,
                         @JsonProperty("name") name: String,
                         @JsonProperty("description") description: String,
                         @JsonProperty("rules") rules: List<Rule>,
                         @JsonProperty("animal") animal: String,
                         @JsonProperty("perkingAnimal") perkingAnimal: String,
                         @param:JsonProperty("multiplier") private val multiplier: Double) : Perk(cost, name, description, rules) {
        private val animal: String
        private val perkingAnimal: String

        init {
            this.animal = Objects.requireNonNull(animal)
            this.perkingAnimal = Objects.requireNonNull(perkingAnimal)
            Assert.isTrue(multiplier > 0, "Perk multiplier must be > 0")
        }

        override fun perkIncome(zoo: Zoo): Double {
            val perkingBuilding = zoo.animal(perkingAnimal)
            val building = zoo.animal(animal)
            return if (perkingBuilding == null || building == null) {
                0.0
            } else building.getIncome() * perkingBuilding.number.toDouble() * multiplier
        }
    }

    class AnimalIncomeMultiplier internal constructor(@JsonProperty("cost") cost: Double,
                                                      @JsonProperty("name") name: String,
                                                      @JsonProperty("description") description: String,
                                                      @JsonProperty("rules") rules: List<Rule>,
                                                      @JsonProperty("animal") animal: String,
                                                      @param:JsonProperty("multiplier") private val multiplier: Double) : Perk(cost, name, description, rules) {
        private val animal: String

        init {
            this.animal = Objects.requireNonNull(animal)
            Assert.isTrue(multiplier > 0, "Perk multiplier must be > 0")
        }

        override fun perkIncome(zoo: Zoo): Double {
            val building = zoo.animal(animal)
            return if (building != null) {
                building.getIncome() * multiplier
            } else 0.0
        }

    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(Type(value = MoreAllAnimals::class, name = "more-all-animals"), Type(value = MoreLosses::class, name = "more-losses"), Type(value = MoreWins::class, name = "more-wins"), Type(value = AnimalLevel::class, name = "animal-level"), Type(value = MoreAnimals::class, name = "more-animals"), Type(value = MoreLostAnimals::class, name = "more-lost-animals"), Type(value = MoreAllLostAnimals::class, name = "more-all-lost-animals"))
    interface Rule {
        fun isAvailable(zoo: Zoo): Boolean
    }

    class MoreWins @JsonCreator
    internal constructor(@param:JsonProperty("wins") private val wins: Int) : Rule {

        init {
            Assert.isTrue(wins > 0, "Wins in rule must be > 0")
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            return zoo.fightWins >= wins
        }
    }

    class MoreLosses @JsonCreator
    internal constructor(@param:JsonProperty("losses") private val losses: Int) : Rule {

        init {
            Assert.isTrue(losses > 0, "Losses in rule must be > 0")
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            return zoo.fightLosses >= losses
        }
    }

    class AnimalLevel @JsonCreator
    internal constructor(@param:JsonProperty("level") private val level: Int,
                         @JsonProperty("animal") animal: String) : Rule {
        private val animal: String

        init {
            Assert.isTrue(level > 0, "Level in rule must be > 0")
            this.animal = Objects.requireNonNull(animal)
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            val building = zoo.animal(animal)
            return building != null && building.level >= level
        }
    }

    class LessAnimals @JsonCreator
    constructor(@param:JsonProperty("number") private val number: Int,
                @JsonProperty("animal") animal: String) : Rule {
        private val animal: String

        init {
            Assert.isTrue(number > 0, "Animal number in rule must be > 0")
            this.animal = Objects.requireNonNull(animal)
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            val building = zoo.animal(animal)
            return building == null || building.number <= number
        }
    }

    class MoreAllAnimals @JsonCreator
    internal constructor(@param:JsonProperty("number") private val number: Int) : Rule {

        init {
            Assert.isTrue(number > 0, "Animal number in rule must be > 0")
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            return number <= StreamEx.of(zoo.buildings).mapToInt { it.number }.sum()
        }
    }

    class MoreAnimals @JsonCreator
    internal constructor(@param:JsonProperty("number") private val number: Int, @JsonProperty("animal") animal: String) : Rule {
        private val animal: String

        init {
            Assert.isTrue(number > 0, "Animal number in rule must be > 0")
            this.animal = Objects.requireNonNull(animal)
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            val building = zoo.animal(animal)
            return building != null && building.number >= number
        }
    }

    class MoreLostAnimals @JsonCreator
    internal constructor(@param:JsonProperty("number") private val number: Int,
                         @JsonProperty("animal") animal: String) : Rule {
        private val animal: String

        init {
            Assert.isTrue(number > 0, "Animal number in rule must be > 0")
            this.animal = Objects.requireNonNull(animal)
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            val building = zoo.animal(animal)
            return building != null && building.lost >= number
        }
    }

    class MoreAllLostAnimals @JsonCreator
    internal constructor(@param:JsonProperty("number") private val number: Int) : Rule {

        init {
            Assert.isTrue(number > 0, "Animal number in rule must be > 0")
        }

        override fun isAvailable(zoo: Zoo): Boolean {
            return StreamEx.of(zoo.buildings)
                    .mapToInt { it.lost }
                    .sum() >= number

        }
    }

}
