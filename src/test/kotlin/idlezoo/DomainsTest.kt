package idlezoo

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@JsonTest
class DomainsTest(@Autowired val mapper: ObjectMapper) {


    @Test
    fun testZooBuildings() {
        val zooBuildings = createZooBuildings()

        Assertions.assertEquals(ZOO_BUILDINGS, mapper.writeValueAsString(zooBuildings))
    }

    @Test
    fun testZoo() {
        val zoo = Zoo(
                listOf(createZooBuildings()),
                "test",
                1.0,
                1.0,
                listOf(),
                listOf(),
                1,
                1,
                true,
                1,
                1.0
        )
        Assertions.assertEquals(ZOO, mapper.writeValueAsString(zoo))
    }

    companion object {
        fun createZooBuildings(): ZooBuildings = ZooBuildings(1, 1, 1, Building(1.0, 1.0, 1.0, "test"))
        val ZOO_BUILDINGS = "{\"building\":{\"baseCost\":1.0,\"baseIncome\":1.0,\"baseUpgrade\":1.0,\"name\":\"test\"},\"level\":1,\"lost\":1,\"number\":1,\"income\":2.0,\"name\":\"test\",\"nextCost\":1.15,\"upgradeCost\":2.0}"
        val ZOO = ("{\"availablePerks\":[],\"baseIncome\":1.0,\"buildings\":["
                + ZOO_BUILDINGS
                + "],\"championTime\":1,\"fightLosses\":1,\"fightWins\":1,\"money\":1.0,\"name\":\"test\",\"perkIncome\":1.0,\"perks\":[],\"waitingForFight\":true,\"moneyIncome\":2.0}")
    }

}