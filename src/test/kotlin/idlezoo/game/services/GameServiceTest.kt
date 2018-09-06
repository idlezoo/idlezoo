package idlezoo.game.services

import idlezoo.game.domain.Zoo
import idlezoo.security.UsersService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class GameServiceTest @Autowired constructor(template: JdbcTemplate, usersService: UsersService, resourcesService: ResourcesService,
                                             val gameService: GameService) : AbstractServiceTest(template, usersService, resourcesService) {
    @Test
    fun testGetZoo() {
        val zoo1 = gameService.getZoo(zoo1Id)
        assertEquals(0, zoo1.fightWins)
        assertEquals(1, zoo1.buildings.size)
        assertEquals(0, zoo1.buildings[0].number)
        assertFalse(zoo1.waitingForFight)
        assertEquals(0.0, zoo1.getMoneyIncome(), 0.0001)
        assertEquals(50.0, zoo1.money, 0.0001)
    }

    @Test
    fun testBuy() {
        val animalType = resourcesService.firstName()
        val zoo1 = gameService.buy(zoo1Id, animalType)
        val type = resourcesService.type(animalType)
        val moneyAfterBuy = resourcesService.startingMoney() - type.buildCost(0)
        assertEquals(moneyAfterBuy, zoo1.money, 0.0001)
        assertEquals(2, zoo1.buildings.size)
        assertEquals(1, zoo1.buildings[0].number)
        assertEquals(0, zoo1.buildings[1].number)
        assertEquals(type.income(0), zoo1.getMoneyIncome(), 0.0001)
    }

    @Test
    fun testUpgrade() {
        setMoney(zoo1Id, 100.0)

        val animalType = resourcesService.firstName()
        val zoo1 = gameService.upgrade(zoo1Id, animalType)
        val type = resourcesService.type(animalType)
        val moneyAfterUpgrade = 100 - type.upgradeCost(0)
        assertEquals(moneyAfterUpgrade, zoo1.money, 0.0001)
        assertEquals(1, zoo1.buildings.size)
        assertEquals(1, zoo1.buildings[0].level)
    }

    @Test
    fun testBuyPerk() {
        setMoney(zoo1Id, 1000000000.0)
        val animal = resourcesService.firstName()
        var zoo1: Zoo? = null
        for (i in 0..99) {
            zoo1 = gameService.buy(zoo1Id, animal)
        }
        assertEquals(100, zoo1!!.buildings[0].number)
        assertEquals(0, zoo1.perks.size)
        assertEquals(3, zoo1.availablePerks.size)
        assertEquals(100.0, zoo1.baseIncome, 0.0001)
        assertEquals(100.0, zoo1.getMoneyIncome(), 0.0001)

        zoo1 = gameService.buyPerk(zoo1Id, resourcesService.perkList[0].name)
        assertEquals(100, zoo1.buildings[0].number)
        assertEquals(1, zoo1.perks.size)
        assertEquals(100.0, zoo1.baseIncome, 0.0001)
        assertEquals(150.0, zoo1.getMoneyIncome(), 0.0001)
        assertEquals(2, zoo1.availablePerks.size)
    }
}
