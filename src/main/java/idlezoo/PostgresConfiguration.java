package idlezoo;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

@Profile("postgres")
@ComponentScan
@SpringBootConfiguration
@EnableAutoConfiguration
public class PostgresConfiguration {

}
