package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.Appointment;
import tn.esprit.workshopjdbc.Entities.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WhatsAppReminderService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String GRAPH_VERSION = env("WHATSAPP_GRAPH_VERSION", "v20.0");
    private static final String ACCESS_TOKEN = env("WHATSAPP_ACCESS_TOKEN", "");
    private static final String PHONE_NUMBER_ID = env("WHATSAPP_PHONE_NUMBER_ID", "");
    private static final String TEMPLATE_NAME = env("WHATSAPP_TEMPLATE_NAME", "appointment_reminder");
    private static final String LANGUAGE_CODE = env("WHATSAPP_LANGUAGE_CODE", "fr");
    private static final String MESSAGE_MODE = env("WHATSAPP_MESSAGE_MODE", "template");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ReminderResult sendAppointmentReminder(Appointment appointment, User patient, User doctor) {
        if (appointment == null || patient == null) {
            return ReminderResult.error("Rendez-vous ou patient introuvable.");
        }

        String phone = normalizePhone(patient.getPhone());
        if (phone.isBlank()) {
            return ReminderResult.error("Le patient n'a pas de numero telephone valide.");
        }

        String message = buildReminderText(appointment, patient, doctor);
        if (!isConfigured()) {
            return ReminderResult.dryRun(message);
        }

        String body = "text".equalsIgnoreCase(MESSAGE_MODE)
                ? buildTextPayload(phone, message)
                : buildTemplatePayload(phone, appointment, patient, doctor);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://graph.facebook.com/" + GRAPH_VERSION + "/" + PHONE_NUMBER_ID + "/messages"))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return ReminderResult.sent("Message WhatsApp envoye.", response.body());
            }
            return ReminderResult.error("WhatsApp API a refuse l'envoi (" + response.statusCode() + "): " + response.body());
        } catch (IOException e) {
            return ReminderResult.error("Erreur reseau WhatsApp: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ReminderResult.error("Envoi WhatsApp interrompu.");
        }
    }

    public String buildReminderText(Appointment appointment, User patient, User doctor) {
        String doctorName = doctor != null ? doctor.getFullName() : valueOr(appointment.getDoctorName(), "votre medecin");
        String date = appointment.getDate() != null ? appointment.getDate().format(DATE_FMT) : "date non precisee";
        return "Bonjour " + patient.getFullName() + ", rappel VitaHealthFX: votre rendez-vous avec Dr. "
                + doctorName + " est prevu le " + date + ". Motif: "
                + valueOr(appointment.getReason(), "consultation") + ". Merci de confirmer votre presence.";
    }

    private String buildTextPayload(String phone, String message) {
        return """
                {
                  "messaging_product": "whatsapp",
                  "to": "%s",
                  "type": "text",
                  "text": { "preview_url": false, "body": "%s" }
                }
                """.formatted(json(phone), json(message));
    }

    private String buildTemplatePayload(String phone, Appointment appointment, User patient, User doctor) {
        String doctorName = doctor != null ? doctor.getFullName() : valueOr(appointment.getDoctorName(), "Medecin");
        String date = appointment.getDate() != null ? appointment.getDate().format(DATE_FMT) : "date non precisee";
        return """
                {
                  "messaging_product": "whatsapp",
                  "to": "%s",
                  "type": "template",
                  "template": {
                    "name": "%s",
                    "language": { "code": "%s" },
                    "components": [{
                      "type": "body",
                      "parameters": [
                        { "type": "text", "text": "%s" },
                        { "type": "text", "text": "%s" },
                        { "type": "text", "text": "%s" },
                        { "type": "text", "text": "%s" }
                      ]
                    }]
                  }
                }
                """.formatted(
                json(phone),
                json(TEMPLATE_NAME),
                json(LANGUAGE_CODE),
                json(patient.getFullName()),
                json(doctorName),
                json(date),
                json(valueOr(appointment.getReason(), "consultation"))
        );
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

    private static String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String json(String value) {
        return valueOr(value, "")
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

    public record ReminderResult(boolean sent, boolean dryRun, String message, String providerResponse) {
        public static ReminderResult sent(String message, String response) {
            return new ReminderResult(true, false, message, response);
        }

        public static ReminderResult dryRun(String message) {
            return new ReminderResult(false, true, "Mode test: " + message, "");
        }

        public static ReminderResult error(String message) {
            return new ReminderResult(false, false, message, "");
        }
    }
}
