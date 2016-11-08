package idlezoo.game.services.postgres;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.GameService;

@Service
@Transactional
@Profile("postgres")
public class GameServicePostgres implements GameService{

  @Override
  public boolean createZoo(String username) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Zoo getZoo(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Zoo buy(String name, String animal) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Zoo upgrade(String name, String animal) {
    // TODO Auto-generated method stub
    return null;
  }

}
