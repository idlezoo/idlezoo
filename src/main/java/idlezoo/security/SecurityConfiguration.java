
package idlezoo.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private UsersService usersService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurerAdapter() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:9000",
            "https://idlezoo.github.io");
      }
    };
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().and()
        .formLogin()
        .loginPage("/#/login")
        .loginProcessingUrl("/login")
        .successHandler(new MyAuthenticationSuccessHandler())
        .and().logout()
        .and().authorizeRequests().antMatchers("/game/*").authenticated()
        .antMatchers("/admin/*").hasAuthority("ADMIN")
        .anyRequest().permitAll()
        .and().csrf().disable();
  }

  public static class MyAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // This is actually not an error, but an OK message. It is sent to avoid redirects.
        response.sendError(HttpServletResponse.SC_OK);
    }
}

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
        .userDetailsService(usersService)
        .passwordEncoder(passwordEncoder());

  }
}