package idlezoo.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class UsersServiceTest {
    private final UsersService usersService;

    @Autowired
    UsersServiceTest(UsersService usersService) {
        this.usersService = usersService;
    }

    @Test
    void testNotFound() {
        assertThrows(UsernameNotFoundException.class,
                () -> usersService.loadUserByUsername("no_such_user")
        )
        ;
    }

    @Test
    void testAddUser() {
        assertTrue(usersService.addUser("1", ""));
        assertNotNull(usersService.loadUserByUsername("1"));
        assertFalse(usersService.addUser("1", ""));
    }
}
