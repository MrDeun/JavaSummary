package com.mrdeun.java_analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import com.mrdeun.java_analyzer.cli.CliArguments;
import com.mrdeun.java_analyzer.cli.CliParser;
import com.mrdeun.java_analyzer.client.MavenRepositroryClient;
import com.mrdeun.java_analyzer.client.OpenAIClient;
import com.mrdeun.java_analyzer.exceptions.OpenAIApiKeyIsMissingException;
import com.mrdeun.java_analyzer.helpers.Helpers;
import com.mrdeun.java_analyzer.workspace.WorkspaceRunner;

@SpringBootApplication
public class JavaAnalyzerApplication implements CommandLineRunner {
 @Value("${analyzer.summary_file}")
    private String summaryPath;  // Non-static
    
    @Autowired
    private OpenAIClient client;  // Let Spring inject this
    
    // @Autowired
    // private MavenRepositroryClient mavenClient;
    
    public static void main(String[] args) {
        SpringApplication.run(JavaAnalyzerApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        CliArguments cli = CliParser.parse(args);
        System.out.println(cli.toString());
        WorkspaceRunner runner = new WorkspaceRunner(client/* , mavenClient */);
        Map<String, Object> result = new HashMap<>();
        
        try {
            result = runner.run(cli);
        } catch (Exception err) {
            result.put("status", "Unexpected Failure");
            result.put("result", err.toString());
        }

        System.out.println(result);
        
        if ("SOLVED".equals(result.get("status"))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(summaryPath, false))) {
                writer.write(result.get("content").toString());
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

}
