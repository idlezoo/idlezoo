package idlezoo.security

import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Statement

@Service
@Transactional
class UsersService(private val template: JdbcTemplate, private val passwordEncoder: PasswordEncoder) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails? {
        try {
            return template.queryForObject("select id, username, password from users where lower(username)=lower(?)",
                    RowMapper { res, _ ->
                        IdUser(res.getInt("id"),
                                res.getString("username"),
                                res.getString("password")
                        )
                    }, username)!!
        } catch (empty: EmptyResultDataAccessException) {
            throw UsernameNotFoundException("User $username not found")
        }

    }

    fun addUser(username: String?, password: String): Boolean {
        try {
            val idHolder = GeneratedKeyHolder()
            template.update({ con ->
                val pst = con.prepareStatement("insert into users(username, password) values(?,?) returning id", Statement.RETURN_GENERATED_KEYS)
                pst.setString(1, username)
                pst.setString(2, passwordEncoder.encode(password))
                pst
            }, idHolder)
            template.update("insert into animal(user_id, animal_type) values(?,?)",
                    idHolder.key!!.toInt(), 0)
            return true
        } catch (duplicate: DuplicateKeyException) {
            return false
        }

    }

}
