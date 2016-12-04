package idlezoo.game.services.inmemory;

import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

import idlezoo.game.services.AbstractGameServiceTest;

public class GameServiceInMemoryTest extends AbstractGameServiceTest {

  @Autowired
  private Storage storage;

  @After
  public void tearDown() {
    storage.getZoos().clear();
  }

  @Override
  protected int getZooId(String zooName) {
    return storage.getZoo(zooName).getId();
  }

  @Override
  protected void setMoney(int zooId, double value) {
    storage.getZoo(zoo1Id).setMoney(value);
  }

}
