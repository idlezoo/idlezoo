package idlezoo.game.domain;

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@JsonTest
class ZooBuildingsTest(@Autowired val mapper: ObjectMapper) {

    @Test
    fun testWrite() {
        val zooBuildings = createZooBuildings()

        assertEquals(ZOO_BUILDINGS, mapper.writeValueAsString(zooBuildings))
    }

    companion object {

        val ZOO_BUILDINGS = "{\"building\":{\"baseCost\":1.0,\"baseIncome\":1.0,\"baseUpgrade\":1.0,\"name\":\"test\"},\"level\":1,\"lost\":1,\"number\":1,\"income\":2.0,\"name\":\"test\",\"nextCost\":1.15,\"upgradeCost\":2.0}"

        fun createZooBuildings(): ZooBuildings = ZooBuildings(1, 1, 1, Building(1.0, 1.0, 1.0, "test"))

    }
}