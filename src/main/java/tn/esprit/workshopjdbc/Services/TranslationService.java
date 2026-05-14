package tn.esprit.workshopjdbc.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TranslationService {
    
        private static final String[] LIBRETRANSLATE_ENDPOINTS = new String[] {
            "https://libretranslate.com/translate",
            "https://libretranslate.de/translate",
            "https://translate.astian.org/translate"
        };
        private static final String API_KEY = System.getenv("LIBRETRANSLATE_API_KEY");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static class TranslationResult {
        public String originalText;
        public String translatedText;
        public String sourceLanguage;
        public String targetLanguage;
        public boolean success;
        public String error;
    }
    
    /**
     * Translates text using LibreTranslate API (POST)
     * @param text Text to translate
     * @param sourceLang Source language code (e.g., "en", "fr") or "auto" for auto-detect
     * @param targetLang Target language code (e.g., "en", "fr")
     * @return TranslationResult containing translated text or error
     */
    public static TranslationResult translate(String text, String sourceLang, String targetLang) {
        TranslationResult result = new TranslationResult();
        result.originalText = text;
        result.sourceLanguage = sourceLang;
        result.targetLanguage = targetLang;
        
        if (text == null || text.trim().isEmpty()) {
            result.success = false;
            result.error = "Text cannot be empty";
            return result;
        }
        
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("q", text);
            payload.put("source", (sourceLang == null || sourceLang.isBlank()) ? "auto" : sourceLang);
            payload.put("target", targetLang);
            payload.put("format", "text");
            if (API_KEY != null && !API_KEY.isBlank()) {
                payload.put("api_key", API_KEY);
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);
            String lastError = null;

            for (String endpoint : LIBRETRANSLATE_ENDPOINTS) {
                TranslationResult attempt = callEndpoint(endpoint, jsonPayload, result);
                if (attempt.success) {
                    return attempt;
                }
                lastError = attempt.error;
            }

            result.success = false;
            result.error = (lastError != null) ? lastError : "Translation failed";
            return result;
        } catch (Exception e) {
            result.success = false;
            result.error = "Translation error: " + e.getMessage();
            e.printStackTrace();
            return result;
        }
    }
    
    private static TranslationResult callEndpoint(String endpoint, String jsonPayload, TranslationResult result) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            InputStream responseStream = responseCode == HttpURLConnection.HTTP_OK
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String responseBody = readStream(responseStream);
            connection.disconnect();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                result.success = false;
                result.error = "API returned status code: " + responseCode + " from " + endpoint + " :: " + responseBody;
                return result;
            }

            JsonNode json = objectMapper.readTree(responseBody);
            JsonNode translatedNode = json.get("translatedText");
            if (translatedNode != null && !translatedNode.asText().isEmpty()) {
                result.translatedText = translatedNode.asText();
                result.success = true;
            } else if (json.has("error")) {
                result.success = false;
                result.error = "Translation failed: " + json.get("error").asText();
            } else {
                result.success = false;
                result.error = "No translation returned";
            }
            return result;
        } catch (Exception e) {
            result.success = false;
            result.error = "Translation error: " + e.getMessage();
            return result;
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    /**
     * Auto-detect language and translate to target language
     * @param text Text to translate
     * @param targetLang Target language code
     * @return TranslationResult
     */
    public static TranslationResult translateAuto(String text, String targetLang) {
        // Use auto detection by setting source to auto
        return translate(text, "auto", targetLang);
    }
    
    /**
     * Get available language pairs
     * @return Array of supported language codes
     */
    public static String[] getSupportedLanguages() {
        return new String[]{"en", "fr", "es", "de", "it", "pt", "ru", "ja", "zh", "ar", "hi", "ko"};
    }
    
    /**
     * Get language name from code
     * @param langCode Language code (e.g., "en", "fr")
     * @return Language name
     */
    public static String getLanguageName(String langCode) {
        return switch (langCode) {
            case "en" -> "English";
            case "fr" -> "Français";
            case "es" -> "Español";
            case "de" -> "Deutsch";
            case "it" -> "Italiano";
            case "pt" -> "Português";
            case "ru" -> "Русский";
            case "ja" -> "日本語";
            case "zh" -> "中文";
            case "ar" -> "العربية";
            case "hi" -> "हिन्दी";
            case "ko" -> "한국어";
            default -> langCode.toUpperCase();
        };
    }
}