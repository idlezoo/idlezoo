package idlezoo

import com.fasterxml.jackson.databind.ObjectMapper
import one.util.streamex.IntStreamEx
import one.util.streamex.StreamEx
import org.springframework.beans.factory.InitializingBean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.query
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

const val STARTING_MONEY = 50.0

enum class Outcome {
    WIN, LOSS, WAITING
}

@Service
@Transactional
class FightService(private val template: JdbcTemplate,
                   private val gameService: GameService,
                   private val resourcesService: ResourcesService) {

    fun fight(userId: Int): OutcomeContainer {
        val waitingFighter = template.queryForObject(
                "select waiting_user_id from arena for update",
                Int::class.java)
        if (userId == waitingFighter) {
            return OutcomeContainer.WAITING
        }
        if (waitingFighter == null) {
            template.update("update arena set waiting_user_id=?", userId)
            template.update("update users set waiting_for_fight_start=now() where id=?", userId)
            return OutcomeContainer.WAITING
        }
        val (buildings, name) = gameService.getZoo(waitingFighter)
        val (buildings1) = gameService.getZoo(userId)

        val buildingsSuperSet = HashSet<String>()
        buildingsSuperSet.addAll(
                StreamEx.of(buildings).filter { (_, number) -> number != 0 }.map { it.getName() }.toList())
        buildingsSuperSet.addAll(
                StreamEx.of(buildings1).filter { (_, number) -> number != 0 }.map { it.getName() }.toList())
        var waitingWins = 0
        var fighterWins = 0
        for (building in buildingsSuperSet) {
            val fighterAnimals = StreamEx.of(buildings1)
                    .filter(compose(Function { it.getName() }, Predicate<String> { building == it }))
                    .findAny().orElse(null)
            if (fighterAnimals == null) {
                waitingWins++
                continue
            }

            val waitingAnimals = StreamEx.of(buildings)
                    .filter(compose(Function { it.getName() }, Predicate<String> { building == it }))
                    .findAny().orElse(null)
            if (waitingAnimals == null) {
                fighterWins++
                continue
            }

            val buildingIndex = resourcesService.animalIndex(building)
            if (waitingAnimals.number >= fighterAnimals.number) {
                waitingWins++
                template.update(
                        "update animal set count=count-?, lost=lost+? where user_id=? and animal_type=?",
                        fighterAnimals.number, fighterAnimals.number, waitingFighter, buildingIndex)
                template.update(
                        "update animal set count=0, lost=lost+? where user_id=? and animal_type=?",
                        fighterAnimals.number, userId, buildingIndex)
            } else {
                fighterWins++
                template.update(
                        "update animal set count=count-?, lost=lost+?  where user_id=? and animal_type=?",
                        waitingAnimals.number, waitingAnimals.number, userId, buildingIndex)
                template.update(
                        "update animal set count=0, lost=lost+? where user_id=? and animal_type=?",
                        waitingAnimals.number, waitingFighter, buildingIndex)
            }
        }
        val outcome: Outcome
        if (waitingWins >= fighterWins) {
            outcome = Outcome.LOSS
            template.update("update users set fights_win=fights_win+1 where id=?", waitingFighter)
            template.update("update users set fights_loss=fights_loss+1 where id=?", userId)
        } else {
            outcome = Outcome.WIN
            template.update("update users set fights_win=fights_win+1 where id=?", userId)
            template.update("update users set fights_loss=fights_loss+1 where id=?", waitingFighter)
        }

        template.update("update arena set waiting_user_id=null")
        template.update("update users set waiting_for_fight_start=null"
                + ", champion_time = champion_time + EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint"
                + " where username=?", name)

        gameService.updateIncome(userId)
        return OutcomeContainer(outcome, gameService.updateIncomeAndGetZoo(waitingFighter))
    }

    private fun <K, T> compose(fn: Function<T, K>, pred: Predicate<K>): Predicate<T> {
        return Predicate { t -> pred.test(fn.apply(t)) }
    }

    class OutcomeContainer internal constructor(val outcome: Outcome, val waitingFighter: Zoo?) {
        companion object {

            internal val WAITING = OutcomeContainer(Outcome.WAITING, null)
        }
    }


}


@Service
@Transactional
class GameService(private val template: JdbcTemplate, private val resourcesService: ResourcesService) {
    private fun updateMoney(userId: Int) {
        template.update(UPDATE_MONEY, userId)
    }

    private fun updateAndGetMoney(userId: Int): Double {
        return template.queryForObject("$UPDATE_MONEY returning money", arrayOf(userId))!!
    }

    fun getZoo(userId: Int): Zoo {
        updateMoney(userId)
        return getZooNoUpdate(userId)
    }

    private fun getZooNoUpdate(userId: Int): Zoo {
        val zoo = getZooNoAvailablePerks(userId, getBuildings(userId))
        return zoo.copy(availablePerks = resourcesService.availablePerks(zoo))
    }

