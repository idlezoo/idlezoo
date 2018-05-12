package idlezoo.game.services;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = NONE)
@Transactional
class TopServiceTest {
    @Autowired
    private TopService topService;
    @Autowired
    private ResourcesService resourcesService;

    @Test
    void testBuilding() {
        String startingAnimal = resourcesService.startingAnimal().getName();
        assertEquals(0, topService.building(startingAnimal).size());
    }

    @Test
    void testChampionTime() {
        assertEquals(0, topService.championTime().size());
    }

    @Test
    void testIncome() {
        assertEquals(0, topService.income().size());
    }

    @Test
    void testWins() {
        assertEquals(0, topService.wins().size());
    }

    @Test
    void testLosses() {
        assertEquals(0, topService.losses().size());
    }
}
