package idlemage.game.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlemage.game.domain.Mage;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FightServiceTest {

	@Autowired
	private FightService fightService;

	@Autowired
	private GameService gameService;

	@Autowired
	private ResourcesService resourcesService;

	private Mage mage1, mage2;

	@Before
	public void setup() {
		assertTrue(gameService.createMage("1"));
		assertTrue(gameService.createMage("2"));
		mage1 = gameService.getMage("1");
		mage1.setMana(1000000);
		mage2 = gameService.getMage("2");
		mage2.setMana(1000000);
	}

	@After
	public void tearDown() {
		gameService.getMages().clear();
	}

	@Test
	public void test1vs0() {
		mage1.buy(resourcesService.startingCreature().getName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(1, mage1.getFightWins());
		assertEquals(1, mage1.getBuildings().get(0).getNumber());
		assertEquals(0, mage2.getFightWins());
	}

	@Test
	public void test1vs1() {
		mage1.buy(resourcesService.startingCreature().getName(), resourcesService);
		mage2.buy(resourcesService.startingCreature().getName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(1, mage1.getFightWins());
		assertEquals(0, mage1.getBuildings().get(0).getNumber());
		assertEquals(0, mage2.getFightWins());
	}

	@Test
	public void test0vs1() {
		mage2.buy(resourcesService.startingCreature().getName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(0, mage1.getFightWins());
		assertEquals(1, mage2.getBuildings().get(0).getNumber());
		assertEquals(1, mage2.getFightWins());
	}
	
	@Test
	public void test1vs2() {
		mage1.buy(resourcesService.firstName(), resourcesService);
		mage2.buy(resourcesService.firstName(), resourcesService);
		mage2.buy(resourcesService.firstName(), resourcesService);
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(0, mage1.getFightWins());
		assertEquals(1, mage2.getBuildings().get(0).getNumber());
		assertEquals(1, mage2.getFightWins());
	}
	
	
	@Test
	public void test1and1vs1and2() {
		mage1.buy(resourcesService.firstName(), resourcesService);
		mage1.buy(resourcesService.secondName(), resourcesService);
		mage2.buy(resourcesService.firstName(), resourcesService);
		mage2.buy(resourcesService.secondName(), resourcesService);
		mage2.buy(resourcesService.secondName(), resourcesService);
		
		
		fightService.fight("1");
		fightService.fight("2");

		assertEquals(1, mage1.getFightWins());
		assertEquals(0, mage1.getBuildings().get(0).getNumber());
		assertEquals(0, mage1.getBuildings().get(1).getNumber());
		assertEquals(0, mage2.getBuildings().get(0).getNumber());
		assertEquals(1, mage2.getBuildings().get(1).getNumber());
		assertEquals(0, mage2.getFightWins());
	}
	
	

}
