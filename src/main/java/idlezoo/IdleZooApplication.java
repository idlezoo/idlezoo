package idlezoo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootConfiguration
public class IdleZooApplication {

  public static void main(String[] args) {
    SpringApplication.run(IdleZooApplication.class, args);
  }
}
