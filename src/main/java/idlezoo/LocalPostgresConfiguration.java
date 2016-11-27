package idlezoo;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("local")
@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude = {
    RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class,
    SessionAutoConfiguration.class})
public class LocalPostgresConfiguration {

}
