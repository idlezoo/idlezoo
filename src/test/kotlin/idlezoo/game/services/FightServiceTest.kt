package idlezoo.game.services

import idlezoo.game.services.FightService.Outcome
import idlezoo.security.UsersService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class FightServiceTest @Autowired constructor(template: JdbcTemplate, usersService: UsersService, resourcesService: ResourcesService,
                                              val fightService: FightService, val gameService: GameService) : AbstractServiceTest(template, usersService, resourcesService) {
    private var zoo2Id: Int = 0

    @BeforeEach
    override fun setup() {
        assertTrue(usersService.addUser(AbstractServiceTest.ZOO1, ""))
        assertTrue(usersService.addUser(ZOO2, ""))
        zoo1Id = getZooId(AbstractServiceTest.ZOO1)
        zoo2Id = getZooId(ZOO2)

        setMoney(zoo1Id, 1000000.0)
        setMoney(zoo2Id, 1000000.0)

        template.update("update arena set waiting_user_id=null")
    }


    @Test
    fun isWaitingAfterStart() {
        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).outcome)
        assertTrue(gameService.getZoo(zoo1Id).waitingForFight)
    }

    @Test
    fun test1vs0() {
        gameService.buy(zoo1Id, resourcesService.firstName())
        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(Outcome.LOSS, fightOutcome.outcome)
        assertEquals(AbstractServiceTest.ZOO1, fightOutcome.waitingFighter!!.name)

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

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(Outcome.LOSS, fightOutcome.outcome)
        assertEquals(AbstractServiceTest.ZOO1, fightOutcome.waitingFighter!!.name)

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

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(Outcome.WIN, fightOutcome.outcome)
        assertEquals(AbstractServiceTest.ZOO1, fightOutcome.waitingFighter!!.name)

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

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(Outcome.WIN, fightOutcome.outcome)
        assertEquals(AbstractServiceTest.ZOO1, fightOutcome.waitingFighter!!.name)

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

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).outcome)
        val fightOutcome = fightService.fight(zoo2Id)
        assertEquals(Outcome.LOSS, fightOutcome.outcome)
        assertEquals(AbstractServiceTest.ZOO1, fightOutcome.waitingFighter!!.name)

        val zoo1 = gameService.getZoo(zoo1Id)
        assertEquals(1, zoo1.fightWins)
        assertEquals(0, zoo1.buildings[0].number)
        assertEquals(0, zoo1.buildings[1].number)

        val zoo2 = gameService.getZoo(zoo2Id)
        assertEquals(0, zoo2.buildings[0].number)
        assertEquals(1, zoo2.buildings[1].number)
        assertEquals(0, zoo2.fightWins)
    }

    companion object {
        private const val ZOO2 = "2"
    }

}
