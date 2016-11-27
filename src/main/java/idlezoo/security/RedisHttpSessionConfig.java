package idlezoo.security;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import redis.clients.jedis.JedisShardInfo;

@EnableRedisHttpSession
@Profile("heroku")
public class RedisHttpSessionConfig {

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() throws URISyntaxException {
    URI redisUri = new URI(System.getenv("REDIS_URL"));
    return new JedisConnectionFactory(new JedisShardInfo(redisUri));
  }
}