package idlezoo.game.services

import idlezoo.game.domain.Perks
import idlezoo.game.domain.Zoo
import idlezoo.game.domain.ZooBuildings
import one.util.streamex.IntStreamEx
import one.util.streamex.StreamEx
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GameService(private val template: JdbcTemplate, private val resourcesService: ResourcesService) {
    private val zooBuildingMapper: RowMapper<ZooBuildings>

    init {
        zooBuildingMapper = RowMapper { res, _ ->
            val building = resourcesService.animalByIndex(res.getInt("animal_type"))
            ZooBuildings(
                    res.getInt("level"),
                    res.getInt("count"),
                    res.getInt("lost"),
                    building
            )
        }
    }

    private fun updateMoney(userId: Int) {
        template.update(UPDATE_MONEY, userId)
    }

    private fun updateAndGetMoney(userId: Int): Double {
        return template.queryForObject("$UPDATE_MONEY returning money", Double::class.java, userId)
    }

    fun getZoo(userId: Int): Zoo {
        updateMoney(userId)
        return getZooNoUpdate(userId)
    }

    private fun getZooNoUpdate(userId: Int): Zoo {
        val zoo = getZooBuilder(userId, getBuildings(userId))
        return zoo.copy(availablePerks = resourcesService.availablePerks(zoo))
    }

    private fun getZooBuilder(userId: Int, buildings: List<ZooBuildings>): Zoo {
        return template.queryForObject<Zoo>("select *,"
                + " EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint as waiting_fight_time"
                + " from users where id=?",
                RowMapper { res, _ ->
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
                }, userId)!!
    }

    private fun getBuildings(userId: Int): List<ZooBuildings> {
        return template.query("select * from animal where user_id=? order by animal_type",
                zooBuildingMapper, userId)
    }

    fun buyPerk(userId: Int, perkName: String): Zoo {
        val perk = resourcesService.perk(perkName)
        val money = updateAndGetMoney(userId)
        if (money < perk.cost) {
            return getZooNoUpdate(userId)
        }

        val zoo = getZooBuilder(userId, getBuildings(userId))
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
        val zoo = getZooBuilder(userId, buildings).copy(baseIncome = newBaseIncome)
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
