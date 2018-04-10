package idlezoo.game;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertTest {

    @Test
    void testAssert() {
        assertThrows(AssertionError.class,
                () -> {
                    assert false;
                }
        );
    }
}
