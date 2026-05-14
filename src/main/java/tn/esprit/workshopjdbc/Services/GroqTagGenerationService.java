package tn.esprit.workshopjdbc.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GroqTagGenerationService {
    private static final String API_KEY = System.getenv("GROQ_API_KEY") != null ?
        System.getenv("GROQ_API_KEY") : "YOUR_GROQ_API_KEY_HERE";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final List<String> MEDICAL_KEYWORDS = Arrays.asList(
            "diabetes", "hypertension", "cardiology", "cancer", "surgery", "mental health",
            "pain", "infection", "allergy", "asthma", "obesity", "arthritis", "depression",
            "anxiety", "stroke", "heart attack", "pneumonia", "flu", "vaccine", "immunization",
            "pregnancy", "pediatrics", "geriatrics", "neurology", "orthopedics", "dermatology",
            "gastroenterology", "urology", "ophthalmology", "ent", "oncology", "rheumatology"
    );

    public static String generateTag(String title, String content) {
        if (title == null || content == null || title.isEmpty() || content.isEmpty()) {
            return "General";
        }

        // Try API first with timeout
        try {
            String apiTag = generateTagWithGroq(title, content);
            if (apiTag != null && !apiTag.equals("General") && !apiTag.isEmpty()) {
                return apiTag;
            }
        } catch (Exception e) {
            System.err.println("Groq API failed, falling back to local tags: " + e.getMessage());
        }
        
        // Fallback to local tag if API fails
        return getLocalTag(title + " " + content);
    }

    private static String getLocalTag(String text) {
        String lowerText = text.toLowerCase();

        for (String keyword : MEDICAL_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                return capitalizeFirstLetter(keyword);
            }
        }

        // Check for common patterns
        if (lowerText.contains("question") || lowerText.contains("ask")) return "Question";
        if (lowerText.contains("advice") || lowerText.contains("tip")) return "Advice";
        if (lowerText.contains("experience") || lowerText.contains("story")) return "Experience";
        if (lowerText.contains("research") || lowerText.contains("study")) return "Research";
        if (lowerText.contains("emergency") || lowerText.contains("urgent")) return "Urgent";
        if (lowerText.contains("treatment") || lowerText.contains("therapy")) return "Treatment";
        if (lowerText.contains("medication") || lowerText.contains("drug")) return "Medication";
        if (lowerText.contains("symptom")) return "Symptoms";
        if (lowerText.contains("prevention")) return "Prevention";
        if (lowerText.contains("diet") || lowerText.contains("nutrition")) return "Nutrition";

        return "General";
    }

    private static String generateTagWithGroq(String title, String content) {
        try {
            String prompt = "Based on this medical forum post, suggest a single, concise tag (2-3 words max) that categorizes it.\n" +
                    "Title: \"" + title + "\"\n" +
                    "Content: \"" + content.substring(0, Math.min(300, content.length())) + "\"\n" +
                    "Respond ONLY with the tag, no explanation.";

            String requestBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String tag = extractTagFromResponse(response.body());
                System.out.println("Generated tag from Groq: " + tag);
                return tag;
            }
        } catch (Exception e) {
            System.err.println("Tag generation API call failed: " + e.getMessage());
        }
        return "General";
    }

    private static String extractTagFromResponse(String response) {
        try {
            int contentStart = response.indexOf("\"content\":\"");
            if (contentStart != -1) {
                contentStart += "\"content\":\"".length();
                int contentEnd = response.indexOf("\"", contentStart);
                if (contentEnd != -1) {
                    String tag = response.substring(contentStart, contentEnd).trim();
                    // Clean up the tag
                    tag = tag.replaceAll("[\"\\*#-]", "").trim();
                    // Take first 30 characters max
                    if (tag.length() > 30) {
                        tag = tag.substring(0, 30).trim();
                    }
                    return tag.isEmpty() ? "General" : tag;
                }
            }
        } catch (Exception e) {
            System.err.println("Tag extraction error: " + e.getMessage());
        }
        return "General";
    }

    private static String buildRequestBody(String prompt) {
        return "{" +
                "\"model\": \"" + MODEL + "\"," +
                "\"messages\": [{" +
                "\"role\": \"system\"," +
                "\"content\": \"You are a medical forum content categorizer. Generate appropriate tags for posts.\"" +
                "}, {" +
                "\"role\": \"user\"," +
                "\"content\": \"" + escapeJson(prompt) + "\"" +
                "}]," +
                "\"temperature\": 0.5," +
                "\"max_tokens\": 50" +
                "}";
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static List<String> generateMultipleTags(String title, String content, int count) {
        List<String> tags = new ArrayList<>();
        String mainTag = generateTag(title, content);
        tags.add(mainTag);

        if (count > 1) {
            try {
                String prompt = "Based on this medical forum post, suggest " + (count - 1) + " more relevant tags (comma-separated).\n" +
                        "Title: \"" + title + "\"\n" +
                        "Content: \"" + content.substring(0, Math.min(300, content.length())) + "\"\n" +
                        "Don't repeat: " + mainTag + "\n" +
                        "Respond ONLY with tags, comma-separated.";

                String requestBody = buildRequestBody(prompt);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(java.time.Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseText = extractTagFromResponse(response.body());
                    List<String> additionalTags = Arrays.stream(responseText.split(","))
                            .map(String::trim)
                            .filter(t -> !t.isEmpty() && !t.equals(mainTag))
                            .limit(count - 1)
                            .collect(Collectors.toList());
                    tags.addAll(additionalTags);
                }
            } catch (Exception e) {
                System.err.println("Multiple tags generation failed: " + e.getMessage());
            }
        }

        return tags.stream().distinct().limit(count).collect(Collectors.toList());
    }
}
