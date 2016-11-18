package idlezoo.game.services.postgres;

import java.util.List;

import org.springframework.context.annotation.Profile;
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
			Building building = resourcesService.byIndex(res.getInt("animal_type"));
			return new ZooBuildings(building, res.getInt("level"), res.getInt("count"));
		};

		zooMapper = (res, rowNum) -> {
			Builder builder = new Zoo.Builder();
			builder.setName(res.getString("username"));
			builder.setFightWins(res.getInt("fights_win"));
			builder.setIncome(res.getDouble("income"));
			builder.setMoney(res.getDouble("money"));
			long waitingFightTime = res.getLong("waiting_fight_time");
			builder.setWaitingForFight(!res.wasNull());
			builder.setChampionTime(res.getLong("champion_time") + waitingFightTime);
			return builder;
		};

	}

	private static final String UPDATE_MONEY = "update users set money=money + income * EXTRACT(EPOCH FROM now()-last_money_update)"
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
		Builder zooBuilder = template.queryForObject("select *,"
				+ " EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint as waiting_fight_time"
				+ " from users where id=?", zooMapper, userId);
		List<ZooBuildings> buildings = getBuildings(userId);

		zooBuilder.setBuildings(buildings);
		return zooBuilder.build();
	}

	private List<ZooBuildings> getBuildings(Integer userId) {
		return template.query("select * from animal where user_id=? order by animal_type", zooBuildingMapper, userId);
	}

	@Override
	public Zoo buy(Integer userId, String animal) {
		double money = updateAndGetMoney(userId);
		int count = template.queryForObject(
				"select count from animal where user_id=? and animal_type=?", Integer.class, userId,
				resourcesService.index(animal));
		Building type = resourcesService.type(animal);
		double buildCost = type.buildCost(count);
		if (money < buildCost) {
			return getZooNoUpdate(userId);
		}
		template.update("update animal set count=count+1"
				+ " where user_id=? and animal_type=?", userId, resourcesService.index(animal));
		template.update("update users set money=money-? where id=?", buildCost, userId);
		if (count == 0) {
			Building next = resourcesService.nextType(animal);
			if (next != null) {
				int nextIndex = resourcesService.index(next.getName());
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
				resourcesService.index(animal));
		Building type = resourcesService.type(animal);
		double upgradeCost = type.upgradeCost(level);
		if (money < upgradeCost) {
			return getZooNoUpdate(userId);
		}
		template.update("update animal set level=level+1"
				+ " where user_id=? and animal_type=?", userId, resourcesService.index(animal));
		template.update("update users set money=money-? where id=?", upgradeCost, userId);
		return updateIncomeAndGetZoo(userId);
	}

	public void updateIncome(Integer userId) {
		updateIncome(userId, getBuildings(userId));
	}

	private double updateIncome(Integer userId, List<ZooBuildings> buildings) {
		final double newIncome = buildings.stream().mapToDouble(ZooBuildings::getIncome).sum();
		template.update("update users set income=? where id=?", newIncome, userId);
		return newIncome;
	}

	public Zoo updateIncomeAndGetZoo(Integer userId) {
		final Zoo zoo = getZooNoUpdate(userId);
		final double newIncome = updateIncome(userId, zoo.getBuildings());
		return zoo.withIncome(newIncome);
	}

}
