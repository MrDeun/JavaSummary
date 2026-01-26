package com.mrdeun.java_analyzer.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrdeun.java_analyzer.client.MavenRepositroryClient;
import com.mrdeun.java_analyzer.client.OpenAIClient;
import com.mrdeun.java_analyzer.dto.Dependency;
import com.mrdeun.java_analyzer.dto.LibraryInfo;
import com.mrdeun.java_analyzer.dto.PomInfo;
import com.mrdeun.java_analyzer.helpers.Helpers;
import com.mrdeun.java_analyzer.helpers.PomAnalyzer;
import com.mrdeun.java_analyzer.prompts.Prompts;

public class WorkspaceRunner {

    private final OpenAIClient openai;
    private final MavenRepositroryClient maven;

    public WorkspaceRunner(OpenAIClient openai, MavenRepositroryClient maven) {
        this.openai = openai;
        this.maven = maven;
    }

    private Map<String, Object> userMsg(String text) {
        return Map.of(
                "role", "user",
                "content", List.of(Map.of("type", "text", "text", text)));
    }

    private Map<String, Object> systemMsg(String text) {
        return Map.of(
                "role", "system",
                "content", List.of(Map.of("type", "text", "text", text)));
    }

    public Map<String, Object> run(
            String targetClass,
            String targetMethod,
            String projectRoot,
            boolean generateTest) throws Exception {
        if (projectRoot == null) {
            projectRoot = System.getProperty("user.dir");
        }
        PomInfo pomInfo = PomAnalyzer.analyzePom(projectRoot);
        if (pomInfo.isEmpty()) {
            pomInfo = PomAnalyzer.analyzeGradle(projectRoot);
        }

        String content = Helpers.loadJavaClass(targetClass);
        // Build the initial input prompt with target method information
        String initialInput = String.format("""
                TARGET METHOD (IMMUTABLE):
                - Class: %s
                - Method: %s
                - Content: %s
                Rules:
                - Analyze ONLY this method
                - Ignore other methods unless they are called by it
                - Never change the target
                """, targetClass, targetMethod != null ? targetMethod : "\"\"", content);

        WorkspaceState state = new WorkspaceState();
        state.javaFiles.add(initialInput);
        if (!pomInfo.isEmpty()) {
            String dependencyReport = PomAnalyzer.generateLibraryInfoReport(pomInfo);
            state.javaFiles.add(dependencyReport);
        }
        final int MAX_ITERATIONS = 10;
        List<String> unresolved = new ArrayList<>();

        for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
            System.out.println("Iteration %d\n %s".formatted(iteration, state.toString()));
            List<Map<String, Object>> history = new ArrayList<>();

            // Inject all known Java files and context
            for (String file : state.javaFiles) {
                history.add(userMsg(file));
            }

            // ---- JAVA ANALYZER ----
            history.add(systemMsg(Prompts.JAVA_ANALYZER));
            JsonNode javaAnalyzerResp = openai.call(history);
            history.add(userMsg(openai.extractText(javaAnalyzerResp)));

            // ---- AGENT (dependency lookup) ----
            history.add(systemMsg(Prompts.AGENT));
            JsonNode agentResp = openai.call(history);
            String agentText = openai.extractText(agentResp);

            // ---- CLASSIFICATION ----
            List<Map<String, Object>> classifyInput = List.of(
                    systemMsg(Prompts.CLASSIFY),
                    userMsg(agentText));

            JsonNode classifyResp = openai.call(classifyInput);
            String category = openai.extractText(classifyResp);
            System.out.println(category);

            // Check if solvable
            if (!category.contains("Not Solvable")) {
                // Generate summary
                history.add(systemMsg(Prompts.AGENT_SUMMARY));
                JsonNode summaryResp = openai.call(history);

                if (generateTest) {
                    history.add(systemMsg(Prompts.TEST_GENERATION));
                    JsonNode testGenerate = openai.call(history);
                    Map<String, Object> testSourceCode = new ObjectMapper().readValue(openai.extractText(testGenerate),
                            HashMap.class);
                    System.out.println(testSourceCode);
                    try {
                        Helpers.saveTestJavaClass(testSourceCode.get("fqcn").toString(),
                                testSourceCode.get("test_code").toString());

                    } catch (IOException err) {
                        System.err.println(err);
                    }

                }

                return Map.of(
                        "status", "SOLVED",
                        "iterations", iteration,
                        "className", targetClass,
                        "content", openai.extractText(summaryResp),
                        "result", openai.extractText(summaryResp));
            }

            // ---- EXTRACT MISSING CLASSES ----
            List<Map<String, Object>> extractInput = List.of(
                    systemMsg(Prompts.EXTRACT_MISSING),
                    userMsg(agentText));

            JsonNode missingResp = openai.call(extractInput);
            List<String> missingClasses = new ObjectMapper().readValue(
                    openai.extractText(missingResp),
                    List.class);

            System.out.println("Missing classes: ");
            missingClasses.stream().forEach(c -> {
                System.out.println(c);
            });
            boolean addedSomething = false;
            List<String> knownLibraries = new ArrayList<>();
            for (String missingClass : missingClasses) {
                if (state.alreadyAdded.contains(missingClass)) {
                    continue;
                }

                try {
                    String source = Helpers.loadJavaClass(missingClass);

                    String formatted = """
                            ### JAVA FILE
                            Class: %s
                            ```java
                            %s
                            ```
                            """.formatted(missingClass, source);

                    state.javaFiles.add(formatted);
                    state.alreadyAdded.add(missingClass);
                    addedSomething = true;

                } catch (IOException e) {
                    System.out.println("Checking Maven Central for: " + missingClass);
                    LibraryInfo libInfo = maven.lookupClass(missingClass);
                    if (libInfo != null) {
                        // Found in Maven - mark as known
                        String libraryDetails = maven.getLibraryDetails(libInfo);

                        String knownLibInfo = """
                                ### THIRD-PARTY LIBRARY (KNOWN)
                                Class: %s
                                Status: AVAILABLE IN MAVEN CENTRAL

                                %s

                                **Important**: This class is from a well-known library and should be treated as AVAILABLE.
                                Do not mark this as missing. Assume standard behavior for this library.
                                """
                                .formatted(missingClass, libraryDetails);

                        state.javaFiles.add(knownLibInfo);
                        state.alreadyAdded.add(missingClass);
                        knownLibraries.add(missingClass + " (" + libInfo.getCoordinate() + ")");
                        addedSomething = true;
                        System.out.println("✓ Found in Maven: " + libInfo.getCoordinate());

                    } else {
                        // Truly unknown
                        unresolved.add(missingClass);
                        System.out.println("✗ Not found: " + missingClass);
                    }
                }
            }

            // ---- TERMINATION SAFETY ----
            if (!addedSomething) {
                return Map.of(
                        "status", "STUCK",
                        "iterations", iteration,
                        "reason", "Missing classes not available",
                        "unresolved", unresolved,
                        "result", agentText);
            }
        }

        return Map.of(
                "status", "MAX_ITERATIONS_REACHED",
                "iterations", MAX_ITERATIONS,
                "result", "Could not solve within " + MAX_ITERATIONS + " iterations");
    }

}
