package com.mrdeun.java_analyzer.cli;

public class CliParser {

    public static CliArguments parse(String[] args) {
        CliArguments cli = new CliArguments();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--class" -> cli.targetClass = args[++i];
                case "--method" -> cli.targetMethod = args[++i];
                case "--signature" -> cli.signature = args[++i];
                case "--java-root" -> cli.javaRoot = args[++i];
                case "--project-root" -> cli.projectRoot = args[++i];
                case "--generate-test" -> cli.generateTest = true;
            }
        }
        System.out.println(cli.generateTest ? "Test Generation: ON" : "Test Generation: OFF");
        validate(cli);
        return cli;
    }

    private static void validate(CliArguments cli) {
        if (cli.javaRoot == null || cli.javaRoot.isEmpty()) {
            cli.javaRoot = "src/main/java";
        }
        if (cli.targetClass == null) {
            throw new IllegalArgumentException("""
                    Required arguments:
                      --class <FQCN>
                    """);
        }
    }
}