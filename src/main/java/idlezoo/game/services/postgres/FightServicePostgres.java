package idlezoo.game.services.postgres;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.ZooBuildings;
import idlezoo.game.services.FightService;
import idlezoo.game.services.ResourcesService;
import one.util.streamex.StreamEx;

@Service
@Transactional
@Profile("postgres")
public class FightServicePostgres implements FightService {

  private final JdbcTemplate template;
  private final GameServicePostgres gameService;
  private final ResourcesService resourcesService;

  public FightServicePostgres(JdbcTemplate template, GameServicePostgres gameService, ResourcesService resourcesService) {
    this.template = template;
    this.gameService = gameService;
    this.resourcesService=resourcesService;
  }

  @Override
  public OutcomeContainer fight(final Integer userId) {
    final Integer waitingFighter = template.queryForObject("select waiting_user_id from arena for update",
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

      Integer buildingIndex = resourcesService.index(building);
      if (waitingAnimals.getNumber() >= fighterAnimals.getNumber()) {
        waitingWins++;
        template.update("update animal set count=count-? where user_id=? and animal_type=?",
            fighterAnimals.getNumber(), waitingFighter, buildingIndex);
        template.update("update animal set count=0 where user_id=? and animal_type=?",
        		userId, buildingIndex);
      } else {
        fighterWins++;
        template.update("update animal set count=count-? where user_id=? and animal_type=?",
            waitingAnimals.getNumber(), userId, buildingIndex);
        template.update("update animal set count=0 where user_id=? and animal_type=?",
        		waitingFighter, buildingIndex);
      }
    }
    Outcome outcome;
    if (waitingWins >= fighterWins) {
        outcome = Outcome.LOSS;
        template.update("update users set fights_win=fights_win+1 where id=?", waitingFighter);
    } else {
        outcome = Outcome.WIN;
        template.update("update users set fights_win=fights_win+1 where id=?", userId);
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

}
