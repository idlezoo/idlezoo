package idlezoo.game.services.postgres;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.Perks.Perk;
import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.Zoo.Builder;
import idlezoo.game.domain.ZooBuildings;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

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
      Building building = resourcesService.animalByIndex(res.getInt("animal_type"));
      return new ZooBuildings(building, res.getInt("level"), res.getInt("count"), res.getInt(
          "lost"));
    };

    zooMapper = (res, rowNum) -> {
      Builder builder = new Zoo.Builder();
      builder.setName(res.getString("username"));
      builder.setFightWins(res.getInt("fights_win"));
      builder.setFightLosses(res.getInt("fights_loss"));
      builder.setBaseIncome(res.getDouble("base_income"));
      builder.setMoney(res.getDouble("money"));
      long waitingFightTime = res.getLong("waiting_fight_time");
      builder.setWaitingForFight(!res.wasNull());
      builder.setChampionTime(res.getLong("champion_time") + waitingFightTime);
      Integer[] perkIds = (Integer[]) res.getArray("perks").getArray();
      builder.setPerks(IntStreamEx.of(perkIds).mapToObj(resourcesService::perkByIndex).toList());
      return builder;
    };

  }

  private static final String UPDATE_MONEY = "update users set money=money"
      + " + base_income * EXTRACT(EPOCH FROM now()-last_money_update)"
      + " + perk_income * EXTRACT(EPOCH FROM now()-last_money_update)"
      + ", last_money_update=now() where id=?";

  private void updateMoney(Integer userId) {
    template.update(UPDATE_MONEY, userId);
  }

  private double updateAndGetMoney(Integer userId) {
    return template.queryForObject(UPDATE_MONEY + " returning money", Double.class, userId);
  }

  @Override
  public Zoo getZoo(Integer userId) {
    updateMoney(userId);
    return getZooNoUpdate(userId);
  }

  private Zoo getZooNoUpdate(Integer userId) {
    Builder zooBuilder = getZooBuilder(userId);
    List<ZooBuildings> buildings = getBuildings(userId);

    zooBuilder.setBuildings(buildings);
    zooBuilder.setAvailablePerks(resourcesService.availablePerks(zooBuilder));

    return zooBuilder.build();
  }

  private Builder getZooBuilder(Integer userId) {
    return template.queryForObject("select *,"
        + " EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint as waiting_fight_time"
        + " from users where id=?", zooMapper, userId);
  }

  private List<ZooBuildings> getBuildings(Integer userId) {
    return template.query("select * from animal where user_id=? order by animal_type",
        zooBuildingMapper, userId);
  }

  @Override
  public Zoo buyPerk(Integer userId, String perkName) {
    Perk perk = resourcesService.perk(perkName);
    double money = updateAndGetMoney(userId);
    if (money < perk.getCost()) {
      return getZooNoUpdate(userId);
    }

    Builder builder = getZooBuilder(userId).setBuildings(getBuildings(userId));
    List<Perk> availablePerks = resourcesService.availablePerks(builder);
    if (!availablePerks.contains(perk)) {
      return getZooNoUpdate(userId);
    }
    template.update("update users set perks = perks || ? where id=?",
        resourcesService.perkIndex(perkName), userId);

    return updateIncomeAndGetZoo(userId);
  }

  @Override
  public Zoo buy(Integer userId, String animal) {
    double money = updateAndGetMoney(userId);
    int count = template.queryForObject(
        "select count from animal where user_id=? and animal_type=?", Integer.class, userId,
        resourcesService.animalIndex(animal));
    Building type = resourcesService.type(animal);
    double buildCost = type.buildCost(count);
    if (money < buildCost) {
      return getZooNoUpdate(userId);
    }
    template.update("update animal set count=count+1"
        + " where user_id=? and animal_type=?", userId, resourcesService.animalIndex(animal));
    template.update("update users set money=money-? where id=?", buildCost, userId);
    if (count == 0) {
      Building next = resourcesService.nextType(animal);
      if (next != null) {
        int nextIndex = resourcesService.animalIndex(next.getName());
        template.update("insert into animal(user_id, animal_type)"
            + " select ?, ?"
            + " where not exists (select 1 from animal where user_id=? and animal_type=?)"
        // requires postgres 9.5
        // + " on conflict do nothing"
            , userId, nextIndex, userId, nextIndex);
      }
    }
    return updateIncomeAndGetZoo(userId);
  }

  @Override
  public Zoo upgrade(Integer userId, String animal) {
    double money = updateAndGetMoney(userId);
    int level = template.queryForObject(
        "select level from animal where user_id=? and animal_type=?", Integer.class, userId,
        resourcesService.animalIndex(animal));
    Building type = resourcesService.type(animal);
    double upgradeCost = type.upgradeCost(level);
    if (money < upgradeCost) {
      return getZooNoUpdate(userId);
    }
    template.update("update animal set level=level+1"
        + " where user_id=? and animal_type=?", userId, resourcesService.animalIndex(animal));
    template.update("update users set money=money-? where id=?", upgradeCost, userId);
    return updateIncomeAndGetZoo(userId);
  }

  public Builder updateIncome(Integer userId) {
    return updateIncome(userId, getBuildings(userId));
  }

  private Builder updateIncome(Integer userId, List<ZooBuildings> buildings) {
    final double newBaseIncome = buildings.stream().mapToDouble(ZooBuildings::getIncome).sum();
    Builder zooBuilder = getZooBuilder(userId).setBaseIncome(newBaseIncome).setBuildings(buildings);
    final double newPerkIncome = StreamEx.of(zooBuilder.getPerks())
        .mapToDouble(perk -> perk.perkIncome(zooBuilder)).sum();
    template.update("update users set base_income=?, perk_income=? where id=?",
        newBaseIncome, newPerkIncome, userId);
    return zooBuilder;
  }

  public Zoo updateIncomeAndGetZoo(Integer userId) {
    Builder builder = updateIncome(userId, getBuildings(userId));
    builder.setAvailablePerks(resourcesService.availablePerks(builder));
    return builder.build();
  }
}
