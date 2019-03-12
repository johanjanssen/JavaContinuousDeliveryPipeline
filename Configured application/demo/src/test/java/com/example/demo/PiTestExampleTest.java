package com.example.demo;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PiTestExampleTest {

    private PiTestExample piTestExample = new PiTestExample();

    // This test is pretty lousy, and PIT will create several mutants that will survive this test:
    @Test
    public void testGetValueOrBoundary() {
        assertTrue(piTestExample.getValueOrBoundary(20, 100) < 101);
    }
}