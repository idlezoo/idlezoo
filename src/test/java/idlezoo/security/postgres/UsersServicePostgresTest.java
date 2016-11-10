package idlezoo.security.postgres;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles({ "postgres", "local" })
public class UsersServicePostgresTest {

	@Autowired
	private UsersService usersService;

	@Test(expected = UsernameNotFoundException.class)
	public void testNotFound() {
		usersService.loadUserByUsername("no_such_user");
	}

	@Test
	public void testAddUser() {
		assertTrue(usersService.addUser("1", ""));
		assertNotNull(usersService.loadUserByUsername("1"));
		assertFalse(usersService.addUser("1", ""));
	}
}
