package idlezoo.game.services

import com.fasterxml.jackson.databind.ObjectMapper
import idlezoo.game.domain.Building
import idlezoo.game.domain.Perks.Perk
import idlezoo.game.domain.Zoo
import one.util.streamex.StreamEx
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import java.util.*
import java.util.Collections.unmodifiableList
import java.util.Collections.unmodifiableMap

@Service
class ResourcesService(private val mapper: ObjectMapper) : InitializingBean {

    var animalsList: List<Building>? = null
    var perkList: List<Perk>? = null
    private var animalIndexes: MutableMap<String, Int> = HashMap()
    private var animalTypes: MutableMap<String, Building> = HashMap()
    private var nextAnimals: MutableMap<String, Building> = HashMap()
    private var perkIndexes: MutableMap<String, Int> = HashMap()
    private var perkNames: MutableMap<String, Perk> = HashMap()
    private var startingAnimal: Building? = null

    override fun afterPropertiesSet() {
        initAnimals()
        initPerks()
    }

    private fun initPerks() {
        ResourcesService::class.java.getResourceAsStream(
                "/mechanics/perks.json").use { perks ->
            val type = mapper.typeFactory.constructCollectionType(List::class.java, Perk::class.java)
            perkList = unmodifiableList(mapper.readValue(perks, type))
            for (i in perkList!!.indices) {
                val perk = perkList!![i]
                perkIndexes[perk.name] = i
                perkNames[perk.name] = perk
            }
            perkIndexes = unmodifiableMap(perkIndexes)
            perkNames = unmodifiableMap(perkNames)
        }
    }

    private fun initAnimals() {
        ResourcesService::class.java.getResourceAsStream(
                "/mechanics/animals.json").use { creatures ->
            val type = mapper.typeFactory.constructCollectionType(List::class.java,
                    Building::class.java)
            animalsList = unmodifiableList(mapper.readValue(creatures, type))
            startingAnimal = animalsList!![0]

            var prev: Building? = null
            for (i in animalsList!!.indices) {
                val animal = animalsList!![i]
                animalIndexes[animal.name] = i
                animalTypes[animal.name] = animal
                if (prev != null) {
                    nextAnimals[prev.name] = animal
                }
                prev = animal
            }
        }
        animalTypes = unmodifiableMap(animalTypes)
        nextAnimals = unmodifiableMap(nextAnimals)
        animalIndexes = unmodifiableMap(animalIndexes)
    }

    internal fun startingMoney(): Double {
        return STARTING_MONEY
    }

    internal fun startingAnimal(): Building? {
        return startingAnimal
    }

    internal fun firstName(): String {
        return startingAnimal!!.name
    }

    fun secondName(): String {
        return nextType(firstName())!!.name
    }

    fun nextType(buildingName: String): Building? {
        return nextAnimals[buildingName]
    }

    fun type(typeName: String): Building {
        return animalTypes[typeName]!!
    }

    fun perkByIndex(index: Int): Perk {
        return perkList!![index]
    }

    fun perk(name: String): Perk {
        return perkNames[name]!!
    }

    fun perkIndex(perkName: String): Int? {
        return perkIndexes[perkName]
    }

    fun animalByIndex(index: Int): Building {
        return animalsList!![index]
    }

    fun animalIndex(animalName: String): Int? {
        return animalIndexes[animalName]
    }

    fun availablePerks(zoo: Zoo): List<Perk> {
        val result = ArrayList(perkList!!)
        result.removeAll(zoo.perks)
        return StreamEx.of(result)
                .filter { perk -> perk.isAvailable(zoo) }
                .toList()
    }

    companion object {
        private val STARTING_MONEY = 50.0
    }
}
