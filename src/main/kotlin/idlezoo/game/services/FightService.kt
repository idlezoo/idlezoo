package idlezoo.game.services

import idlezoo.game.domain.Zoo
import one.util.streamex.StreamEx
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

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

    enum class Outcome {
        WIN, LOSS, WAITING
    }
}
