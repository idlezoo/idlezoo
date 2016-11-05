package idlezoo.game.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlezoo.game.domain.Zoo;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AbstractFightServiceTest {

  protected static final String ZOO1 = "1";
  protected static final String ZOO2 = "2";

  @Autowired
  protected FightService fightService;

  @Autowired
  protected GameService gameService;

  @Autowired
  protected ResourcesService resourcesService;

  @Test
  public void test1vs0() {
    gameService.buy(ZOO1, resourcesService.firstName());
    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(1, zoo1.getFightWins());
    assertEquals(1, zoo1.getBuildings().get(0).getNumber());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(0, zoo2.getFightWins());
  }

  @Test
  public void test1vs1() {
    gameService.buy(ZOO1, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.firstName());

    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(1, zoo1.getFightWins());
    assertEquals(0, zoo1.getBuildings().get(0).getNumber());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(0, zoo2.getFightWins());
  }

  @Test
  public void test0vs1() {
    gameService.buy(ZOO2, resourcesService.firstName());
    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(0, zoo1.getFightWins());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(1, zoo2.getBuildings().get(0).getNumber());
    assertEquals(1, zoo2.getFightWins());
  }

  @Test
  public void test1vs2() {
    gameService.buy(ZOO1, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.firstName());

    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(0, zoo1.getFightWins());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(1, zoo2.getBuildings().get(0).getNumber());
    assertEquals(1, zoo2.getFightWins());
  }

  @Test
  public void test1and1vs1and2() {
    gameService.buy(ZOO1, resourcesService.firstName());
    gameService.buy(ZOO1, resourcesService.secondName());

    gameService.buy(ZOO2, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.secondName());
    gameService.buy(ZOO2, resourcesService.secondName());

    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(1, zoo1.getFightWins());
    assertEquals(0, zoo1.getBuildings().get(0).getNumber());
    assertEquals(0, zoo1.getBuildings().get(1).getNumber());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(0, zoo2.getBuildings().get(0).getNumber());
    assertEquals(1, zoo2.getBuildings().get(1).getNumber());
    assertEquals(0, zoo2.getFightWins());
  }
}
