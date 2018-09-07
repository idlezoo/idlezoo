package idlezoo

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.security.Principal
import java.sql.Statement
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class IdUser(val id: Int?, username: String, password: String) : User(username, password, emptyList<GrantedAuthority>())

@RestController
class UserController {
    @GetMapping("/user")
    fun user(user: Principal?): Principal? = user
}

@RestController
class RegisterController(private val usersService: UsersService) {

    @PostMapping("/createuser")
    fun user(@RequestParam username: String, @RequestParam password: String) =
            if (usersService.addUser(username, password)) {
                HttpStatus.OK
            } else {
                HttpStatus.FORBIDDEN
            }
}

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

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
@ConditionalOnWebApplication
class SecurityConfiguration(private val usersService: UsersService, private val passwordEncoder: PasswordEncoder) : WebSecurityConfigurerAdapter() {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:9000",
                        "https://idlezoo.github.io").allowCredentials(true)
            }
        }
    }

    override fun configure(http: HttpSecurity) {
        http.cors().and()
                .formLogin()
                .loginPage("/#/login")
                .loginProcessingUrl("/login")
                .successHandler(MyAuthenticationSuccessHandler())
                .and().logout()
                .and().authorizeRequests().antMatchers("/game/*").authenticated()
                .antMatchers("/admin/*").hasAuthority("ADMIN")
                .anyRequest().permitAll()
                .and().csrf().disable()
    }

    internal class MyAuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {

        override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                             authentication: Authentication) {
            // This is actually not an error, but an OK message. It is sent to avoid redirects.
            response.sendError(HttpServletResponse.SC_OK)
        }
    }

    @Throws(Exception::class)
    public override fun configure(auth: AuthenticationManagerBuilder) {
        auth
                .userDetailsService(usersService)
                .passwordEncoder(passwordEncoder)

    }
}