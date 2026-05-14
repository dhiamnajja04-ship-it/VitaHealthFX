package tn.esprit.workshopjdbc.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GroqChatbotService {
    private static final String API_KEY = System.getenv("GROQ_API_KEY") != null ?
        System.getenv("GROQ_API_KEY") : "YOUR_GROQ_API_KEY_HERE";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static class ChatMessage {
        public String role; // "user" or "assistant"
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ChatbotResponse {
        public String reply;
        public boolean success;
        public String error;

        public ChatbotResponse(String reply, boolean success, String error) {
            this.reply = reply;
            this.success = success;
            this.error = error;
        }
    }

    private static final List<ChatMessage> conversationHistory = new ArrayList<>();

    static {
        // Initialize system context
        conversationHistory.add(new ChatMessage("system",
                "You are VitaHealth Assistant, a friendly and knowledgeable medical forum chatbot. " +
                        "You help users navigate the VitaHealth community, answer general health questions, and provide support. " +
                        "Always remind users that for serious medical concerns, they should consult with healthcare professionals. " +
                        "Be professional, empathetic, and concise. Keep responses under 300 words."));
    }

    public static ChatbotResponse chat(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ChatbotResponse("Please enter a message.", false, null);
        }

        try {
            // Check for content moderation first
            GroqContentModerationService.ModerationResult moderation = GroqContentModerationService.moderateContent(userMessage);
            if (!moderation.isClean) {
                return new ChatbotResponse(
                        "Your message contains inappropriate language. Please rephrase your question without offensive words. " +
                                "Flagged words: " + String.join(", ", moderation.flaggedWords),
                        false,
                        moderation.reason
                );
            }

            // Add user message to history
            conversationHistory.add(new ChatMessage("user", userMessage));

            // Build request with conversation history
            String requestBody = buildRequestBodyWithHistory();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String reply = extractReplyFromResponse(response.body());
                conversationHistory.add(new ChatMessage("assistant", reply));

                // Keep conversation history manageable
                if (conversationHistory.size() > 20) {
                    // Keep system message and last 18 messages
                    List<ChatMessage> newHistory = new ArrayList<>();
                    newHistory.add(conversationHistory.get(0)); // system message
                    for (int i = conversationHistory.size() - 18; i < conversationHistory.size(); i++) {
                        newHistory.add(conversationHistory.get(i));
                    }
                    conversationHistory.clear();
                    conversationHistory.addAll(newHistory);
                }

                return new ChatbotResponse(reply, true, null);
            } else {
                // DEBUG: print full error body so we can diagnose
                System.err.println("=== GROQ API ERROR ===");
                System.err.println("Status: " + response.statusCode());
                System.err.println("Body: " + response.body());
                System.err.println("======================");
                return new ChatbotResponse(
                        "I'm having trouble responding right now. Please try again later.",
                        false,
                        "API error: " + response.statusCode() + " | " + response.body()
                );
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ChatbotResponse(
                    "I'm experiencing connection issues. Please try again.",
                    false,
                    e.getMessage()
            );
        }
    }

    public static void resetConversation() {
        conversationHistory.clear();
        conversationHistory.add(new ChatMessage("system",
                "You are VitaHealth Assistant, a friendly and knowledgeable medical forum chatbot. " +
                        "You help users navigate the VitaHealth community, answer general health questions, and provide support. " +
                        "Always remind users that for serious medical concerns, they should consult with healthcare professionals. " +
                        "Be professional, empathetic, and concise. Keep responses under 300 words."));
    }

    private static String buildRequestBodyWithHistory() {
        StringBuilder messagesJson = new StringBuilder("[");
        for (int i = 0; i < conversationHistory.size(); i++) {
            if (i > 0) messagesJson.append(",");
            ChatMessage msg = conversationHistory.get(i);
            messagesJson.append("{")
                    .append("\"role\":").append(jsonString(msg.role)).append(",")
                    .append("\"content\":").append(jsonString(msg.content))
                    .append("}");
        }
        messagesJson.append("]");

        return "{" +
                "\"model\":\"" + MODEL + "\"," +
                "\"messages\":" + messagesJson.toString() + "," +
                "\"temperature\":0.7," +
                "\"max_tokens\":500" +
                "}";
    }

    private static String extractReplyFromResponse(String response) {
        try {
            int contentStart = response.indexOf("\"content\":\"");
            if (contentStart != -1) {
                contentStart += "\"content\":\"".length();
                int contentEnd = findEndOfContent(response, contentStart);
                if (contentEnd != -1) {
                    return unescapeJson(response.substring(contentStart, contentEnd));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "I couldn't generate a response. Please try again.";
    }

    private static int findEndOfContent(String text, int start) {
        int pos = start;
        while (pos < text.length()) {
            if (text.charAt(pos) == '"' && (pos == 0 || text.charAt(pos - 1) != '\\')) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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

    private static String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public static List<ChatMessage> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    public static String getQuickReply(String intent) {
        // Provide quick replies without API call for common intents
        switch (intent.toLowerCase()) {
            case "hello":
            case "hi":
                return "Hello! Welcome to VitaHealth. How can I assist you today?";
            case "help":
                return "I'm here to help! You can ask me about:\n" +
                        "• Forum navigation\n" +
                        "• General health questions\n" +
                        "• How to post or comment\n" +
                        "• Finding information on specific topics\n\n" +
                        "What would you like to know?";
            case "emergency":
                return "If this is a medical emergency, please call emergency services immediately or visit the nearest hospital. " +
                        "VitaHealth is a community platform and not a substitute for emergency medical care.";
            case "thanks":
            case "thank you":
                return "You're welcome! Feel free to ask if you have any other questions.";
            default:
                return null;
        }
    }
}