package idlezoo.game.services.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;
import idlezoo.game.services.FightService.Outcome;
import idlezoo.game.services.FightService.OutcomeContainer;
import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FightServiceInMemoryTest {
	private static final String ZOO1 = "1", ZOO2 = "2";
	private int zoo1id, zoo2id;

	@Autowired
	private FightService fightService;

	@Autowired
	private GameService gameService;

	@Autowired
	private UsersService usersService;

	@Autowired
	private ResourcesService resourcesService;

	@Autowired
	private Storage storage;

	@Before
	public void setup() {
		assertTrue(usersService.addUser(ZOO1, ""));
		assertTrue(usersService.addUser(ZOO2, ""));
		zoo1id = storage.getZoo(ZOO1).getId();
		zoo2id = storage.getZoo(ZOO2).getId();
		storage.getZoo(ZOO1).setMoney(1000000);
		storage.getZoo(ZOO2).setMoney(1000000);
	}

	@After
	public void tearDown() {
		storage.clear();
	}

	@Test
	public void isWaitingAfterStart() {
		assertEquals(Outcome.WAITING, fightService.fight(zoo1id).getOutcome());
		assertTrue(gameService.getZoo(zoo1id).isWaitingForFight());
	}

	@Test
	public void test1vs0() {
		gameService.buy(zoo1id, resourcesService.firstName());
		assertEquals(Outcome.WAITING, fightService.fight(zoo1id).getOutcome());
		OutcomeContainer fightOutcome = fightService.fight(zoo2id);
		assertEquals(Outcome.LOSS, fightOutcome.getOutcome());
		assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(1, zoo1.getFightWins());
		assertEquals(1, zoo1.getBuildings().get(0).getNumber());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(0, zoo2.getFightWins());
	}

	@Test
	public void test1vs1() {
		gameService.buy(zoo1id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.firstName());

		assertEquals(Outcome.WAITING, fightService.fight(zoo1id).getOutcome());
		OutcomeContainer fightOutcome = fightService.fight(zoo2id);
		assertEquals(Outcome.LOSS, fightOutcome.getOutcome());
		assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(1, zoo1.getFightWins());
		assertEquals(0, zoo1.getBuildings().get(0).getNumber());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(0, zoo2.getFightWins());
	}

	@Test
	public void test0vs1() {
		gameService.buy(zoo2id, resourcesService.firstName());
		
		assertEquals(Outcome.WAITING, fightService.fight(zoo1id).getOutcome());
		OutcomeContainer fightOutcome = fightService.fight(zoo2id);
		assertEquals(Outcome.WIN, fightOutcome.getOutcome());
		assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());
		
		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(0, zoo1.getFightWins());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(1, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getFightWins());
	}

	@Test
	public void test1vs2() {
		gameService.buy(zoo1id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.firstName());

		assertEquals(Outcome.WAITING, fightService.fight(zoo1id).getOutcome());
		OutcomeContainer fightOutcome = fightService.fight(zoo2id);
		assertEquals(Outcome.WIN, fightOutcome.getOutcome());
		assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(0, zoo1.getFightWins());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(1, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getFightWins());
	}

	@Test
	public void test1and1vs1and2() {
		gameService.buy(zoo1id, resourcesService.firstName());
		gameService.buy(zoo1id, resourcesService.secondName());

		gameService.buy(zoo2id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.secondName());
		gameService.buy(zoo2id, resourcesService.secondName());

		
		assertEquals(Outcome.WAITING, fightService.fight(zoo1id).getOutcome());
		OutcomeContainer fightOutcome = fightService.fight(zoo2id);
		assertEquals(Outcome.LOSS, fightOutcome.getOutcome());
		assertEquals(ZOO1, fightOutcome.getWaitingFighter().getName());
		
		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(1, zoo1.getFightWins());
		assertEquals(0, zoo1.getBuildings().get(0).getNumber());
		assertEquals(0, zoo1.getBuildings().get(1).getNumber());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(0, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getBuildings().get(1).getNumber());
		assertEquals(0, zoo2.getFightWins());
	}

}
