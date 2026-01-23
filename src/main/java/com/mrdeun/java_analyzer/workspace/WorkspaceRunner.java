package com.mrdeun.java_analyzer.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrdeun.java_analyzer.client.OpenAIClient;
import com.mrdeun.java_analyzer.helpers.Helpers;
import com.mrdeun.java_analyzer.prompts.Prompts;

public class WorkspaceRunner {

    private final OpenAIClient openai;

    public WorkspaceRunner(OpenAIClient openai) {
        this.openai = openai;
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
            String signature, boolean generateTest) throws Exception {
        String content = Helpers.loadJavaClass(targetClass);
        // Build the initial input prompt with target method information
        String initialInput = String.format("""
                TARGET METHOD (IMMUTABLE):
                - Class: %s
                - Method: %s
                - Signature: %s
                - Content: %s
                Rules:
                - Analyze ONLY this method
                - Ignore other methods unless they are called by it
                - Never change the target
                """, targetClass, targetMethod, signature, content);

        WorkspaceState state = new WorkspaceState();
        state.javaFiles.add(initialInput);

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
            JsonNode javaAnalyzerResp = openai.call("gpt-4.1", history);
            history.add(userMsg(openai.extractText(javaAnalyzerResp)));

            // ---- AGENT (dependency lookup) ----
            history.add(systemMsg(Prompts.AGENT));
            JsonNode agentResp = openai.call("gpt-4.1", history);
            String agentText = openai.extractText(agentResp);

            // ---- CLASSIFICATION ----
            List<Map<String, Object>> classifyInput = List.of(
                    systemMsg(Prompts.CLASSIFY),
                    userMsg(agentText));

            JsonNode classifyResp = openai.call("gpt-4.1", classifyInput);
            String category = openai.extractText(classifyResp);
            System.out.println(category);

            // Check if solvable
            if (!category.contains("Not Solvable")) {
                // Generate summary
                history.add(systemMsg(Prompts.AGENT_SUMMARY));
                JsonNode summaryResp = openai.call("gpt-4.1", history);

                if (generateTest) {
                    history.add(systemMsg(Prompts.TEST_GENERATION));
                    JsonNode testGenerate = openai.call("gpt-4.1", history);
                    String testSourceCode = openai.extractText(testGenerate);
                    System.out.println(testSourceCode);
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

            JsonNode missingResp = openai.call("gpt-4.1", extractInput);
            List<String> missingClasses = new ObjectMapper().readValue(
                    openai.extractText(missingResp),
                    List.class);

            System.out.println("Missing classes");
            missingClasses.stream().forEach(c -> {
                System.out.println(c);
            });
            boolean addedSomething = false;
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
                    // File truly not found → cannot solve
                    unresolved.add(missingClass);
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
