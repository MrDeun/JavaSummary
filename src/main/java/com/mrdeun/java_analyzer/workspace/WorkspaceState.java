package com.mrdeun.java_analyzer.workspace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkspaceState {
    public List<String> javaFiles = new ArrayList<>();
    public Set<String> alreadyAdded = new HashSet<>();
    public Set<String> knownThirdPartyLibraries = new HashSet<>();
    boolean solved = false;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WorkspaceState {\n");
        sb.append("  solved: ").append(solved).append("\n");
        sb.append("  javaFiles: ").append(javaFiles.size()).append(" files\n");
        sb.append("  alreadyAdded: ").append(alreadyAdded).append("\n");

        if (!javaFiles.isEmpty()) {
            sb.append("  files:\n");
            for (int i = 0; i < javaFiles.size(); i++) {
                String file = javaFiles.get(i);
                String preview = file.length() > 100
                        ? file.substring(0, 100) + "..."
                        : file;
                sb.append("    [").append(i).append("] ").append(preview.replace("\n", " ")).append("\n");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
