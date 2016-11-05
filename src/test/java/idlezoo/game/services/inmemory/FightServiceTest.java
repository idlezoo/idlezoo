package idlezoo.game.services.inmemory;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import idlezoo.game.services.AbstractFightServiceTest;


public class FightServiceTest extends AbstractFightServiceTest {

  @Autowired
  private Storage storage;

  @Before
  public void setup() {
    assertTrue(gameService.createZoo(ZOO1));
    assertTrue(gameService.createZoo(ZOO2));
    storage.getZoo(ZOO1).setMoney(1000000);
    storage.getZoo(ZOO2).setMoney(1000000);
  }

  @After
  public void tearDown() {
    storage.getZoos().clear();
  }
}
