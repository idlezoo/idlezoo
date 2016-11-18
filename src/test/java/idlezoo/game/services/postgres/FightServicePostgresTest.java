package idlezoo.game.services.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;
import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({ "postgres", "local" })
@Transactional
public class FightServicePostgresTest {
	private static final String ZOO1 = "1", ZOO2 = "2";
	private int zoo1id, zoo2id;

	@Autowired
	private FightService fightService;
	@Autowired
	private GameService gameService;
	@Autowired
	private UsersService usersService;
	@Autowired
	private ResourcesService resourcesService;
	@Autowired
	private JdbcTemplate template;

	@Before
	public void setup() {
		assertTrue(usersService.addUser(ZOO1, ""));
		assertTrue(usersService.addUser(ZOO2, ""));

		zoo1id = template.queryForObject("select id from users where username=?", Integer.class, ZOO1);
		zoo2id = template.queryForObject("select id from users where username=?", Integer.class, ZOO2);

		template.update("update users set money=1000000 where username=?", ZOO1);
		template.update("update users set money=1000000 where username=?", ZOO2);
		template.update("update arena set waiting_user_id=null");
	}

	@Test
	public void isWaitingAfterStart() {
		assertNull(fightService.fight(zoo1id));
		assertTrue(gameService.getZoo(zoo1id).isWaitingForFight());
	}

	@Test
	public void test1vs0() {
		gameService.buy(zoo1id, resourcesService.firstName());
		assertNull(fightService.fight(zoo1id));
		assertEquals(ZOO1, fightService.fight(zoo2id).getName());

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(1, zoo1.getFightWins());
		assertEquals(1, zoo1.getBuildings().get(0).getNumber());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(0, zoo2.getFightWins());
	}

	@Test
	public void test1vs1() {
		gameService.buy(zoo1id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.firstName());

		fightService.fight(zoo1id);
		fightService.fight(zoo2id);

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(1, zoo1.getFightWins());
		assertEquals(0, zoo1.getBuildings().get(0).getNumber());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(0, zoo2.getFightWins());
	}

	@Test
	public void test0vs1() {
		gameService.buy(zoo2id, resourcesService.firstName());
		fightService.fight(zoo1id);
		fightService.fight(zoo2id);

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(0, zoo1.getFightWins());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(1, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getFightWins());
	}

	@Test
	public void test1vs2() {
		gameService.buy(zoo1id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.firstName());

		fightService.fight(zoo1id);
		fightService.fight(zoo2id);

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(0, zoo1.getFightWins());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(1, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getFightWins());
	}

	@Test
	public void test1and1vs1and2() {
		gameService.buy(zoo1id, resourcesService.firstName());
		gameService.buy(zoo1id, resourcesService.secondName());

		gameService.buy(zoo2id, resourcesService.firstName());
		gameService.buy(zoo2id, resourcesService.secondName());
		gameService.buy(zoo2id, resourcesService.secondName());

		fightService.fight(zoo1id);
		fightService.fight(zoo2id);

		Zoo zoo1 = gameService.getZoo(zoo1id);
		assertEquals(1, zoo1.getFightWins());
		assertEquals(0, zoo1.getBuildings().get(0).getNumber());
		assertEquals(0, zoo1.getBuildings().get(1).getNumber());

		Zoo zoo2 = gameService.getZoo(zoo2id);
		assertEquals(0, zoo2.getBuildings().get(0).getNumber());
		assertEquals(1, zoo2.getBuildings().get(1).getNumber());
		assertEquals(0, zoo2.getFightWins());
	}

}
