package idlezoo.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException

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

    @Throws(Exception::class)
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

        @Throws(IOException::class)
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