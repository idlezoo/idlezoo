package idlezoo;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("postgres")
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class PostgresConfiguration {

}
