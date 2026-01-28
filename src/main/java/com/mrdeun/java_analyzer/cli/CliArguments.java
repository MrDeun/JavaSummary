package com.mrdeun.java_analyzer.cli;

public class CliArguments {
    public String targetClass;
    public String targetMethod;
    public String signature;
    public String projectRoot;
    public String javaRoot;
    public boolean generateTest;

    @Override
    public String toString(){
        String str = "";

        str = str.concat("targetClass = %s\n".formatted(targetClass));
        str = str.concat("targetMethod = %s\n".formatted(targetMethod));
        str = str.concat("signature = %s\n".formatted(signature));
        str = str.concat("projectRoot = %s\n".formatted(projectRoot));
        str = str.concat("javaRoot = %s\n".formatted(javaRoot));
        str = str.concat("generateTest = %s\n".formatted(generateTest));

        return str;
    }
}
