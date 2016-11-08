package idlezoo.game.services.postgres;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService;

@Service
@Transactional
@Profile("postgres")
public class FightServicePostgres implements FightService {

  @Override
  public Zoo fight(String username) {
    // TODO Auto-generated method stub
    return null;
  }

}
