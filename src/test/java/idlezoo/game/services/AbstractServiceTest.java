package idlezoo.game.services;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public abstract class AbstractServiceTest {

    static final String ZOO1 = "1";
    Integer zoo1Id;

    @Autowired
    JdbcTemplate template;
    @Autowired
    UsersService usersService;
    @Autowired
    ResourcesService resourcesService;

    @Before
    public void setup() {
        assertTrue(usersService.addUser(ZOO1, ""));
        zoo1Id = getZooId(ZOO1);
    }

    int getZooId(String zooName) {
        return template.queryForObject("select id from users where username=?", Integer.class, zooName);
    }

    void setMoney(int zooId, double value) {
        template.update("update users set money=? where id=?", value, zooId);
    }

}
