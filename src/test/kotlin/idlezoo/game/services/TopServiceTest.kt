package idlezoo.game.services


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = NONE)
@Transactional
class TopServiceTest(
        @Autowired val topService: TopService,
        @Autowired val resourcesService: ResourcesService
) {

    @Test
    fun testBuilding() {
        val startingAnimal = resourcesService.startingAnimal()!!.name
        assertEquals(0, topService.building(startingAnimal).size)
    }

    @Test
    fun testChampionTime() {
        assertEquals(0, topService.championTime().size)
    }

    @Test
    fun testIncome() {
        assertEquals(0, topService.income().size)
    }

    @Test
    fun testWins() {
        assertEquals(0, topService.wins().size)
    }

    @Test
    fun testLosses() {
        assertEquals(0, topService.losses().size)
    }
}
