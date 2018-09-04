package idlezoo.game.services

import org.junit.Assert.assertTrue
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

import idlezoo.security.UsersService

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = NONE)
@Transactional
abstract class AbstractServiceTest(val template: JdbcTemplate, val usersService: UsersService, val resourcesService: ResourcesService) {
    var zoo1Id: Int = 0

    @BeforeEach
    fun setup() {
        assertTrue(usersService.addUser(ZOO1, ""))
        zoo1Id = getZooId(ZOO1)
    }

    fun getZooId(zooName: String): Int {
        return template.queryForObject("select id from users where username=?", Int::class.java, zooName)
    }

    fun setMoney(zooId: Int, value: Double) {
        template.update("update users set money=? where id=?", value, zooId)
    }

    companion object {
        const val ZOO1 = "1"
    }

}
