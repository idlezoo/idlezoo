package idlezoo.security

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(SpringExtension::class)
@SpringBootTest
@Transactional
class UsersServiceTest(@Autowired val usersService: UsersService) {

    @Test
    fun testNotFound() {
        assertThrows(UsernameNotFoundException::class.java) { usersService.loadUserByUsername("no_such_user") }
    }

    @Test
    fun testAddUser() {
        assertTrue(usersService.addUser("1", ""))
        assertNotNull(usersService.loadUserByUsername("1"))
        assertFalse(usersService.addUser("1", ""))
    }
}
