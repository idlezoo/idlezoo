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
import idlezoo.game.services.AbstractGameServiceTest;
import idlezoo.game.services.GameService;
import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameServiceInMemoryTest extends AbstractGameServiceTest {

  @Autowired
  protected GameService gameService;

  @Autowired
  protected UsersService usersService;

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
  }

  @Autowired
  private Storage storage;

  @After
  public void tearDown() {
    storage.getZoos().clear();
  }

}
