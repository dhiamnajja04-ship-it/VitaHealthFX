package com.vitahealth.view;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

public class PharmacieMapView extends BorderPane {
    private final WebView webView;
    private final WebEngine webEngine;

    public PharmacieMapView() {
        webView = new WebView();
        webEngine = webView.getEngine();

        // Autoriser la géolocalisation (la WebView demande la permission)
        webEngine.setConfirmHandler(message -> {
            if (message.contains("geolocation")) {
                return true; // accepte automatiquement
            }
            return false;
        });

        webEngine.load(getClass().getResource("/html/pharmacy_map.html").toExternalForm());
        this.setCenter(webView);
    }
}