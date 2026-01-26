package com.mrdeun.java_analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mrdeun.java_analyzer.cli.CliArguments;
import com.mrdeun.java_analyzer.cli.CliParser;
import com.mrdeun.java_analyzer.client.MavenRepositroryClient;
import com.mrdeun.java_analyzer.client.OpenAIClient;
import com.mrdeun.java_analyzer.helpers.Helpers;
import com.mrdeun.java_analyzer.workspace.WorkspaceRunner;

@SpringBootApplication
public class JavaAnalyzerApplication {
	private static String targetPrompt(
			String clazz,
			String method,
			String signature) {
		return """
				TARGET METHOD (IMMUTABLE):
				- Class: %s
				- Method: %s
				- Signature: %s

				Rules:
				- Analyze ONLY this method
				- Ignore other methods unless they are called by it
				- Never change the target
				""".formatted(clazz, method, signature);
	}

	@Value("analyzer.summary_file")
	private static String summary_path;

	public static void main(String[] args) throws IOException {
		CliArguments cli = CliParser.parse(args);
		OpenAIClient client = new OpenAIClient();
		MavenRepositroryClient mavenClient = new MavenRepositroryClient();

		WorkspaceRunner runner = new WorkspaceRunner(client, mavenClient);
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = runner.run(
					cli.targetClass,
					cli.targetMethod,
					cli.projectRoot,
					cli.generateTest);
		} catch (Exception err) {
			result.put("status", "Unexpected Failure");
			result.put("result", err.toString());
		}

		// System.out.println(result.toString());
		if (result.get("status").equals("SOLVED")) {
			try (FileWriter fw = new FileWriter(summary_path, false)) {
				BufferedWriter writer = new BufferedWriter(fw);
				writer.write(result.get("content").toString());
				writer.close();
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}

}
