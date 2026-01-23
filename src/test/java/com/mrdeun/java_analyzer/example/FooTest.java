package com.mrdeun.java_analyzer.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class FooTest {
    @Test
    public void testSingleClassPrintsIncrementedCounter() {
        // Set up to capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Reset Bar.counter to 0 for test repeatability (reflection hack for test, not for prod)
        try {
            java.lang.reflect.Field field = Bar.class.getDeclaredField("counter");
            field.setAccessible(true);
            field.set(null, 0);
        } catch (Exception e) {
            fail("Could not reset Bar.counter: " + e.getMessage());
        }

        Foo foo = new Foo();
        foo.singleClass();

        // Restore System.out
        System.setOut(originalOut);

        // Check output
        String output = outContent.toString().trim();
        assertEquals("Hello, 1", output, "singleClass should print 'Hello, 1'");
    }
}
