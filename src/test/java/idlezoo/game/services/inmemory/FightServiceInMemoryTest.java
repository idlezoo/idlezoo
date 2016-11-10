package idlezoo.game.services.inmemory;

import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

import idlezoo.game.services.AbstractFightServiceTest;

public class FightServiceInMemoryTest extends AbstractFightServiceTest {

	@Autowired
	private Storage storage;

	@Override
	public void setup() {
		super.setup();
		storage.getZoo(ZOO1).setMoney(1000000);
		storage.getZoo(ZOO2).setMoney(1000000);
	}

	@After
	public void tearDown() {
		storage.getZoos().clear();
	}
}
