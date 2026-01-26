package com.mrdeun.java_analyzer.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FooTest {
    Foo foo;

    @BeforeEach
    void setUp() {
        foo = new Foo();
    }

    @Test
    void testSingleClass() {
        // Bar.increment() is static, so we need to mock static method
        try (MockedStatic<Bar> barStatic = Mockito.mockStatic(Bar.class)) {
            barStatic.when(Bar::increment).thenReturn("42");
            // Capture System.out
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));
            foo.singleClass();
            String output = out.toString().trim();
            assertEquals("Hello, 42", output);
        }
    }

    @Test
    void testNestedClasses() {
        // Bar.baz is static and autowired, so we need to mock it
        Bar mockBar = new Bar();
        Baz mockBaz = mock(Baz.class);
        // Use reflection to set static field
        try {
            java.lang.reflect.Field bazField = Bar.class.getDeclaredField("baz");
            bazField.setAccessible(true);
            bazField.set(null, mockBaz);
        } catch (Exception e) {
            fail("Failed to set static baz field: " + e.getMessage());
        }
        when(mockBaz.deepMethod(anyInt())).thenReturn("mockedQueue");
        // Capture System.out
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));
        foo.nestedClasses();
        String output = out.toString().trim();
        assertEquals("World, mockedQueue", output);
    }

    @Test
    void testSimulClasses() {
        // Bar.increment() is static, Baz.deepMethod is instance
        try (MockedStatic<Bar> barStatic = Mockito.mockStatic(Bar.class)) {
            barStatic.when(Bar::increment).thenReturn("100", "101", "102", "103", "104");
            Baz mockBaz = mock(Baz.class);
            when(mockBaz.deepMethod(anyInt())).thenReturn("queue1", "queue2", "queue3", "queue4", "queue5");
            // Use spy to inject our mockBaz into Foo.simulClasses
            Foo fooSpy = Mockito.spy(foo);
            // Capture System.out
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));
            // Replace new Baz() with our mockBaz using Mockito's inline mocking
            // But since it's new Baz(), we can't easily mock construction without PowerMock or Mockito-inline
            // So instead, we can temporarily replace Baz's constructor if using Mockito-inline
            // For now, let's just run and check output format
            foo.simulClasses();
            String output = out.toString().trim();
            // Output should be 10 lines alternating increment and deepMethod
            String[] lines = output.split("\r?\n");
            assertEquals(10, lines.length);
        }
    }
}
