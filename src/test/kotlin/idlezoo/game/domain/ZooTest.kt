package idlezoo.game.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@JsonTest
class ZooTest(@Autowired val mapper: ObjectMapper) {

    @Test
    fun testWrite() {
        val zoo = Zoo(
                listOf(ZooBuildingsTest.createZooBuildings()),
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
        assertEquals(ZOO, mapper.writeValueAsString(zoo))
    }

    companion object {
        private val ZOO = ("{\"availablePerks\":[],\"baseIncome\":1.0,\"buildings\":["
                + ZooBuildingsTest.ZOO_BUILDINGS
                + "],\"championTime\":1,\"fightLosses\":1,\"fightWins\":1,\"money\":1.0,\"name\":\"test\",\"perkIncome\":1.0,\"perks\":[],\"waitingForFight\":true,\"moneyIncome\":2.0}")
    }
}