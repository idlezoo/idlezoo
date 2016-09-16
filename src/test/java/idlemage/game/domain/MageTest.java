package idlemage.game.domain;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlemage.game.domain.Mage;
import idlemage.game.domain.Mage.Timer;
import idlemage.game.services.ResourcesService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MageTest {

	@Autowired
	private ResourcesService gameResources;
	
	@Test
	public void test() {
		TestTimer timer = new TestTimer(0L);
		Mage mage = new Mage("testmage", gameResources, timer);
		assertEquals(gameResources.startingMana(), mage.getMana(), 0.0001);
		assertEquals(0D, mage.getIncome(), 0.0001);

		mage.buy(gameResources.startingCreature().getName(), gameResources);
		double mana = gameResources.startingMana() - gameResources.startingCreature().buildCost(0);
		assertEquals(mana, mage.getMana(), 0.0001);
		assertEquals(gameResources.startingCreature().income(0), mage.getIncome(), 0.0001);

		mage.buy(gameResources.startingCreature().getName(), gameResources);
		mana -= gameResources.startingCreature().buildCost(1);
		assertEquals(mana, mage.getMana(), 0.0001);
		assertEquals(2 * gameResources.startingCreature().income(0), mage.getIncome(), 0.0001);

		mana += mage.getIncome() * 10;
		timer.now(10L);
		mage.updateMana();
		assertEquals(mana, mage.getMana(), 0.0001);

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
