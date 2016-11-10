package idlezoo.game.services.postgres;

import org.junit.Ignore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import idlezoo.game.services.AbstractFightServiceTest;

@Ignore
@ActiveProfiles({"postgres", "local"})
@Transactional
public class FightServicePostgresTest extends AbstractFightServiceTest{

}
