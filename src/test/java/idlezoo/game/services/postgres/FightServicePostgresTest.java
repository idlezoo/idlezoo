package idlezoo.game.services.postgres;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import idlezoo.game.services.AbstractFightServiceTest;

@ActiveProfiles({"postgres", "local"})
public class FightServicePostgresTest extends AbstractFightServiceTest {

  @Autowired
  private JdbcTemplate template;

  @Before
  @Override
  public void setup() {
    super.setup();
    template.update("update arena set waiting_user_id=null");
  }

  @Override
  protected int getZooId(String zooName) {
    return template.queryForObject("select id from users where username=?", Integer.class, zooName);
  }

  @Override
  protected void setMoney(int zooId, double value) {
    template.update("update users set money=? where id=?", value, zooId);
  }

}
