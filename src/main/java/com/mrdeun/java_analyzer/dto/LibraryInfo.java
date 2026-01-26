package com.mrdeun.java_analyzer.dto;

public class LibraryInfo {
    public final String groupId;
    public final String artifactId;
    public final String version;
    public final String repository;

    public LibraryInfo(String groupId, String artifactId, String version, String repository) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.repository = repository;
    }

    public String getCoordinate() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public String toString() {
        return getCoordinate();
    }
}
