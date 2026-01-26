package com.mrdeun.java_analyzer.dto;

import java.util.ArrayList;
import java.util.List;

public class PomInfo {
    public String groupId;
    public String artifactId;
    public String version;
    public List<Dependency> dependencies = new ArrayList<>();

    public boolean isEmpty() {
        return dependencies.isEmpty();
    }
}
