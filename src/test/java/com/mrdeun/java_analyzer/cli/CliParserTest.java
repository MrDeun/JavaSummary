package com.mrdeun.java_analyzer.cli;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CliParserTest {
    
    @Test
    public void validateParser(){
        String[] args = {"--java-root", "src", "--class", "com.mrdeun.example.Foo"};

        var cliArgs = new CliParser().parse(args);

        System.out.println(cliArgs.toString());

    }

}
