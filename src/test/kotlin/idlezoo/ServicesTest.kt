package idlezoo

import idlezoo.Outcome.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

const val ZOO1 = "1"
const val ZOO2 = "2"

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = NONE)
@Transactional
abstract class AbstractServiceTest(val template: JdbcTemplate, val usersService: UsersService, val resourcesService: ResourcesService) {
    var zoo1Id: Int = 0

    @BeforeEach
    fun setup() {
        assertTrue(usersService.addUser(ZOO1, ""))
        zoo1Id = getZooId(ZOO1)
    }

    fun getZooId(zooName: String): Int {
        return template.queryForObject("select id from users where username=?", Int::class.java, zooName)
    }

    fun setMoney(zooId: Int, value: Double) {
        template.update("update users set money=? where id=?", value, zooId)
    }
}


class FightServiceTest @Autowired constructor(template: JdbcTemplate, usersService: UsersService, resourcesService: ResourcesService,
                                              val fightService: FightService, val gameService: GameService) : AbstractServiceTest(template, usersService, resourcesService) {
    private var zoo2Id: Int = 0

    @BeforeEach
    override fun setup() {
        assertTrue(usersService.addUser(ZOO1, ""))
        assertTrue(usersService.addUser(ZOO2, ""))
        zoo1Id = getZooId(ZOO1)
        zoo2Id = getZooId(ZOO2)

        setMoney(zoo1Id, 1000000.0)
        setMoney(zoo2Id, 1000000.0)

        template.update("update arena set waiting_user_id=null")
    }


    @Test
    fun isWaitingAfterStart() {
        assertEquals(WAITING, fightService.fight(zoo1Id).outcome)
        assertTrue(gameService.getZoo(zoo1Id).waitingForFight)
    }

    @Test
    fun test1vs0() {
        gameService.buy(zoo1Id, resourcesService.firstName())
        assertEquals(WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(LOSS, fightOutcome.outcome)
        assertEquals(ZOO1, fightOutcome.waitingFighter!!.name)

        val zoo1 = gameService.getZoo(zoo1Id)
        assertEquals(1, zoo1.fightWins)
        assertEquals(1, zoo1.buildings[0].number)

        val zoo2 = gameService.getZoo(zoo2Id)
        assertEquals(0, zoo2.fightWins)
    }

    @Test
    fun test1vs1() {
        gameService.buy(zoo1Id, resourcesService.firstName())
        gameService.buy(zoo2Id, resourcesService.firstName())

        assertEquals(WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(LOSS, fightOutcome.outcome)
        assertEquals(ZOO1, fightOutcome.waitingFighter!!.name)

        val zoo1 = gameService.getZoo(zoo1Id)
        assertEquals(1, zoo1.fightWins)
        assertEquals(0, zoo1.buildings[0].number)
        assertEquals(1, zoo1.buildings[0].lost)

        val zoo2 = gameService.getZoo(zoo2Id)
        assertEquals(0, zoo2.fightWins)
        assertEquals(0, zoo2.buildings[0].number)
        assertEquals(1, zoo2.buildings[0].lost)
    }

    @Test
    fun test0vs1() {
        gameService.buy(zoo2Id, resourcesService.firstName())

        assertEquals(WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(WIN, fightOutcome.outcome)
        assertEquals(ZOO1, fightOutcome.waitingFighter!!.name)

        val zoo1 = gameService.getZoo(zoo1Id)
        assertEquals(0, zoo1.fightWins)

        val zoo2 = gameService.getZoo(zoo2Id)
        assertEquals(1, zoo2.buildings[0].number)
        assertEquals(1, zoo2.fightWins)
    }

    @Test
    fun test1vs2() {
        gameService.buy(zoo1Id, resourcesService.firstName())
        gameService.buy(zoo2Id, resourcesService.firstName())
        gameService.buy(zoo2Id, resourcesService.firstName())

        assertEquals(WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(WIN, fightOutcome.outcome)
        assertEquals(ZOO1, fightOutcome.waitingFighter!!.name)

        val zoo1 = gameService.getZoo(zoo1Id)
        assertEquals(0, zoo1.fightWins)

        val zoo2 = gameService.getZoo(zoo2Id)
        assertEquals(1, zoo2.buildings[0].number)
        assertEquals(1, zoo2.fightWins)
    }

    @Test
    fun test1and1vs1and2() {
        gameService.buy(zoo1Id, resourcesService.firstName())
        gameService.buy(zoo1Id, resourcesService.secondName())

        gameService.buy(zoo2Id, resourcesService.firstName())
        gameService.buy(zoo2Id, resourcesService.secondName())
        gameService.buy(zoo2Id, resourcesService.secondName())

        assertEquals(WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(LOSS, fightOutcome.outcome)
        assertEquals(ZOO1, fightOutcome.waitingFighter!!.name)

        val zoo1 = gameService.getZoo(zoo1Id)
        assertEquals(1, zoo1.fightWins)
        assertEquals(0, zoo1.buildings[0].number)
        assertEquals(0, zoo1.buildings[1].number)

        val zoo2 = gameService.getZoo(zoo2Id)
        assertEquals(0, zoo2.buildings[0].number)
        assertEquals(1, zoo2.buildings[1].number)
        assertEquals(0, zoo2.fightWins)
    }
}


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
        val moneyAfterBuy = STARTING_MONEY - type.buildCost(0)
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


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = NONE)
class ResourcesServiceTest(@Autowired val gameResources: ResourcesService) {

    @Test
    fun testAnimals() {
        assertNotNull(gameResources.startingAnimal())
        assertSame(gameResources.startingAnimal(), gameResources.animalsList[0])
        var previous = gameResources.startingAnimal()
        for (creature in gameResources.animalsList) {
            if (creature === previous) {
                continue
            }
            assertTrue(previous.baseCost < creature.baseCost)
            assertTrue(previous.baseIncome < creature.baseIncome)
            assertTrue(previous.baseUpgrade < creature.baseUpgrade)
            previous = creature
        }
    }

    @Test
    fun testPerks() {
        assertEquals("TODO100", gameResources.perkList[0].name)
        assertEquals(1e7, gameResources.perkList[0].cost, 0.0001)

        for (perk in gameResources.perkList) {
            val index = gameResources.perkIndex(perk.name)
            assertEquals(perk, gameResources.perkByIndex(index!!))
            assertEquals(perk, gameResources.perk(perk.name))
        }
    }
}


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = NONE)
@Transactional
class TopServiceTest(
        @Autowired val topService: TopService,
        @Autowired val resourcesService: ResourcesService
) {

    @Test
    fun testBuilding() {
        val startingAnimal = resourcesService.startingAnimal().name
        assertEquals(0, topService.building(startingAnimal).size)
    }

    @Test
    fun testChampionTime() {
        assertEquals(0, topService.championTime().size)
    }

    @Test
    fun testIncome() {
        assertEquals(0, topService.income().size)
    }

    @Test
    fun testWins() {
        assertEquals(0, topService.wins().size)
    }

    @Test
    fun testLosses() {
        assertEquals(0, topService.losses().size)
    }
}
