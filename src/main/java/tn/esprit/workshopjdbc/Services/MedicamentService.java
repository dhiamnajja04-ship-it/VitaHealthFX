package tn.esprit.workshopjdbc.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MedicamentService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // API BDPM hébergée par un tiers – fonctionnelle et gratuite
    private static final String BASE_URL = "https://bdpmgf.vedielaute.fr/api/medicaments/specialites";

    public static class Medicament {
        private String nom;
        private String dosage;
        private String forme;
        private String codeCIS;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getForme() { return forme; }
        public void setForme(String forme) { this.forme = forme; }
        public String getCodeCIS() { return codeCIS; }
        public void setCodeCIS(String codeCIS) { this.codeCIS = codeCIS; }

        @Override
        public String toString() {
            if (dosage != null && !dosage.isEmpty()) return nom + " " + dosage;
            return nom != null ? nom : "";
        }
    }

    public CompletableFuture<List<Medicament>> rechercher(String query) {
        if (query == null || query.trim().length() < 2) {
            return CompletableFuture.completedFuture(List.of());
        }
        // L'API accepte le wildcard * pour la recherche
        String url = BASE_URL + "?q=" + encode(query) + "*";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Accept", "application/json")
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseResponse)
                .exceptionally(ex -> {
                    System.err.println("Erreur API BDPM: " + ex.getMessage());
                    ex.printStackTrace();
                    return List.of();
                });
    }

    private List<Medicament> parseResponse(String json) {
        List<Medicament> list = new ArrayList<>();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode results = root.get("data");
            if (results != null && results.isArray()) {
                for (JsonNode item : results) {
                    Medicament m = new Medicament();
                    m.setNom(item.path("denomination").asText());
                    m.setDosage(""); // facultatif, le dosage est inclus dans la dénomination
                    m.setForme(item.path("forme_pharma").asText());
                    m.setCodeCIS(item.path("cis").asText());
                    list.add(m);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}