package com.mrdeun.java_analyzer.example;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class BarTest {
    @BeforeEach
    void resetCounter() {
        // Reset the static counter before each test
        ReflectionTestUtils.setField(Bar.class, "counter", 0);
    }

    @Test
    void testIncrement() {
        // First call
        String result1 = Bar.increment();
        assertEquals("1", result1);

        // Second call
        String result2 = Bar.increment();
        assertEquals("2", result2);
    }

    @Test
    void testIncrement2() {
        // Mock Baz and inject into static field
        Baz mockBaz = Mockito.mock(Baz.class);
        // The counter is 0, ++counter makes it 1
        Mockito.when(mockBaz.deepMethod(1)).thenReturn("deep-1");
        ReflectionTestUtils.setField(Bar.class, "baz", mockBaz);

        Bar bar = new Bar();
        String result = bar.increment2();
        assertEquals("deep-1", result);
        Mockito.verify(mockBaz).deepMethod(1);
    }

    @Test
    void testIncrement2_MultipleCalls() {
        Baz mockBaz = Mockito.mock(Baz.class);
        // First call: counter goes from 0 to 1
        Mockito.when(mockBaz.deepMethod(1)).thenReturn("deep-1");
        // Second call: counter goes from 1 to 2
        Mockito.when(mockBaz.deepMethod(2)).thenReturn("deep-2");
        ReflectionTestUtils.setField(Bar.class, "baz", mockBaz);

        Bar bar = new Bar();
        String result1 = bar.increment2();
        assertEquals("deep-1", result1);
        String result2 = bar.increment2();
        assertEquals("deep-2", result2);
        Mockito.verify(mockBaz).deepMethod(1);
        Mockito.verify(mockBaz).deepMethod(2);
    }
}
