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

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;
import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameServiceInMemoryTest {

  private static final String ZOO1 = "1";

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

  }

  @After
  public void tearDown() {
    storage.getZoos().clear();
  }


  @Test
  public void testGetZoo() {
    Zoo zoo1 = gameService.getZoo(ZOO1);
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
    Zoo zoo1 = gameService.buy(ZOO1, animalType);
    assertEquals(2, zoo1.getBuildings().size());
    assertEquals(1, zoo1.getBuildings().get(0).getNumber());
    assertEquals(0, zoo1.getBuildings().get(1).getNumber());
    assertEquals(resourcesService.type(animalType).income(0), zoo1.getMoneyIncome(), 0.0001);
  }

  @Test
  public void testUpgrade() {
    storage.getZoo(ZOO1).setMoney(100);
    Zoo zoo1 = gameService.upgrade(ZOO1, resourcesService.firstName());
    assertEquals(1, zoo1.getBuildings().size());
    assertEquals(1, zoo1.getBuildings().get(0).getLevel());
  }

}
