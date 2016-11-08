package idlezoo.game.services.postgres;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.domain.TopEntry;
import idlezoo.game.services.TopService;

@Service
@Transactional
@Profile("postgres")
public class TopServicePostgres implements TopService {

  @Override
  public List<TopEntry<Integer>> building(String building) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TopEntry<Double>> income() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TopEntry<Integer>> wins() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TopEntry<Long>> championTime() {
    // TODO Auto-generated method stub
    return null;
  }

}
