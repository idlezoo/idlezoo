package idlezoo.game.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@JsonTest
class ZooBuildingsTest {
    private final ObjectMapper mapper;

    @Autowired
    ZooBuildingsTest(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    static final String ZOO_BUILDINGS = "{\"building\":{\"baseCost\":1.0,\"baseIncome\":1.0,\"baseUpgrade\":1.0,\"name\":\"test\"},\"income\":2.0,\"level\":1,\"lost\":1,\"name\":\"test\",\"nextCost\":1.15,\"number\":1,\"upgradeCost\":2.0}";

    @Test
    void testWrite() throws JsonProcessingException {
        ZooBuildings zooBuildings = createZooBuildings();

        assertEquals(ZOO_BUILDINGS, mapper.writeValueAsString(zooBuildings));
    }

    static ZooBuildings createZooBuildings() {
        ImmutableBuilding building = ImmutableBuilding.builder()
                .name("test")
                .baseCost(1.0)
                .baseIncome(1.0)
                .baseUpgrade(1.0)
                .build();

        return ImmutableZooBuildings.builder()
                .building(building)
                .name("test")
                .level(1)
                .number(1)
                .lost(1)
                .build();
    }
}