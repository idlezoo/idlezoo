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
  private final RowMapper<ZooBuildings> zooBuildingMapper;
  private final RowMapper<Zoo.Builder> zooMapper;

  public GameServicePostgres(JdbcTemplate template, ResourcesService resourcesService) {
    this.template = template;
    zooBuildingMapper = (res, rowNum) -> {
      Building building = resourcesService.type(res.getString("animal_type"));
      return new ZooBuildings(building, res.getInt("level"), res.getInt("number"));
    };

    zooMapper = (res, rowNum) -> {
      Builder builder = new Zoo.Builder();
      builder.setName(res.getString("username"));
      builder.setFightWins(res.getInt("fight_wins"));
      builder.setIncome(res.getDouble("income"));
      builder.setMoney(res.getDouble("money"));
      return builder;
    };

  }

  
  void updateMoney(String username){
    template.update("update users set money+=income*select EXTRACT(EPOCH FROM now()-last_money_update)"
        + ", last_money_update=now() where username=?", username);
  }
  
  @Override
  public Zoo getZoo(String name) {
    updateMoney(name);
    List<ZooBuildings> buildings = template.query("select * from animal where username=?",
        zooBuildingMapper, name);

    Builder builder = new Zoo.Builder();
    builder.setName(name);
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Zoo buy(String name, String animal) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Zoo upgrade(String name, String animal) {
    // TODO Auto-generated method stub
    return null;
  }

}
