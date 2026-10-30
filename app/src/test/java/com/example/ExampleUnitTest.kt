package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testConfidenceBounds() {
        val authenticProb = 20.0f
        val calculatedConf = (100f - authenticProb).coerceIn(68f, 97f)
        assertEquals(80.0f, calculatedConf, 0.01f)

        val manipulatedProb = 85.0f
        val manipulatedConf = manipulatedProb.coerceIn(65f, 98.5f)
        assertEquals(85.0f, manipulatedConf, 0.01f)
    }

    @Test
    fun testArithmeticSanity() {
        assertEquals(4, 2 + 2)
    }
}
