package idlezoo.game.services.postgres;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import idlezoo.game.services.AbstractGameServiceTest;

@ActiveProfiles({"postgres", "local"})
public class GameServicePostgresTest extends AbstractGameServiceTest {

  @Autowired
  private JdbcTemplate template;

  @Override
  protected int getZooId(String zooName) {
    return template.queryForObject("select id from users where username=?", Integer.class, zooName);
  }

  @Override
  protected void setMoney(int zooId, double value) {
    template.update("update users set money=? where id=?", value, zooId);
  }

}
