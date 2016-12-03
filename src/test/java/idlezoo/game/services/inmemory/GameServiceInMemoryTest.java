package idlezoo.game.services.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.Zoo;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;
import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameServiceInMemoryTest {

	private static final String ZOO1 = "1";
	private Integer zoo1Id;

	@Autowired
	private GameService gameService;

	@Autowired
	private UsersService usersService;

	@Autowired
	protected ResourcesService resourcesService;

	@Autowired
	private Storage storage;

	@Before
	public void setup() {
		assertTrue(usersService.addUser(ZOO1, ""));
		zoo1Id = storage.getZoo(ZOO1).getId();

	}

	@After
	public void tearDown() {
		storage.getZoos().clear();
	}

	@Test
	public void testGetZoo() {
		Zoo zoo1 = gameService.getZoo(zoo1Id);
		assertEquals(0, zoo1.getFightWins());
		assertEquals(1, zoo1.getBuildings().size());
		assertEquals(0, zoo1.getBuildings().get(0).getNumber());
		assertFalse(zoo1.isWaitingForFight());
		assertEquals(0, zoo1.getMoneyIncome(), 0.0001);
		assertEquals(50, zoo1.getMoney(), 0.0001);
	}

	@Test
	public void testBuy() {
		String animalType = resourcesService.firstName();
		Zoo zoo1 = gameService.buy(zoo1Id, animalType);
		Building type = resourcesService.type(animalType);
		double moneyAfterBuy = resourcesService.startingMoney() - type.buildCost(0);
		assertEquals(moneyAfterBuy, zoo1.getMoney(), 0.0001);
		assertEquals(2, zoo1.getBuildings().size());
		assertEquals(1, zoo1.getBuildings().get(0).getNumber());
		assertEquals(0, zoo1.getBuildings().get(1).getNumber());
		assertEquals(type.income(0), zoo1.getMoneyIncome(), 0.0001);
	}

	@Test
	public void testUpgrade() {
		storage.getZoo(zoo1Id).setMoney(100);

		String animalType = resourcesService.firstName();
		Zoo zoo1 = gameService.upgrade(zoo1Id, animalType);
		Building type = resourcesService.type(animalType);
		double moneyAfterUpgrade = 100 - type.upgradeCost(0);
		assertEquals(moneyAfterUpgrade, zoo1.getMoney(), 0.0001);
		assertEquals(1, zoo1.getBuildings().size());
		assertEquals(1, zoo1.getBuildings().get(0).getLevel());
	}
	

  @Test
  public void testBuyPerk() {
    storage.getZoo(zoo1Id).setMoney(1000_000_000);
    String animal = resourcesService.firstName();
    Zoo zoo1 = null;
    for (int i = 0; i < 100; i++) {
      zoo1 = gameService.buy(zoo1Id, animal);
    }
    assertEquals(100, zoo1.getBuildings().get(0).getNumber());
    assertEquals(0, zoo1.getPerks().size());
    assertEquals(1, zoo1.getAvailablePerks().size());
    assertEquals(100, zoo1.getBaseIncome(), 0.0001);
    assertEquals(100, zoo1.getMoneyIncome(), 0.0001);
    
    zoo1 = gameService.buyPerk(zoo1Id, resourcesService.getPerkList().get(0).getName());
    assertEquals(100, zoo1.getBuildings().get(0).getNumber());
    assertEquals(1, zoo1.getPerks().size());
    assertEquals(100, zoo1.getBaseIncome(), 0.0001);
    assertEquals(150, zoo1.getMoneyIncome(), 0.0001);
    assertEquals(0, zoo1.getAvailablePerks().size());
  }

}
