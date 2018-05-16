package idlezoo.game.services;

import idlezoo.game.domain.*;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GameService {
    private final JdbcTemplate template;
    private final ResourcesService resourcesService;
    private final RowMapper<ZooBuildings> zooBuildingMapper;
    private final RowMapper<ImmutableZoo.Builder> zooMapper;

    public GameService(JdbcTemplate template, ResourcesService resourcesService) {
        this.template = template;
        this.resourcesService = resourcesService;
        zooBuildingMapper = (res, rowNum) -> {
            Building building = resourcesService.animalByIndex(res.getInt("animal_type"));
            return ImmutableZooBuildings.builder()
                    .building(building)
                    .name(building.getName())
                    .level(res.getInt("level"))
                    .number(res.getInt("count"))
                    .lost(res.getInt("lost"))
                    .build();
        };

        zooMapper = (res, rowNum) -> {
            long waitingFightTime = res.getLong("waiting_fight_time");
            return ImmutableZoo.builder()
                    .isWaitingForFight(!res.wasNull())
                    .championTime(res.getLong("champion_time") + waitingFightTime)
                    .name(res.getString("username"))
                    .fightWins(res.getInt("fights_win"))
                    .fightLosses(res.getInt("fights_loss"))
                    .money(res.getDouble("money"))
                    .baseIncome(res.getDouble("base_income"))
                    .perkIncome(res.getDouble("perk_income"))
                    .perks(IntStreamEx.of((Integer[]) res.getArray("perks").getArray())
                            .mapToObj(resourcesService::perkByIndex).toList()
                    );
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

    public Zoo getZoo(Integer userId) {
        updateMoney(userId);
        return getZooNoUpdate(userId);
    }

    private Zoo getZooNoUpdate(Integer userId) {
        ImmutableZoo.Builder zooBuilder = getZooBuilder(userId);
        List<ZooBuildings> buildings = getBuildings(userId);

        zooBuilder.buildings(buildings);
        ImmutableZoo zoo = zooBuilder.build();
        return zoo.withAvailablePerks(resourcesService.availablePerks(zoo));

    }

    private ImmutableZoo.Builder getZooBuilder(Integer userId) {
        return template.queryForObject("select *,"
                + " EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint as waiting_fight_time"
                + " from users where id=?", zooMapper, userId);
    }

    private List<ZooBuildings> getBuildings(Integer userId) {
        return template.query("select * from animal where user_id=? order by animal_type",
                zooBuildingMapper, userId);
    }

    public Zoo buyPerk(Integer userId, String perkName) {
        Perks.Perk perk = resourcesService.perk(perkName);
        double money = updateAndGetMoney(userId);
        if (money < perk.getCost()) {
            return getZooNoUpdate(userId);
        }

        ImmutableZoo.Builder builder = getZooBuilder(userId).buildings(getBuildings(userId));
        List<Perks.Perk> availablePerks = resourcesService.availablePerks(builder.build());
        if (!availablePerks.contains(perk)) {
            return getZooNoUpdate(userId);
        }
        template.update("update users set perks = perks || ? where id=?",
                resourcesService.perkIndex(perkName), userId);

        return updateIncomeAndGetZoo(userId);
    }

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
//                        + " values(?, ?)"
                                + " select ?, ?"
                                + " where not exists (select 1 from animal where user_id=? and animal_type=?)"
//                         requires postgres 9.5
//                         + " on conflict do nothing"
                        , userId, nextIndex, userId, nextIndex);
            }
        }
        return updateIncomeAndGetZoo(userId);
    }

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

    public void updateIncome(Integer userId) {
        updateIncome(userId, getBuildings(userId));
    }

    private ImmutableZoo updateIncome(Integer userId, List<ZooBuildings> buildings) {
        final double newBaseIncome = buildings.stream().mapToDouble(ZooBuildings::getIncome).sum();
        ImmutableZoo.Builder zooBuilder = getZooBuilder(userId).baseIncome(newBaseIncome).buildings(buildings);
        ImmutableZoo zoo = zooBuilder.build();
        final double newPerkIncome = StreamEx.of(zoo.getPerks())
                .mapToDouble(perk -> perk.perkIncome(zoo)).sum();
        template.update("update users set base_income=?, perk_income=? where id=?",
                newBaseIncome, newPerkIncome, userId);
        return zoo.withPerkIncome(newPerkIncome);
    }

    public Zoo updateIncomeAndGetZoo(Integer userId) {
        ImmutableZoo zoo = updateIncome(userId, getBuildings(userId));
        return zoo.withAvailablePerks(resourcesService.availablePerks(zoo));
    }
}
