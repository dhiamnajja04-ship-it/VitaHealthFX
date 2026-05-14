package tn.esprit.workshopjdbc.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public class OtpVerificationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final String GRAPH_VERSION = env("WHATSAPP_GRAPH_VERSION", "v20.0");
    private static final String ACCESS_TOKEN = env("WHATSAPP_ACCESS_TOKEN", "");
    private static final String PHONE_NUMBER_ID = env("WHATSAPP_PHONE_NUMBER_ID", "");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String currentCode;
    private String currentPhone;
    private Instant expiresAt;

    public OtpResult sendCode(String phone, String purpose) {
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone.isBlank()) {
            return OtpResult.error("Numero telephone obligatoire.");
        }

        currentCode = String.valueOf(100000 + RANDOM.nextInt(900000));
        currentPhone = normalizedPhone;
        expiresAt = Instant.now().plus(TTL);

        String message = "VitaHealthFX - Votre code de verification "
                + purposeLabel(purpose) + " est: " + currentCode
                + ". Il expire dans 10 minutes.";

        if (!isConfigured()) {
            return OtpResult.dryRun("Mode test OTP: code " + currentCode + " envoye a +" + normalizedPhone + ".");
        }

        String body = """
                {
                  "messaging_product": "whatsapp",
                  "to": "%s",
                  "type": "text",
                  "text": { "preview_url": false, "body": "%s" }
                }
                """.formatted(json(normalizedPhone), json(message));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://graph.facebook.com/" + GRAPH_VERSION + "/" + PHONE_NUMBER_ID + "/messages"))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return OtpResult.sent("Code OTP envoye par WhatsApp.");
            }
            return OtpResult.error("WhatsApp OTP refuse (" + response.statusCode() + "): " + response.body());
        } catch (IOException e) {
            return OtpResult.error("Erreur reseau WhatsApp OTP: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return OtpResult.error("Envoi OTP interrompu.");
        }
    }

    public boolean verify(String phone, String code) {
        if (currentCode == null || expiresAt == null || Instant.now().isAfter(expiresAt)) {
            return false;
        }
        return normalizePhone(phone).equals(currentPhone)
                && code != null
                && code.trim().equals(currentCode);
    }

    public void clear() {
        currentCode = null;
        currentPhone = null;
        expiresAt = null;
    }

    private boolean isConfigured() {
        return !ACCESS_TOKEN.isBlank() && !PHONE_NUMBER_ID.isBlank();
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        String normalized = phone.trim().replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        if (normalized.startsWith("+")) return normalized.substring(1);
        return normalized;
    }

    private static String purposeLabel(String purpose) {
        if (purpose == null || purpose.isBlank()) return "";
        return "pour " + purpose;
    }

    private static String json(String value) {
        return (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key.toLowerCase(Locale.ROOT).replace('_', '.'));
        }
        return value == null || value.isBlank() ? fallback : value;
    }

    public record OtpResult(boolean sent, boolean dryRun, String message) {
        public static OtpResult sent(String message) {
            return new OtpResult(true, false, message);
        }

        public static OtpResult dryRun(String message) {
            return new OtpResult(false, true, message);
        }

        public static OtpResult error(String message) {
            return new OtpResult(false, false, message);
        }
    }
}
