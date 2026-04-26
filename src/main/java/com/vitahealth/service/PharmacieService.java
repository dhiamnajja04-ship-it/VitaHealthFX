package com.vitahealth.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PharmacieService {

    private final HttpClient client = HttpClient.newHttpClient();

    public static class Pharmacy {
        private double lat, lon;
        private String name;
        private String address;
        private double distance;

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
    }

    public interface Callback {
        void onSuccess(List<Pharmacy> pharmacies, double radiusUsed);
        void onError(String message);
    }

    // Pour tester, on force Tunis. Vous pourrez ensuite réactiver l'IP.
    public void autoSearchNearby(Callback callback) {
        // Tunis centre
        double lat = 36.8065;
        double lon = 10.1815;
        searchWithIncreasingRadius(lat, lon, callback);
    }

    private void searchWithIncreasingRadius(double lat, double lon, Callback callback) {
        searchWithRadius(lat, lon, 5.0, callback, new int[]{5, 10, 20, 50});
    }

    private void searchWithRadius(double lat, double lon, double radius, Callback callback, int[] radiusList) {
        int radiusM = (int)(radius * 1000);
        String overpassQuery = "[out:json];(node[\"amenity\"=\"pharmacy\"](around:" + radiusM + "," + lat + "," + lon + ");way[\"amenity\"=\"pharmacy\"](around:" + radiusM + "," + lat + "," + lon + ");relation[\"amenity\"=\"pharmacy\"](around:" + radiusM + "," + lat + "," + lon + "););out center;";
        String url = "https://overpass-api.de/api/interpreter?data=" + encode(overpassQuery);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "VitaHealthFX/2.0")
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Pharmacy> pharmacies = parseOverpass(json, lat, lon);
                        if (!pharmacies.isEmpty()) {
                            callback.onSuccess(pharmacies, radius);
                        } else {
                            int nextRadius = getNextRadius(radius, radiusList);
                            if (nextRadius > 0) {
                                searchWithRadius(lat, lon, nextRadius, callback, radiusList);
                            } else {
                                callback.onSuccess(new ArrayList<>(), radius);
                            }
                        }
                    } catch (Exception e) {
                        callback.onError("Erreur lors du chargement des pharmacies : " + e.getMessage());
                    }
                })
                .exceptionally(e -> { callback.onError("Erreur réseau Overpass"); return null; });
    }

    private int getNextRadius(double current, int[] list) {
        for (int i = 0; i < list.length - 1; i++) {
            if (list[i] == (int)current) return list[i+1];
        }
        return -1;
    }

    private List<Pharmacy> parseOverpass(String json, double centerLat, double centerLon) {
        List<Pharmacy> list = new ArrayList<>();
        String[] elements = json.split("\\{");
        for (String elem : elements) {
            if (elem.contains("\"amenity\"") && elem.contains("\"pharmacy\"")) {
                Pharmacy p = new Pharmacy();
                // Latitude
                int latIdx = elem.indexOf("\"lat\":");
                if (latIdx != -1) {
                    int latStart = elem.indexOf(":", latIdx) + 1;
                    int latEnd = elem.indexOf(",", latStart);
                    if (latEnd == -1) latEnd = elem.indexOf("}", latStart);
                    try {
                        p.setLat(Double.parseDouble(elem.substring(latStart, latEnd).trim()));
                    } catch (NumberFormatException ignored) {}
                } else {
                    int centerIdx = elem.indexOf("\"center\":");
                    if (centerIdx != -1) {
                        String centerPart = elem.substring(centerIdx);
                        int clatIdx = centerPart.indexOf("\"lat\":");
                        if (clatIdx != -1) {
                            int clatStart = centerPart.indexOf(":", clatIdx) + 1;
                            int clatEnd = centerPart.indexOf(",", clatStart);
                            if (clatEnd == -1) clatEnd = centerPart.indexOf("}", clatStart);
                            p.setLat(Double.parseDouble(centerPart.substring(clatStart, clatEnd).trim()));
                        }
                    }
                }
                // Longitude
                int lonIdx = elem.indexOf("\"lon\":");
                if (lonIdx != -1) {
                    int lonStart = elem.indexOf(":", lonIdx) + 1;
                    int lonEnd = elem.indexOf(",", lonStart);
                    if (lonEnd == -1) lonEnd = elem.indexOf("}", lonStart);
                    p.setLon(Double.parseDouble(elem.substring(lonStart, lonEnd).trim()));
                }
                if (p.getLat() == 0 && p.getLon() == 0) continue;

                // Nom
                int nameIdx = elem.indexOf("\"name\":\"");
                if (nameIdx != -1) {
                    int nameStart = nameIdx + 8;
                    int nameEnd = elem.indexOf("\"", nameStart);
                    p.setName(elem.substring(nameStart, nameEnd));
                } else {
                    p.setName("Pharmacie");
                }
                // Adresse
                String street = null, city = null;
                int streetIdx = elem.indexOf("\"addr:street\":\"");
                if (streetIdx != -1) {
                    int sStart = streetIdx + 15;
                    int sEnd = elem.indexOf("\"", sStart);
                    street = elem.substring(sStart, sEnd);
                }
                int cityIdx = elem.indexOf("\"addr:city\":\"");
                if (cityIdx != -1) {
                    int cStart = cityIdx + 13;
                    int cEnd = elem.indexOf("\"", cStart);
                    city = elem.substring(cStart, cEnd);
                }
                String address = "";
                if (street != null) address += street;
                if (city != null) address += (address.isEmpty() ? "" : ", ") + city;
                p.setAddress(address.isEmpty() ? "Adresse inconnue" : address);

                double dist = haversine(centerLat, centerLon, p.getLat(), p.getLon());
                p.setDistance(dist);
                list.add(p);
            }
        }
        return list;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public void openGoogleMapsDirections(double lat, double lon) {
        String url = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
        try {
            java.awt.Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}