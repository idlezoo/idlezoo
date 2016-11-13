package idlezoo.game.services.postgres;

import java.util.HashSet;
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
  public Zoo fight(String username) {
    String waitingFighter = template.queryForObject("select waiting_user from arena for update",
        String.class);
    if (username.equals(waitingFighter)) {
      return null;
    }
    if (waitingFighter == null) {
      template.update("update arena set waiting_user=?", username);
      template.update("update users set waiting_for_fight_start=now() where username=?", username);
      return null;
    }
    Zoo waiting = gameService.getZoo(waitingFighter);
    Zoo fighter = gameService.getZoo(username);

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

      int buildingIndex = resourcesService.index(building);
      if (waitingAnimals.getNumber() >= fighterAnimals.getNumber()) {
        waitingWins++;
        template.update("update animal set count=count-? where username=? and animal_type=?",
            fighterAnimals.getNumber(), waiting.getName(), buildingIndex);
        template.update("update animal set count=0 where username=? and animal_type=?",
            fighter.getName(), buildingIndex);
      } else {
        fighterWins++;
        template.update("update animal set count=count-? where username=? and animal_type=?",
            waitingAnimals.getNumber(), fighter.getName(), buildingIndex);
        template.update("update animal set count=0 where username=? and animal_type=?",
            waiting.getName(), buildingIndex);
      }
    }
    if (waitingWins >= fighterWins) {
      template.update("update users set fights_win=fights_win+1 where username=?", waiting
          .getName());
    } else {
      template.update("update users set fights_win=fights_win+1 where username=?", fighter
          .getName());
    }

    template.update("update arena set waiting_user=null");
    template.update("update users set waiting_for_fight_start=null"
        + ", champion_time = champion_time + EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint"
        + " where username=?", waiting.getName());

    gameService.updateIncome(fighter.getName());
    return gameService.updateIncomeAndGetZoo(waiting.getName());
  }

  public static <K, T> Predicate<T> compose(Function<T, K> fn, Predicate<K> pred) {
    return t -> pred.test(fn.apply(t));
  }

}
