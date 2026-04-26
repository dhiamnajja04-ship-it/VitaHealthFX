package com.vitahealth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class TraductionService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String apiUrl = "http://localhost:5000/translate";

    public void setApiUrl(String url) {
        this.apiUrl = url;
    }

    /**
     * Traduction asynchrone.
     * @param text texte à traduire
     * @param sourceLang code source ("auto" pour détection)
     * @param targetLang code cible (fr, en, ar...)
     * @return CompletableFuture contenant le texte traduit (ou texte original si erreur)
     */
    public CompletableFuture<String> traduire(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) {
            return CompletableFuture.completedFuture("");
        }
        String payload = String.format(
                "{\"q\":\"%s\", \"source\":\"%s\", \"target\":\"%s\", \"format\":\"text\"}",
                escapeJson(text), sourceLang, targetLang
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseResponse)
                .exceptionally(ex -> {
                    System.err.println("Erreur traduction: " + ex.getMessage());
                    return text; // fallback : texte original
                });
    }

    private String parseResponse(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path("translatedText").asText();
        } catch (Exception e) {
            System.err.println("Erreur JSON: " + e.getMessage());
            return "";
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}