    private fun getZooNoAvailablePerks(userId: Int, buildings: List<ZooBuildings>): Zoo {
        return template.queryForObject("select *,"
                + " EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint as waiting_fight_time"
                + " from users where id=?",
                userId) { res, _ ->
            val waitingFightTime = res.getLong("waiting_fight_time")
            val waitingForFight = !res.wasNull()
            Zoo(
                    buildings,
                    res.getString("username"),
                    res.getDouble("money"),
                    res.getDouble("perk_income"),
                    IntStreamEx.of(res.getArray("perks").getArray() as Array<Int>)
                            .mapToObj<Perks.Perk> { resourcesService.perkByIndex(it) }.toList(),
                    listOf(),
                    res.getInt("fights_win"),
                    res.getInt("fights_loss"),
                    waitingForFight,
                    res.getLong("champion_time") + waitingFightTime,
                    res.getDouble("base_income")
            )
        }!!
    }

    private fun getBuildings(userId: Int): List<ZooBuildings> {
        return template.query("select * from animal where user_id=? order by animal_type", userId)
        { res, _ ->
            val building = resourcesService.animalByIndex(res.getInt("animal_type"))
            ZooBuildings(
                    res.getInt("level"),
                    res.getInt("count"),
                    res.getInt("lost"),
                    building
            )
        }
    }

    fun buyPerk(userId: Int, perkName: String): Zoo {
        val perk = resourcesService.perk(perkName)
        val money = updateAndGetMoney(userId)
        if (money < perk.cost) {
            return getZooNoUpdate(userId)
        }

        val zoo = getZooNoAvailablePerks(userId, getBuildings(userId))
        val availablePerks = resourcesService.availablePerks(zoo)
        if (!availablePerks.contains(perk)) {
            return getZooNoUpdate(userId)
        }
        template.update("update users set perks = perks || ? where id=?",
                resourcesService.perkIndex(perkName), userId)

        return updateIncomeAndGetZoo(userId)
    }

    fun buy(userId: Int, animal: String): Zoo {
        val money = updateAndGetMoney(userId)
        val count = template.queryForObject(
                "select count from animal where user_id=? and animal_type=?", Int::class.java, userId,
                resourcesService.animalIndex(animal))
        val type = resourcesService.type(animal)
        val buildCost = type.buildCost(count)
        if (money < buildCost) {
            return getZooNoUpdate(userId)
        }
        template.update("update animal set count=count+1" + " where user_id=? and animal_type=?", userId, resourcesService.animalIndex(animal))
        template.update("update users set money=money-? where id=?", buildCost, userId)
        if (count == 0) {
            val next = resourcesService.nextType(animal)
            if (next != null) {
                val nextIndex = resourcesService.animalIndex(next.name)!!
                template.update("insert into animal(user_id, animal_type)"
                        //+ " values(?, ?)"
                        + " select ?, ?"
                        + " where not exists (select 1 from animal where user_id=? and animal_type=?)", userId, nextIndex, userId, nextIndex)
                //requires postgres 9.5
                //+ " on conflict do nothing"
            }
        }
        return updateIncomeAndGetZoo(userId)
    }

    fun upgrade(userId: Int, animal: String): Zoo {
        val money = updateAndGetMoney(userId)
        val level = template.queryForObject(
                "select level from animal where user_id=? and animal_type=?", Int::class.java, userId,
                resourcesService.animalIndex(animal))
        val type = resourcesService.type(animal)
        val upgradeCost = type.upgradeCost(level)
        if (money < upgradeCost) {
            return getZooNoUpdate(userId)
        }
        template.update("update animal set level=level+1" + " where user_id=? and animal_type=?", userId, resourcesService.animalIndex(animal))
        template.update("update users set money=money-? where id=?", upgradeCost, userId)
        return updateIncomeAndGetZoo(userId)
    }

    fun updateIncome(userId: Int) {
        updateIncome(userId, getBuildings(userId))
    }

    private fun updateIncome(userId: Int, buildings: List<ZooBuildings>): Zoo {
        val newBaseIncome = buildings.stream().mapToDouble { it.getIncome() }.sum()
        val zoo = getZooNoAvailablePerks(userId, buildings).copy(baseIncome = newBaseIncome)
        val newPerkIncome = StreamEx.of<Perks.Perk>(zoo.perks)
                .mapToDouble { perk -> perk.perkIncome(zoo) }.sum()
        template.update("update users set base_income=?, perk_income=? where id=?",
                newBaseIncome, newPerkIncome, userId)
        return zoo.copy(perkIncome = newPerkIncome)
    }

    fun updateIncomeAndGetZoo(userId: Int): Zoo {
        val zoo = updateIncome(userId, getBuildings(userId))
        return zoo.copy(availablePerks = resourcesService.availablePerks(zoo))
    }

    companion object {

        private val UPDATE_MONEY = ("update users set money=money"
                + " + base_income * EXTRACT(EPOCH FROM now()-last_money_update)"
                + " + perk_income * EXTRACT(EPOCH FROM now()-last_money_update)"
                + ", last_money_update=now() where id=?")
    }
}


