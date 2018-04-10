package idlezoo.game.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class TopServiceTest {

  @Autowired
  private TopService topService;

  @Autowired
  private ResourcesService resourcesService;

  @Test
  public void testBuilding() {
    String startingAnimal = resourcesService.startingAnimal().getName();
    assertEquals(0, topService.building(startingAnimal).size());
  }

  @Test
  public void testChampionTime() {
    assertEquals(0, topService.championTime().size());
  }

  @Test
  public void testIncome() {
    assertEquals(0, topService.income().size());
  }

  @Test
  public void testWins() {
    assertEquals(0, topService.wins().size());
  }

  @Test
  public void testLosses() {
    assertEquals(0, topService.losses().size());
  }

}
