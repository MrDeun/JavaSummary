package com.mrdeun.java_analyzer.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.mrdeun.java_analyzer.dto.Dependency;

public class Helpers {
    public static String[] packageToPath(String packageString, String javaRoot) {
        var javaPaths = javaRoot.split("\\/");
        Path base = Paths.get(System.getProperty("user.dir"),
                "src", "main", "java");

        String[] parts = packageString.split("/");

        for (int i = 0; i < parts.length - 1; i++) {
            base = base.resolve(parts[i]);
        }

        String className = parts[parts.length - 1];
        return new String[] { base.toString(), className };
    }

    public static List<String> readFile(String dir, String className)
            throws IOException {

        Path file = Paths.get(dir, className + ".java");

        if (!Files.exists(file)) {
            throw new FileNotFoundException("Missing file: " + file);
        }

        return Files.readAllLines(file);
    }

    /**
     * Convenience method: fully-qualified class → Java source as String
     */
    public static String loadJavaClass(String fqcn, String javaRoot) throws IOException {
        String[] parts = packageToPath(fqcn, javaRoot);
        List<String> lines = readFile(parts[0], parts[1]);
        return String.join("\n", lines);
    }

    public static void saveTestJavaClass(String fqcn, String source) throws IOException {
        Path base = Paths.get(System.getProperty("user.dir"), "src", "test", "java");
        String[] parts = fqcn.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            base = base.resolve(parts[i]);
        }

        if(Files.notExists(base)){
            Files.createDirectories(base);
        }

        String className = parts[parts.length - 1].concat(".java");
        var file = base.resolve(className).toFile();
        file.createNewFile();
        try (
                FileWriter fw = new FileWriter(file);) {
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(source);
            writer.close();

        } catch (IOException err) {
            throw err;
        }
    }
}
