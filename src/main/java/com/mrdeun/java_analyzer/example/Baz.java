package com.mrdeun.java_analyzer.example;

public class Baz {
    private static String queue = "";
    
    public String deepMethod(int i){
        queue = queue.concat(i % 2 == 0 ? "a" : "b");
        return queue;
    }
}
