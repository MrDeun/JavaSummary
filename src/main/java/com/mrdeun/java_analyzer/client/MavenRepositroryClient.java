package com.mrdeun.java_analyzer.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrdeun.java_analyzer.dto.LibraryInfo;
import com.mrdeun.java_analyzer.exceptions.MavenClassNotFoundException;

public class MavenRepositroryClient {
    private static final String MAVEN_CENTRAL_SEARCH = "https://search.maven.org/solrsearch/select";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("maven.http.timeout_secs")
    private long TIMEOUT;

    private String extractPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            return className.substring(0, lastDot);
        }
        return className;
    }

    private LibraryInfo searchMaven(String query, boolean exactMatch) throws Exception {
        String searchQuery = exactMatch ? "\"" + query + "\"" : query;
        String encoded = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&rows=5&wt=json", MAVEN_CENTRAL_SEARCH, encoded);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode docs = root.path("response").path("docs");

        if (docs.isArray() && docs.size() > 0) {
            JsonNode firstResult = docs.get(0);
            return new LibraryInfo(
                    firstResult.path("g").asText(),
                    firstResult.path("a").asText(),
                    firstResult.path("latestVersion").asText(),
                    firstResult.path("repositoryId").asText("central"));
        }

        return null;
    }

        public String getLibraryDetails(LibraryInfo info) {
        if (info == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("### THIRD-PARTY LIBRARY INFORMATION\n\n");
        sb.append("**Library Found in Maven Central**\n\n");
        sb.append("- **Group ID**: ").append(info.groupId).append("\n");
        sb.append("- **Artifact ID**: ").append(info.artifactId).append("\n");
        sb.append("- **Latest Version**: ").append(info.version).append("\n");
        sb.append("- **Repository**: ").append(info.repository).append("\n\n");
        
        sb.append("**Maven Dependency:**\n");
        sb.append("```xml\n");
        sb.append("<dependency>\n");
        sb.append("    <groupId>").append(info.groupId).append("</groupId>\n");
        sb.append("    <artifactId>").append(info.artifactId).append("</artifactId>\n");
        sb.append("    <version>").append(info.version).append("</version>\n");
        sb.append("</dependency>\n");
        sb.append("```\n\n");
        
        sb.append("**Gradle Dependency:**\n");
        sb.append("```gradle\n");
        sb.append("implementation '").append(info.groupId).append(":")
          .append(info.artifactId).append(":").append(info.version).append("'\n");
        sb.append("```\n\n");
        
        sb.append("This library is available in Maven Central and can be added to your project.\n");
        sb.append("The classes from this library should be considered as KNOWN/AVAILABLE.\n");
        
        return sb.toString();
    }

    public boolean isKnownThirdPartyClass(String className) {
        LibraryInfo info = lookupClass(className);
        return info != null;
    }

    public LibraryInfo lookupClass(String className) {
        try {
            String packageName = extractPackageName(className);
            LibraryInfo result = searchMaven(packageName, true);
            if (result != null) {
                return result;
            }

            result = searchMaven(packageName, false);
            return result;
        } catch (Exception err) {
            System.out.println(err.toString());
            return null;
        }
    }
}
