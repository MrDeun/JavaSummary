package com.mrdeun.java_analyzer.example;

import com.mrdeun.java_analyzer.example.Bar;

public class Foo {
    public void singleClass() {
        System.out.println("Hello, " + new Bar().increment());
    }

    public void nestedClasses() {
        System.out.println("World, " + new Bar().increment2());
    }

    public void simulClasses() {
        Bar bar = new Bar(); 
        Baz baz = new Baz();
        for (int i = 0; i < 5; i++) {
            System.out.println(bar.increment());
            System.out.println(baz.deepMethod(i));
        }
    }
}
