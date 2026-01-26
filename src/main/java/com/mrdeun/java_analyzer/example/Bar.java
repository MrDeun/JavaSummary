
package com.mrdeun.java_analyzer.example;

import org.springframework.beans.factory.annotation.Autowired;


public class Bar {
    private static Integer counter = 0;
    
    @Autowired
    private static Baz baz;

    public static String increment() {
        counter++;
        return counter.toString();
    }

    public String increment2() {
        return baz.deepMethod(++counter);
    }
}