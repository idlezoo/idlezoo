package idlezoo.game.services.postgres;

import org.springframework.test.context.ActiveProfiles;

import idlezoo.game.services.AbstractTopServiceTest;

@ActiveProfiles({"postgres", "local"})
public class TopServicePostgresTest extends AbstractTopServiceTest {

}
