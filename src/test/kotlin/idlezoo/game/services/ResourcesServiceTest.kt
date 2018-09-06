package idlezoo.game.services


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = NONE)
class ResourcesServiceTest(@Autowired val gameResources: ResourcesService) {

    @Test
    fun testAnimals() {
        assertNotNull(gameResources.startingAnimal())
        assertSame(gameResources.startingAnimal(), gameResources.animalsList[0])
        var previous = gameResources.startingAnimal()
        for (creature in gameResources.animalsList) {
            if (creature === previous) {
                continue
            }
            assertTrue(previous!!.baseCost < creature.baseCost)
            assertTrue(previous.baseIncome < creature.baseIncome)
            assertTrue(previous.baseUpgrade < creature.baseUpgrade)
            previous = creature
        }
    }

    @Test
    fun testPerks() {
        assertEquals("TODO100", gameResources.perkList[0].name)
        assertEquals(1e7, gameResources.perkList[0].cost, 0.0001)

        for (perk in gameResources.perkList) {
            val index = gameResources.perkIndex(perk.name)
            assertEquals(perk, gameResources.perkByIndex(index!!))
            assertEquals(perk, gameResources.perk(perk.name))
        }
    }
}
