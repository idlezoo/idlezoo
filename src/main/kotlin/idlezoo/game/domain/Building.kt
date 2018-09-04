package idlezoo.game.domain

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(alphabetic = true)
data class Building(val baseCost: Double, val baseIncome: Double, val baseUpgrade: Double, val name: String) {
    fun income(level: Int): Double = baseIncome + baseIncome * level * level
    fun upgradeCost(level: Int): Double = baseUpgrade * Math.pow(2.0, level.toDouble())
    fun buildCost(index: Int): Double = baseCost * Math.pow(1.15, index.toDouble())
}