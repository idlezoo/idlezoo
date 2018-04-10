package idlezoo.game.services;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService.Outcome;
import idlezoo.game.services.FightService.OutcomeContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

 class FightServiceTest extends AbstractServiceTest {
    private static final String ZOO2 = "2";
    private int zoo2Id;

    @Autowired
    private FightService fightService;
    @Autowired
    private GameService gameService;

    @BeforeEach
    @Override
    public void setup() {
        assertTrue(usersService.addUser(ZOO1, ""));
        assertTrue(usersService.addUser(ZOO2, ""));
        zoo1Id = getZooId(ZOO1);
        zoo2Id = getZooId(ZOO2);

        setMoney(zoo1Id, 1000_000);
        setMoney(zoo2Id, 1000_000);

        template.update("update arena set waiting_user_id=null");
    }


    @Test
    void isWaitingAfterStart() {
        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).getOutcome());
        assertTrue(gameService.getZoo(zoo1Id).isWaitingForFight());
    }

    @Test
    void test1vs0() {
        gameService.buy(zoo1Id, resourcesService.firstName());
        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).getOutcome());
        OutcomeContainer fightOutcome = fightService.fight(zoo2Id);
        assertEquals(Outcome.LOSS, fightOutcome.getOutcome());
        assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

        Zoo zoo1 = gameService.getZoo(zoo1Id);
        assertEquals(1, zoo1.getFightWins());
        assertEquals(1, zoo1.getBuildings().get(0).getNumber());

        Zoo zoo2 = gameService.getZoo(zoo2Id);
        assertEquals(0, zoo2.getFightWins());
    }

    @Test
    void test1vs1() {
        gameService.buy(zoo1Id, resourcesService.firstName());
        gameService.buy(zoo2Id, resourcesService.firstName());

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).getOutcome());
        OutcomeContainer fightOutcome = fightService.fight(zoo2Id);
        assertEquals(Outcome.LOSS, fightOutcome.getOutcome());
        assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

        Zoo zoo1 = gameService.getZoo(zoo1Id);
        assertEquals(1, zoo1.getFightWins());
        assertEquals(0, zoo1.getBuildings().get(0).getNumber());
        assertEquals(1, zoo1.getBuildings().get(0).getLost());

        Zoo zoo2 = gameService.getZoo(zoo2Id);
        assertEquals(0, zoo2.getFightWins());
        assertEquals(0, zoo2.getBuildings().get(0).getNumber());
        assertEquals(1, zoo2.getBuildings().get(0).getLost());
    }

    @Test
    void test0vs1() {
        gameService.buy(zoo2Id, resourcesService.firstName());

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).getOutcome());
        OutcomeContainer fightOutcome = fightService.fight(zoo2Id);
        assertEquals(Outcome.WIN, fightOutcome.getOutcome());
        assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

        Zoo zoo1 = gameService.getZoo(zoo1Id);
        assertEquals(0, zoo1.getFightWins());

        Zoo zoo2 = gameService.getZoo(zoo2Id);
        assertEquals(1, zoo2.getBuildings().get(0).getNumber());
        assertEquals(1, zoo2.getFightWins());
    }

    @Test
    void test1vs2() {
        gameService.buy(zoo1Id, resourcesService.firstName());
        gameService.buy(zoo2Id, resourcesService.firstName());
        gameService.buy(zoo2Id, resourcesService.firstName());

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).getOutcome());
        OutcomeContainer fightOutcome = fightService.fight(zoo2Id);
        assertEquals(Outcome.WIN, fightOutcome.getOutcome());
        assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

        Zoo zoo1 = gameService.getZoo(zoo1Id);
        assertEquals(0, zoo1.getFightWins());

        Zoo zoo2 = gameService.getZoo(zoo2Id);
        assertEquals(1, zoo2.getBuildings().get(0).getNumber());
        assertEquals(1, zoo2.getFightWins());
    }

    @Test
    void test1and1vs1and2() {
        gameService.buy(zoo1Id, resourcesService.firstName());
        gameService.buy(zoo1Id, resourcesService.secondName());

        gameService.buy(zoo2Id, resourcesService.firstName());
        gameService.buy(zoo2Id, resourcesService.secondName());
        gameService.buy(zoo2Id, resourcesService.secondName());

        assertEquals(Outcome.WAITING, fightService.fight(zoo1Id).getOutcome());
        OutcomeContainer fightOutcome = fightService.fight(zoo2Id);
        assertEquals(Outcome.LOSS, fightOutcome.getOutcome());
        assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

        Zoo zoo1 = gameService.getZoo(zoo1Id);
        assertEquals(1, zoo1.getFightWins());
        assertEquals(0, zoo1.getBuildings().get(0).getNumber());
        assertEquals(0, zoo1.getBuildings().get(1).getNumber());

        Zoo zoo2 = gameService.getZoo(zoo2Id);
        assertEquals(0, zoo2.getBuildings().get(0).getNumber());
        assertEquals(1, zoo2.getBuildings().get(1).getNumber());
        assertEquals(0, zoo2.getFightWins());
    }

}
