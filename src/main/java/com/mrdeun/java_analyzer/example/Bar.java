
package com.mrdeun.java_analyzer.example;

public class Bar {
    private static Integer counter = 0;
    private static Baz baz;

    public Bar() {
        this.baz = new Baz();
    }

    public String increment() {
        counter++;
        return counter.toString();
    }

    public String increment2() {
        return baz.deepMethod(++counter);
    }
}