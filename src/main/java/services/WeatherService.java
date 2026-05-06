package services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class WeatherService {

    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String LATITUDE = "36.8189"; // Tunis
    private static final String LONGITUDE = "10.1657"; // Tunis

    public static CompletableFuture<String> getWeatherForecast(LocalDate date) {
        String url = String.format("%s?latitude=%s&longitude=%s&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto&start_date=%s&end_date=%s",
                API_URL, LATITUDE, LONGITUDE, date, date);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseWeather(response.body());
                    } else {
                        return "Météo indisponible";
                    }
                })
                .exceptionally(ex -> "Erreur météo");
    }

    private static String parseWeather(String jsonBody) {
        try {
            JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();
            JsonObject daily = json.getAsJsonObject("daily");
            JsonArray codes = daily.getAsJsonArray("weathercode");
            JsonArray maxTemps = daily.getAsJsonArray("temperature_2m_max");
            JsonArray minTemps = daily.getAsJsonArray("temperature_2m_min");

            if (codes.size() > 0) {
                int code = codes.get(0).getAsInt();
                double max = maxTemps.get(0).getAsDouble();
                double min = minTemps.get(0).getAsDouble();

                String description = getWeatherDescription(code);
                return String.format("%s (%.1f°C / %.1f°C)", description, min, max);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Météo indisponible";
    }

    private static String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "Ensoleillé";
            case 1, 2, 3 -> "Partiellement nuageux";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55 -> "Bruine";
            case 61, 63, 65 -> "Pluie";
            case 71, 73, 75 -> "Neige";
            case 80, 81, 82 -> "Averses de pluie";
            case 95, 96, 99 -> "Orages";
            default -> "Variable";
        };
    }
}