@Service
class ResourcesService(private val mapper: ObjectMapper) : InitializingBean {

    lateinit var animalsList: List<Building>
    lateinit var perkList: List<Perks.Perk>
    private lateinit var animalIndexes: Map<String, Int>
    private lateinit var animalTypes: Map<String, Building>
    private lateinit var nextAnimals: Map<String, Building>
    private lateinit var perkIndexes: Map<String, Int>
    private lateinit var perkNames: Map<String, Perks.Perk>
    private lateinit var startingAnimal: Building

    override fun afterPropertiesSet() {
        initAnimals()
        initPerks()
    }

    private fun initPerks() {
        val perkIndexesBuilder = mutableMapOf<String, Int>()
        val perkNamesBuilder = mutableMapOf<String, Perks.Perk>()
        ResourcesService::class.java.getResourceAsStream(
                "/mechanics/perks.json").use { perks ->
            val type = mapper.typeFactory.constructCollectionType(List::class.java, Perks.Perk::class.java)
            perkList = Collections.unmodifiableList(mapper.readValue(perks, type))
            for (i in perkList.indices) {
                val perk = perkList[i]
                perkIndexesBuilder[perk.name] = i
                perkNamesBuilder[perk.name] = perk
            }
        }
        perkIndexes = perkIndexesBuilder
        perkNames = perkNamesBuilder
    }

    private fun initAnimals() {
        val animalTypesBuilder = mutableMapOf<String, Building>()
        val nextAnimalsBuilder = mutableMapOf<String, Building>()
        val animalIndexesBuilder = mutableMapOf<String, Int>()
        ResourcesService::class.java.getResourceAsStream(
                "/mechanics/animals.json").use { creatures ->
            val type = mapper.typeFactory.constructCollectionType(List::class.java,
                    Building::class.java)
            animalsList = Collections.unmodifiableList(mapper.readValue(creatures, type))
            startingAnimal = animalsList[0]

            var prev: Building? = null
            for (i in animalsList.indices) {
                val animal = animalsList[i]
                animalIndexesBuilder[animal.name] = i
                animalTypesBuilder[animal.name] = animal
                if (prev != null) {
                    nextAnimalsBuilder[prev.name] = animal
                }
                prev = animal
            }
        }
        animalTypes = animalTypesBuilder
        nextAnimals = nextAnimalsBuilder
        animalIndexes = animalIndexesBuilder
    }

    internal fun startingAnimal(): Building {
        return startingAnimal
    }

    internal fun firstName(): String {
        return startingAnimal.name
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

    fun perkByIndex(index: Int): Perks.Perk {
        return perkList[index]
    }

    fun perk(name: String): Perks.Perk {
        return perkNames[name]!!
    }

    fun perkIndex(perkName: String): Int? {
        return perkIndexes[perkName]
    }

    fun animalByIndex(index: Int): Building {
        return animalsList[index]
    }

    fun animalIndex(animalName: String): Int? {
        return animalIndexes[animalName]
    }

    fun availablePerks(zoo: Zoo): List<Perks.Perk> {
        val result = ArrayList(perkList)
        result.removeAll(zoo.perks)
        return StreamEx.of(result)
                .filter { perk -> perk.isAvailable(zoo) }
                .toList()
    }
}


@Service
@Transactional
class TopService(private val resourcesService: ResourcesService, private val template: JdbcTemplate) {

    fun building(building: String): List<TopEntry<Int>> =
            template.query("select u.username, a.count as topvalue"
                    + " from animal a"
                    + " join users u"
                    + " on a.user_id=u.id"
                    + " where a.animal_type=?"
                    + " order by a.count desc"
                    + " limit 10",
                    RowMapper { it, _ -> mapTopEntry<Int>(it) },
                    resourcesService.animalIndex(building)
            )


    fun income(): List<TopEntry<Double>> =
            template.query("select username, base_income + perk_income as topvalue"
                    + " from users"
                    + " order by topvalue desc"
                    + " limit 10")
            { it, _ -> mapTopEntry<Double>(it) }

    fun wins(): List<TopEntry<Int>> =
            template.query("select username, fights_win as topvalue"
                    + " from users"
                    + " order by fights_win desc"
                    + " limit 10")
            { it, _ -> mapTopEntry<Int>(it) }


    fun losses(): List<TopEntry<Int>> =
            template.query("select username, fights_loss as topvalue"
                    + " from users"
                    + " order by fights_loss desc"
                    + " limit 10")
            { it, _ -> mapTopEntry<Int>(it) }


    fun championTime(): List<TopEntry<Long>> =
            template.query(
                    "select username,"
                            + " champion_time + coalesce(EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint, 0) as topvalue"
                            + " from users"
                            + " order by topvalue desc"
                            + " limit 10")
            { it, _ -> mapTopEntry<Long>(it) }

    private inline fun <reified T : Any> mapTopEntry(res: ResultSet): TopEntry<T> =
            TopEntry(
                    res.getString("username"),
                    res.getObject("topvalue", T::class.java)
            )
}


