package com.mrdeun.java_analyzer.example;

import org.springframework.context.annotation.Bean;

public class Baz {
    private static String queue = "";
    
    @Bean("baz")
    public Baz baz(){
        return new Baz();
    }



    public String deepMethod(int i){
        queue = queue.concat(i % 2 == 0 ? "a" : "b");
        return queue;
    }
}
