package idlezoo


import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertThrows

class AssertTest {

    @Test
    fun testAssert() {
        assertThrows(AssertionError::class.java) { assert(false) }
    }
}
