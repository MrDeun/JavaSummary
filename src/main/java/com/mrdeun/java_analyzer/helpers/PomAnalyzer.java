package com.mrdeun.java_analyzer.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mrdeun.java_analyzer.dto.Dependency;
import com.mrdeun.java_analyzer.dto.PomInfo;

public class PomAnalyzer {
    public static PomInfo analyzePom(String projectRoot) {
        try {
            Path pomPath = Paths.get(projectRoot, "pom.xml");

            if (!Files.exists(pomPath)) {
                System.out.println("No pom.xml found at: " + pomPath);
                return new PomInfo();
            }

            System.out.println("Analyzing pom.xml at: " + pomPath);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomPath.toFile());
            doc.getDocumentElement().normalize();

            PomInfo info = new PomInfo();

            // Extract project info
            info.groupId = getTextContent(doc, "groupId");
            info.artifactId = getTextContent(doc, "artifactId");
            info.version = getTextContent(doc, "version");

            // Extract dependencies
            NodeList dependencies = doc.getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dep = (Element) dependencies.item(i);

                String groupId = getElementText(dep, "groupId");
                String artifactId = getElementText(dep, "artifactId");
                String version = getElementText(dep, "version");
                String scope = getElementText(dep, "scope");

                if (groupId != null && artifactId != null) {
                    Dependency LibraryInfo = new Dependency(groupId, artifactId, version, scope);
                    info.dependencies.add(LibraryInfo);
                }
            }

            System.out.println("Found " + info.dependencies.size() + " dependencies in pom.xml");

            return info;

        } catch (Exception e) {
            System.err.println("Failed to parse pom.xml: " + e.getMessage());
            return new PomInfo();
        }
    }

    /**
     * Analyze build.gradle instead of pom.xml
     */
    public static PomInfo analyzeGradle(String projectRoot) {
        try {
            Path gradlePath = Paths.get(projectRoot, "build.gradle");

            if (!Files.exists(gradlePath)) {
                System.out.println("No build.gradle found at: " + gradlePath);
                return new PomInfo();
            }

            System.out.println("Analyzing build.gradle at: " + gradlePath);

            String content = Files.readString(gradlePath);
            PomInfo info = new PomInfo();

            // Regex patterns for Gradle dependencies
            // implementation 'group:artifact:version'
            // implementation "group:artifact:version"
            Pattern pattern = Pattern.compile(
                    "(implementation|compile|api|testImplementation|runtimeOnly)\\s+['\"]([^:]+):([^:]+):([^'\"]+)['\"]");

            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String scope = matcher.group(1);
                String groupId = matcher.group(2);
                String artifactId = matcher.group(3);
                String version = matcher.group(4);

                Dependency dep = new Dependency(groupId, artifactId, version, scope);
                info.dependencies.add(dep);
            }

            System.out.println("Found " + info.dependencies.size() + " dependencies in build.gradle");

            return info;

        } catch (IOException e) {
            System.err.println("Failed to parse build.gradle: " + e.getMessage());
            return new PomInfo();
        }
    }

    /**
     * Get text content from XML element
     */
    private static String getTextContent(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    /**
     * Get text from child element
     */
    private static String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    /**
     * Check if a class belongs to any known LibraryInfo
     */
    public static boolean isFromKnownLibraryInfo(String className, PomInfo pomInfo) {
        String packageName = extractPackageName(className);

        for (Dependency dep : pomInfo.dependencies) {
            // Check if package matches groupId
            if (packageName.startsWith(dep.groupId)) {
                return true;
            }

            // Check common patterns
            // e.g., com.google.gson -> com.google.code.gson
            String normalizedGroup = dep.groupId.replace("-", ".");
            if (packageName.startsWith(normalizedGroup)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extract package name from class name
     */
    private static String extractPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            return className.substring(0, lastDot);
        }
        return className;
    }

    /**
     * Generate a summary report for the AI agent
     */
    public static String generateLibraryInfoReport(PomInfo pomInfo) {
        if (pomInfo.dependencies.isEmpty()) {
            return "No third-party dependencies found in project configuration.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("### PROJECT DEPENDENCIES (from pom.xml)\n\n");

        if (pomInfo.groupId != null) {
            sb.append("**Project**: ").append(pomInfo.groupId).append(":")
                    .append(pomInfo.artifactId).append(":").append(pomInfo.version).append("\n\n");
        }

        sb.append("**Third-Party Libraries Available**:\n\n");

        Map<String, List<Dependency>> byGroup = new HashMap<>();
        for (Dependency dep : pomInfo.dependencies) {
            byGroup.computeIfAbsent(dep.groupId, k -> new ArrayList<>()).add(dep);
        }

        int count = 1;
        for (Map.Entry<String, List<Dependency>> entry : byGroup.entrySet()) {
            for (Dependency dep : entry.getValue()) {
                sb.append(count++).append(". **").append(dep.artifactId).append("**\n");
                sb.append("   - Coordinate: `").append(dep.getCoordinate()).append("`\n");
                sb.append("   - Package: `").append(dep.groupId).append(".*`\n");
                if (dep.scope != null && !dep.scope.isEmpty()) {
                    sb.append("   - Scope: ").append(dep.scope).append("\n");
                }
                sb.append("\n");
            }
        }

        sb.append("\n**Important**: All classes from these libraries are AVAILABLE and should be treated as KNOWN.\n");
        sb.append("Do NOT mark classes from these packages as missing dependencies.\n");

        return sb.toString();
    }
}
