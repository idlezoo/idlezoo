package idlezoo.game.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@JsonTest
class ZooTest {
    private final ObjectMapper mapper;

    @Autowired
    ZooTest(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private static final String ZOO = "{\"availablePerks\":[],\"baseIncome\":1.0,\"buildings\":["
            + ZooBuildingsTest.ZOO_BUILDINGS
            + "],\"championTime\":1,\"fightLosses\":1,\"fightWins\":1,\"money\":1.0,\"moneyIncome\":2.0,\"name\":\"test\",\"perkIncome\":1.0,\"perks\":[],\"waitingForFight\":true}";

    @Test
    void testWrite() throws JsonProcessingException {
        Zoo zoo = ImmutableZoo.builder()
                .name("test")
                .money(1.0)
                .baseIncome(1.0)
                .perkIncome(1.0)
                .fightWins(1)
                .fightLosses(1)
                .isWaitingForFight(true)
                .championTime(1)
                .addBuildings(ZooBuildingsTest.createZooBuildings())
                .build();

        assertEquals(ZOO, mapper.writeValueAsString(zoo));


    }

}