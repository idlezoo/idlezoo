package idlezoo.game.services.inmemory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlezoo.game.services.ResourcesService;
import idlezoo.game.services.inmemory.InMemoryZoo;
import idlezoo.game.services.inmemory.InMemoryZoo.Timer;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ZooTest {

	@Autowired
	private ResourcesService gameResources;
	
	@Test
	public void test() {
		TestTimer timer = new TestTimer(0L);
		InMemoryZoo zoo = new InMemoryZoo("testzoo", "", gameResources, timer);
		assertEquals(gameResources.startingMoney(), zoo.getMoney(), 0.0001);
		assertEquals(0D, zoo.getIncome(), 0.0001);

		zoo.buy(gameResources.startingAnimal().getName(), gameResources);
		double money = gameResources.startingMoney() - gameResources.startingAnimal().buildCost(0);
		assertEquals(money, zoo.getMoney(), 0.0001);
		assertEquals(gameResources.startingAnimal().income(0), zoo.getIncome(), 0.0001);

		zoo.buy(gameResources.startingAnimal().getName(), gameResources);
		money -= gameResources.startingAnimal().buildCost(1);
		assertEquals(money, zoo.getMoney(), 0.0001);
		assertEquals(2 * gameResources.startingAnimal().income(0), zoo.getIncome(), 0.0001);

		money += zoo.getIncome() * 10;
		timer.now(10L);
		zoo.updateMoney();
		assertEquals(money, zoo.getMoney(), 0.0001);

	}

	private static class TestTimer implements Timer {
		private long testNow;

		public TestTimer(long testNow) {
			this.testNow = testNow;
		}

		public void now(long testNow) {
			this.testNow = testNow;
		}

		@Override
		public long now() {
			return testNow;
		}

	}

}
