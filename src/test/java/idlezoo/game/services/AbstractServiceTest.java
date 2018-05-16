package idlezoo.game.services;

import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.security.UsersService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = NONE)
@Transactional
abstract class AbstractServiceTest {

    static final String ZOO1 = "1";
    Integer zoo1Id;

    final JdbcTemplate template;
    final UsersService usersService;
    final ResourcesService resourcesService;

    protected AbstractServiceTest(JdbcTemplate template, UsersService usersService, ResourcesService resourcesService) {
        this.template = template;
        this.usersService = usersService;
        this.resourcesService = resourcesService;
    }

    @BeforeEach
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
