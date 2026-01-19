package com.mrdeun.java_analyzer.cli;

public class CliParser {

    public static CliArguments parse(String[] args) {
        CliArguments cli = new CliArguments();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--class" -> cli.targetClass = args[++i];
                case "--method" -> cli.targetMethod = args[++i];
                case "--signature" -> cli.signature = args[++i];
                case "--project-root" -> cli.projectRoot = args[++i];
            }
        }

        validate(cli);
        return cli;
    }

    private static void validate(CliArguments cli) {
        if (cli.targetClass == null ||
                cli.targetMethod == null ||
                cli.signature == null) {

            throw new IllegalArgumentException("""
                    Required arguments:
                      --class <FQCN>
                      --method <methodName>
                      --signature <methodSignature>
                    """);
        }
    }
}