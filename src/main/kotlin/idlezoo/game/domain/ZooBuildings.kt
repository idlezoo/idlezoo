package idlezoo.game.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder


@JsonPropertyOrder(alphabetic = true)
data class ZooBuildings(val level: Int, val number: Int, val lost: Int, val building: Building) {
    fun getIncome(): Double = building.income(level) * number
    fun getNextCost(): Double = building.buildCost(number)
    fun getUpgradeCost(): Double = building.upgradeCost(level)
    fun getName(): String = building.name
}