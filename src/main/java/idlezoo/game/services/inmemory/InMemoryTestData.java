package idlezoo.game.services.inmemory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import idlezoo.security.inmemory.UsersServiceInMemory;

@Component
@Profile("default1")
public class InMemoryTestData implements InitializingBean {

  private final Storage storage;

  private final UsersServiceInMemory usersService;
  
  public InMemoryTestData(Storage storage, UsersServiceInMemory usersService) {
    this.storage = storage;
    this.usersService=usersService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    usersService.addUser("1", "1");
    InMemoryZoo zoo = storage.getZoo("1");
    zoo.setMoney(1e15D);
    zoo.getInMemoryBuildings().get(0).setNumber(1000);
    zoo.computeIncome();
    
  }

}
