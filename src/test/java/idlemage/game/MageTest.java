package idlemage.game;

import static idlemage.game.GameResources.STARTING_BUILDING;
import static idlemage.game.GameResources.STARTING_MANA;
import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

import idlemage.game.Mage.Timer;

public class MageTest {

	@Test
	public void test() {
		TestTimer timer = new TestTimer(LocalDateTime.now());
		Mage mage = new Mage(timer);
		assertEquals(STARTING_MANA, mage.getMana(), 0.0001);
		assertEquals(0D, mage.getIncome(), 0.0001);

		mage.buy(STARTING_BUILDING.getName());
		double mana = STARTING_MANA - STARTING_BUILDING.buildCost(0);
		assertEquals(mana, mage.getMana(), 0.0001);
		assertEquals(STARTING_BUILDING.income(0), mage.getIncome(), 0.0001);

		mage.buy(STARTING_BUILDING.getName());
		mana -= STARTING_BUILDING.buildCost(1);
		assertEquals(mana, mage.getMana(), 0.0001);
		assertEquals(2 * STARTING_BUILDING.income(0), mage.getIncome(), 0.0001);

		mana += mage.getIncome() * 10;
		timer.now(timer.now().plusSeconds(10));
		mage.updateMana();
		assertEquals(mana, mage.getMana(), 0.0001);

	}

	private static class TestTimer implements Timer {
		private LocalDateTime testNow;

		public TestTimer(LocalDateTime testNow) {
			this.testNow = testNow;
		}

		public void now(LocalDateTime testNow) {
			this.testNow = testNow;
		}

		@Override
		public LocalDateTime now() {
			return testNow;
		}

	}

}
