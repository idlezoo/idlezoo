package idlezoo.game.services.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.Zoo;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;
import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles({"postgres", "local"})
public class GameServicePostgresTest {
  private static final String ZOO1 = "1";
  
  @Autowired
  private GameService gameService;

  @Autowired
  private UsersService usersService;
  
  @Autowired
  private ResourcesService resourcesService;
  
  @Autowired
  private JdbcTemplate template;

  @Before
  public void setup() {
    assertTrue(usersService.addUser(ZOO1, ""));

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
  public void testBuy(){
    String animalType = resourcesService.firstName();
    Zoo zoo1 = gameService.buy(ZOO1, animalType);
    Building type = resourcesService.type(animalType);
    double moneyAfterBuy = resourcesService.startingMoney() - type.buildCost(0);
    assertEquals(moneyAfterBuy, zoo1.getMoney(), 0.0001);
    assertEquals(2, zoo1.getBuildings().size());
    assertEquals(1, zoo1.getBuildings().get(0).getNumber());
    assertEquals(0, zoo1.getBuildings().get(1).getNumber());
    assertEquals(type.income(0), zoo1.getMoneyIncome(), 0.0001);
  }
  
  @Test
  public void testUpgrade(){
    template.update("update users set money=100 where username=?", ZOO1);

    String animalType = resourcesService.firstName();
    Zoo zoo1 = gameService.upgrade(ZOO1, animalType);
    Building type = resourcesService.type(animalType);
    double moneyAfterUpgrade = 100 - type.upgradeCost(0);
    assertEquals(moneyAfterUpgrade, zoo1.getMoney(), 0.0001);
    assertEquals(1, zoo1.getBuildings().size());
    assertEquals(1, zoo1.getBuildings().get(0).getLevel());
  }
  

}
