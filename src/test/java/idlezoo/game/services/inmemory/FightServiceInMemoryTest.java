package idlezoo.game.services.inmemory;

import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

import idlezoo.game.services.AbstractFightServiceTest;


public class FightServiceInMemoryTest extends AbstractFightServiceTest{

	@Autowired
	private Storage storage;

	@After
	public void tearDown() {
		storage.clear();
	}

  @Override
  protected int getZooId(String zooName) {
    return storage.getZoo(zooName).getId();
  }

  @Override
  protected void setMoney(int zooId, double value) {
    storage.getZoo(zooId).setMoney(value);
  }

}
