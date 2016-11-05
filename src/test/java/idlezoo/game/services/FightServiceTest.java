package idlezoo.game.services;

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

@RunWith(SpringRunner.class)
@SpringBootTest
public class FightServiceTest {

	@Autowired
	private FightService fightService;

	@Autowired
	private GameService gameService;

	@Autowired
	private ResourcesService resourcesService;

	private Zoo zoo1, zoo2;

	@Before
	public void setup() {
		assertTrue(gameService.createZoo("1"));
		assertTrue(gameService.createZoo("2"));
		zoo1 = gameService.getZoo("1");
		zoo1.setMoney(1000000);
		zoo2 = gameService.getZoo("2");
		zoo2.setMoney(1000000);
	}

	@After
	public void tearDown() {
		gameService.getZoos().clear();
	}

	@Test
	public void test1vs0() {
		zoo1.buy(resourcesService.startingAnimal().getName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(1, zoo1.getFightWins());
		assertEquals(1, zoo1.getBuildings().get(0).getNumber());
		assertEquals(0, zoo2.getFightWins());
	}

	@Test
	public void test1vs1() {
		zoo1.buy(resourcesService.startingAnimal().getName(), resourcesService);
		zoo2.buy(resourcesService.startingAnimal().getName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(1, zoo1.getFightWins());
		assertEquals(0, zoo1.getBuildings().get(0).getNumber());
		assertEquals(0, zoo2.getFightWins());
	}

	@Test
	public void test0vs1() {
		zoo2.buy(resourcesService.startingAnimal().getName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(0, zoo1.getFightWins());
		assertEquals(1, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getFightWins());
	}
	
	@Test
	public void test1vs2() {
		zoo1.buy(resourcesService.firstName(), resourcesService);
		zoo2.buy(resourcesService.firstName(), resourcesService);
		zoo2.buy(resourcesService.firstName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(0, zoo1.getFightWins());
		assertEquals(1, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getFightWins());
	}
	
	
	@Test
	public void test1and1vs1and2() {
		zoo1.buy(resourcesService.firstName(), resourcesService);
		zoo1.buy(resourcesService.secondName(), resourcesService);
		zoo2.buy(resourcesService.firstName(), resourcesService);
		zoo2.buy(resourcesService.secondName(), resourcesService);
		zoo2.buy(resourcesService.secondName(), resourcesService);
		
		
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(1, zoo1.getFightWins());
		assertEquals(0, zoo1.getBuildings().get(0).getNumber());
		assertEquals(0, zoo1.getBuildings().get(1).getNumber());
		assertEquals(0, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getBuildings().get(1).getNumber());
		assertEquals(0, zoo2.getFightWins());
	}
	
	

}
