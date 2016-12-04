package idlezoo.game.services.inmemory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

@Configuration
@Profile("default")
public class InMemoryTestConfiguration {

  @Bean
  public PlatformTransactionManager transactionManager(){
    return new NoopTransactionManager();
  }
  
  public static class NoopTransactionManager implements PlatformTransactionManager{

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition)
        throws TransactionException {
      return new SimpleTransactionStatus();
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
      //no code
      
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
      // no code
    }
    
  }
  
}
