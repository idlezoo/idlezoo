package idlezoo;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("default")
@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class})
public class InMemoryConfiguration {

}
