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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mrdeun.java_analyzer.cli.CliArguments;
import com.mrdeun.java_analyzer.cli.CliParser;
import com.mrdeun.java_analyzer.client.OpenAIClient;
import com.mrdeun.java_analyzer.helpers.Helpers;
import com.mrdeun.java_analyzer.workspace.WorkspaceRunner;

import io.github.cdimascio.dotenv.Dotenv;

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

	public static void main(String[] args) throws IOException {
		CliArguments cli = CliParser.parse(args);

		if (cli.projectRoot != null) {
			System.setProperty("user.dir", cli.projectRoot);
		}
		String apiKey = System.getenv("OPENAI_API_KEY");
		OpenAIClient client = new OpenAIClient(apiKey);

		WorkspaceRunner runner = new WorkspaceRunner(client);
		Map<String, Object> result = new HashMap<String, Object>() {

		};
		try {
			result = runner.run(
					cli.targetClass,
					cli.targetMethod,
					cli.signature);
		} catch (Exception err) {
			result.put("status", "Unexpected Failure");
			result.put("result", err.toString());
		}

		System.out.println("STATUS: " + result.get("status"));
		System.out.println(result.get("result"));
		if (result.get("status").equals("SOLVED")) {
			try (FileWriter fw = new FileWriter("RESULT.md")) {
				BufferedWriter writer = new BufferedWriter(fw);
				writer.write(result.get("result").toString());
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}

}
