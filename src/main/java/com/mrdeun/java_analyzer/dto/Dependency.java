package com.mrdeun.java_analyzer.dto;

public class Dependency {
    public final String groupId;
    public final String artifactId;
    public final String version;
    public final String scope;

    public Dependency(String groupId, String artifactId, String version, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
    }

    public String getCoordinate() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public String toString() {
        return getCoordinate();
    }
}
