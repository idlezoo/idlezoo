package idlezoo.game.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import idlezoo.game.domain.Perks.Perk;

@JsonPropertyOrder(alphabetic = true)
data class Zoo(
        val buildings: List<ZooBuildings>,
        val name: String,
        val money: Double,
        val perkIncome: Double,
        val perks: List<Perk>,
        val availablePerks: List<Perk>,
        val fightWins: Int,
        val fightLosses: Int,
        val waitingForFight: Boolean,
        val championTime: Long,
        val baseIncome: Double
) {

    private val buildingsMap: Map<String, ZooBuildings>

    init {
        buildingsMap = mutableMapOf()
        buildings.forEach { buildingsMap.put(it.getName(), it) }
    }

    fun getMoneyIncome(): Double = baseIncome + perkIncome
    fun animal(animal: String): ZooBuildings? = buildingsMap[animal]

}