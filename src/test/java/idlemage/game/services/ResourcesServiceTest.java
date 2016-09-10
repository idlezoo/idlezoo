package idlemage.game.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlemage.game.domain.Building;
import idlemage.game.services.ResourcesService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResourcesServiceTest {

	@Autowired
	private ResourcesService gameResources;

	@Test
	public void test() {
		assertNotNull(gameResources.startingCreature());
		assertSame(gameResources.startingCreature(), gameResources.getCreaturesList().get(0));
		Building previous = gameResources.startingCreature();
		for (Building creature : gameResources.getCreaturesList()) {
			if (creature == previous) {
				continue;
			}
			assertTrue(previous.getBaseCost() < creature.getBaseCost());
			assertTrue(previous.getBaseIncome() < creature.getBaseIncome());
			assertTrue(previous.getBaseUpgrade() < creature.getBaseUpgrade());
			previous = creature;
		}
	}
}
