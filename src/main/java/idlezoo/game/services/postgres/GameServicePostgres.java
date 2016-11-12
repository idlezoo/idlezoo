package idlezoo.game.services.postgres;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.Zoo.Builder;
import idlezoo.game.domain.ZooBuildings;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;

@Service
@Transactional
@Profile("postgres")
public class GameServicePostgres implements GameService {

  private final JdbcTemplate template;
  private final ResourcesService resourcesService;
  private final RowMapper<ZooBuildings> zooBuildingMapper;
  private final RowMapper<Zoo.Builder> zooMapper;

  public GameServicePostgres(JdbcTemplate template, ResourcesService resourcesService) {
    this.template = template;
    this.resourcesService = resourcesService;
    zooBuildingMapper = (res, rowNum) -> {
      Building building = resourcesService.type(res.getString("animal_type"));
      return new ZooBuildings(building, res.getInt("level"), res.getInt("count"));
    };

    zooMapper = (res, rowNum) -> {
      Builder builder = new Zoo.Builder();
      builder.setName(res.getString("username"));
      builder.setFightWins(res.getInt("fights_win"));
      builder.setIncome(res.getDouble("income"));
      builder.setMoney(res.getDouble("money"));
      long waitingFightTime = res.getLong("waiting_fight_time");
      builder.setChampionTime(res.getLong("champion_time") + waitingFightTime);
      builder.setWaitingForFight(waitingFightTime != 0);
      return builder;
    };

  }

  private static final String UPDATE_MONEY =
      "update users set money=money + income * EXTRACT(EPOCH FROM now()-last_money_update)"
          + ", last_money_update=now() where username=?";

  private void updateMoney(String username) {
    template.update(UPDATE_MONEY, username);
  }

  private double updateAndGetMoney(String username) {
    return template.queryForObject(UPDATE_MONEY + " returning money", Double.class, username);
  }

  @Override
  public Zoo getZoo(String name) {
    updateMoney(name);
    return getZooNoUpdate(name);
  }

  private Zoo getZooNoUpdate(String name) {
    Builder zooBuilder = template.queryForObject("select *,"
        + " EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint as waiting_fight_time"
        + " from users where username=?", zooMapper, name);
    List<ZooBuildings> buildings = getBuildings(name);

    zooBuilder.setBuildings(buildings);
    return zooBuilder.build();
  }

  private List<ZooBuildings> getBuildings(String name) {
    return template.query("select * from animal where username=?", zooBuildingMapper, name);
  }

  @Override
  public Zoo buy(String name, String animal) {
    double money = updateAndGetMoney(name);
    int count = template.queryForObject(
        "select count from animal where username=? and animal_type=?", Integer.class, name, animal);
    Building type = resourcesService.type(animal);
    double buildCost = type.buildCost(count);
    if (money < buildCost) {
      return getZooNoUpdate(name);
    }
    template.update("update animal set count=count+1"
        + " where username=? and animal_type=?", name, animal);
    if (count == 0) {
      Building next = resourcesService.nextType(animal);
      if (next != null) {
        try {
          template.update("insert into animal(username, animal_type) values(?,?)"
          // requires postgres 9.5
          // + " on conflict do nothing"
              , name, next.getName());
        } catch (DuplicateKeyException duplicate) {
          // ignore
        }
      }
    }
    return updateIncomeAndGetZoo(name);
  }

  @Override
  public Zoo upgrade(String name, String animal) {
    double money = updateAndGetMoney(name);
    int level = template.queryForObject(
        "select level from animal where username=? and animal_type=?", Integer.class, name, animal);
    Building type = resourcesService.type(animal);
    double upgradeCost = type.upgradeCost(level);
    if (money < upgradeCost) {
      return getZooNoUpdate(name);
    }
    template.update("update animal set level=level+1"
        + " where username=? and animal_type=?", name, animal);
    return updateIncomeAndGetZoo(name);
  }

  public void updateIncome(String name) {
    updateIncome(name, getBuildings(name));
  }

  private double updateIncome(String name, List<ZooBuildings> buildings) {
    final double newIncome = buildings.stream().mapToDouble(ZooBuildings::getIncome).sum();
    template.update("update users set income=? where username=?", newIncome, name);
    return newIncome;
  }

  public Zoo updateIncomeAndGetZoo(String name) {
    final Zoo zoo = getZooNoUpdate(name);
    final double newIncome = updateIncome(name, zoo.getBuildings());
    return zoo.withIncome(newIncome);
  }

}
