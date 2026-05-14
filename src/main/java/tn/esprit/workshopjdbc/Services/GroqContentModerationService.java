package tn.esprit.workshopjdbc.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GroqContentModerationService {
    private static final String API_KEY = System.getenv("GROQ_API_KEY") != null ?
        System.getenv("GROQ_API_KEY") : "YOUR_GROQ_API_KEY_HERE";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // List of explicit bad words for quick detection
    private static final List<String> EXPLICIT_WORDS = new ArrayList<>();

    static {
        EXPLICIT_WORDS.add("fuck");
        EXPLICIT_WORDS.add("shit");
        EXPLICIT_WORDS.add("asshole");
        EXPLICIT_WORDS.add("damn");
        EXPLICIT_WORDS.add("crap");
        EXPLICIT_WORDS.add("piss");
        EXPLICIT_WORDS.add("bitch");
        EXPLICIT_WORDS.add("bastard");
        EXPLICIT_WORDS.add("hell");
    }

    public static class ModerationResult {
        public boolean isClean;
        public List<String> flaggedWords;
        public String reason;
        public String aiAnalysis;

        public ModerationResult(boolean isClean, List<String> flaggedWords, String reason, String aiAnalysis) {
            this.isClean = isClean;
            this.flaggedWords = flaggedWords;
            this.reason = reason;
            this.aiAnalysis = aiAnalysis;
        }
    }

    public static ModerationResult moderateContent(String content) {
        if (content == null || content.isEmpty()) {
            return new ModerationResult(true, new ArrayList<>(), "Empty content", "");
        }

        // First, check for explicit words locally
        List<String> flaggedWords = checkForExplicitWords(content);
        if (!flaggedWords.isEmpty()) {
            String reason = "Explicit language detected: " + String.join(", ", flaggedWords);
            return new ModerationResult(false, flaggedWords, reason, "");
        }

        // Use Groq API for deeper analysis
        return analyzeWithGroq(content);
    }

    private static List<String> checkForExplicitWords(String content) {
        List<String> found = new ArrayList<>();
        String lowerContent = content.toLowerCase();

        for (String word : EXPLICIT_WORDS) {
            if (lowerContent.contains(word)) {
                found.add(word);
            }
        }

        return found;
    }

    private static ModerationResult analyzeWithGroq(String content) {
        try {
            String prompt = "Analyze the following medical forum post for inappropriate content:\n" +
                    "1. Hate speech\n2. Violence\n3. Spam\n4. Medical misinformation\n" +
                    "Post: '" + content.substring(0, Math.min(300, content.length())) + "'\n" +
                    "Reply only: CLEAN or FLAGGED (brief reason)";

            String requestBody = buildRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String analysis = extractAnalysisFromResponse(response.body());
                return parseAnalysis(analysis, content);
            } else {
                System.err.println("Groq API error: " + response.statusCode());
                return new ModerationResult(true, new ArrayList<>(), "API skipped", "");
            }
        } catch (java.net.http.HttpTimeoutException | java.net.ConnectException e) {
            System.err.println("API timeout - using local moderation only");
            return new ModerationResult(true, new ArrayList<>(), "Timeout", "");
        } catch (Exception e) {
            System.err.println("Moderation error: " + e.getMessage());
            return new ModerationResult(true, new ArrayList<>(), "Local only", "");
        }
    }

    private static String buildRequestBody(String prompt) {
        return "{" +
                "\"model\":\"" + MODEL + "\"," +
                "\"messages\":[{" +
                "\"role\":\"system\"," +
                "\"content\":\"You are a content moderation expert for a medical forum. Analyze posts for inappropriate content.\"" +
                "},{" +
                "\"role\":\"user\"," +
                "\"content\":" + jsonString(prompt) +
                "}]," +
                "\"temperature\":0.3," +
                "\"max_tokens\":200" +
                "}";
    }

    private static String extractAnalysisFromResponse(String response) {
        try {
            int contentStart = response.indexOf("\"content\":\"");
            if (contentStart != -1) {
                contentStart += "\"content\":\"".length();
                int contentEnd = response.indexOf("\"", contentStart);
                if (contentEnd != -1) {
                    return response.substring(contentStart, contentEnd);
                }
            }
        } catch (Exception e) {
            System.err.println("Analysis extraction error: " + e.getMessage());
        }
        return "";
    }

    private static ModerationResult parseAnalysis(String analysis, String content) {
        try {
            boolean isClean = !analysis.toLowerCase().contains("flagged") &&
                    !analysis.toLowerCase().contains("inappropriate") &&
                    !analysis.toLowerCase().contains("violation");

            String reason = analysis.isEmpty() ? "Content analyzed" : analysis;
            List<String> flaggedWords = extractFlaggedWordsFromAnalysis(analysis);

            return new ModerationResult(isClean, flaggedWords, reason, analysis);
        } catch (Exception e) {
            System.err.println("Analysis parsing error: " + e.getMessage());
            return new ModerationResult(true, new ArrayList<>(), "Local moderation only", analysis);
        }
    }

    private static List<String> extractFlaggedWordsFromAnalysis(String analysis) {
        List<String> words = new ArrayList<>();
        try {
            String lower = analysis.toLowerCase();
            for (String word : EXPLICIT_WORDS) {
                if (lower.contains(word)) {
                    words.add(word);
                }
            }
        } catch (Exception e) {
            System.err.println("Word extraction error: " + e.getMessage());
        }
        return words;
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    private static String jsonString(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
