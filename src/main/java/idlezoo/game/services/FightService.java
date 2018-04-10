package idlezoo.game.services;

import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.ZooBuildings;
import one.util.streamex.StreamEx;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@Transactional
public class FightService {
    private final JdbcTemplate template;
    private final GameService gameService;
    private final ResourcesService resourcesService;

    public FightService(JdbcTemplate template,
                        GameService gameService,
                        ResourcesService resourcesService) {
        this.template = template;
        this.gameService = gameService;
        this.resourcesService = resourcesService;
    }

    public OutcomeContainer fight(final Integer userId) {
        final Integer waitingFighter = template.queryForObject(
                "select waiting_user_id from arena for update",
                Integer.class);
        if (Objects.equals(userId, waitingFighter)) {
            return OutcomeContainer.WAITING;
        }
        if (waitingFighter == null) {
            template.update("update arena set waiting_user_id=?", userId);
            template.update("update users set waiting_for_fight_start=now() where id=?", userId);
            return OutcomeContainer.WAITING;
        }
        Zoo waiting = gameService.getZoo(waitingFighter);
        Zoo fighter = gameService.getZoo(userId);

        Set<String> buildingsSuperSet = new HashSet<>();
        buildingsSuperSet.addAll(
                StreamEx.of(waiting.getBuildings()).filter(b -> b.getNumber() != 0).map(
                        ZooBuildings::getName).toList());
        buildingsSuperSet.addAll(
                StreamEx.of(fighter.getBuildings()).filter(b -> b.getNumber() != 0).map(
                        ZooBuildings::getName).toList());
        int waitingWins = 0, fighterWins = 0;
        for (String building : buildingsSuperSet) {
            ZooBuildings fighterAnimals = StreamEx.of(fighter.getBuildings())
                    .filter(compose(ZooBuildings::getName, building::equals))
                    .findAny().orElse(null);
            if (fighterAnimals == null) {
                waitingWins++;
                continue;
            }

            ZooBuildings waitingAnimals = StreamEx.of(waiting.getBuildings())
                    .filter(compose(ZooBuildings::getName, building::equals))
                    .findAny().orElse(null);
            if (waitingAnimals == null) {
                fighterWins++;
                continue;
            }

            Integer buildingIndex = resourcesService.animalIndex(building);
            if (waitingAnimals.getNumber() >= fighterAnimals.getNumber()) {
                waitingWins++;
                template.update(
                        "update animal set count=count-?, lost=lost+? where user_id=? and animal_type=?",
                        fighterAnimals.getNumber(), fighterAnimals.getNumber(), waitingFighter, buildingIndex);
                template.update(
                        "update animal set count=0, lost=lost+? where user_id=? and animal_type=?",
                        fighterAnimals.getNumber(), userId, buildingIndex);
            } else {
                fighterWins++;
                template.update(
                        "update animal set count=count-?, lost=lost+?  where user_id=? and animal_type=?",
                        waitingAnimals.getNumber(), waitingAnimals.getNumber(), userId, buildingIndex);
                template.update(
                        "update animal set count=0, lost=lost+? where user_id=? and animal_type=?",
                        waitingAnimals.getNumber(), waitingFighter, buildingIndex);
            }
        }
        Outcome outcome;
        if (waitingWins >= fighterWins) {
            outcome = Outcome.LOSS;
            template.update("update users set fights_win=fights_win+1 where id=?", waitingFighter);
            template.update("update users set fights_loss=fights_loss+1 where id=?", userId);
        } else {
            outcome = Outcome.WIN;
            template.update("update users set fights_win=fights_win+1 where id=?", userId);
            template.update("update users set fights_loss=fights_loss+1 where id=?", waitingFighter);
        }

        template.update("update arena set waiting_user_id=null");
        template.update("update users set waiting_for_fight_start=null"
                + ", champion_time = champion_time + EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint"
                + " where username=?", waiting.getName());

        gameService.updateIncome(userId);
        return new OutcomeContainer(outcome, gameService.updateIncomeAndGetZoo(waitingFighter));
    }

    public static <K, T> Predicate<T> compose(Function<T, K> fn, Predicate<K> pred) {
        return t -> pred.test(fn.apply(t));
    }

    public static class OutcomeContainer {
        private final Outcome outcome;
        private final Zoo waitingFighter;

        public OutcomeContainer(Outcome outcome, Zoo waitingFighter) {
            this.outcome = outcome;
            this.waitingFighter = waitingFighter;
        }

        public static final OutcomeContainer WAITING = new OutcomeContainer(Outcome.WAITING, null);

        public static OutcomeContainer win(Zoo waitingFighter) {
            return new OutcomeContainer(Outcome.WIN, waitingFighter);
        }

        public static OutcomeContainer loss(Zoo waitingFighter) {
            return new OutcomeContainer(Outcome.LOSS, waitingFighter);
        }

        public Outcome getOutcome() {
            return outcome;
        }

        public Zoo getWaitingFighter() {
            return waitingFighter;
        }
    }

    public enum Outcome {
        WIN, LOSS, WAITING;
    }
}
