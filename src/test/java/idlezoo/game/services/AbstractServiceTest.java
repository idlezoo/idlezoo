package idlezoo.game.services;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public abstract class AbstractServiceTest {

  protected static final String ZOO1 = "1";
	protected Integer zoo1Id;

	@Autowired
	protected UsersService usersService;

	@Autowired
	protected ResourcesService resourcesService;

	@Before
	public void setup() {
		assertTrue(usersService.addUser(ZOO1, ""));
		zoo1Id = getZooId(ZOO1);
	}


	protected abstract int getZooId(String zooName);

	protected abstract void setMoney(int zooId, double value);

}
