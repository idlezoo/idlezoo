package idlezoo.game.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlezoo.game.domain.Building;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResourcesServiceTest {

	@Autowired
	private ResourcesService gameResources;

	@Test
	public void testAnimals() {
		assertNotNull(gameResources.startingAnimal());
		assertSame(gameResources.startingAnimal(), gameResources.getAnimalsList().get(0));
		Building previous = gameResources.startingAnimal();
		for (Building creature : gameResources.getAnimalsList()) {
			if (creature == previous) {
				continue;
			}
			assertTrue(previous.getBaseCost() < creature.getBaseCost());
			assertTrue(previous.getBaseIncome() < creature.getBaseIncome());
			assertTrue(previous.getBaseUpgrade() < creature.getBaseUpgrade());
			previous = creature;
		}
	}

	@Test
	public void testPerks() throws Exception {

		assertEquals(9, gameResources.getPerkList().size());

	}

}
