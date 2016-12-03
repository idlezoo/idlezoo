package idlezoo.game;

import org.junit.Test;

public class AssertTest {

  @Test(expected=AssertionError.class)
  public void testAssert() {
    assert false;
  }
}
