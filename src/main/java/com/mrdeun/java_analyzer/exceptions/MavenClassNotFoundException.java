package com.mrdeun.java_analyzer.exceptions;

public class MavenClassNotFoundException extends RuntimeException {
    public MavenClassNotFoundException() {
        super("Class has not been found in maven repository");
    }
}
