package com.vitahealth.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class HomeController {

    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Button getStartedBtn;

    @FXML
    public void initialize() {
        System.out.println("🏠 HomeController initialisé");

        if (loginBtn != null) {
            loginBtn.setOnAction(e -> openLogin());
        }

        if (registerBtn != null) {
            registerBtn.setOnAction(e -> openRegister());
        }

        if (getStartedBtn != null) {
            getStartedBtn.setOnAction(e -> openLogin());
        }
    }

    private void openLogin() {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            Scene scene = new Scene(loginView, 450, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            System.out.println("✅ Ouverture de la page de connexion");
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture Login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openRegister() {
        try {
            Parent registerView = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Stage stage = (Stage) registerBtn.getScene().getWindow();
            Scene scene = new Scene(registerView, 500, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Inscription");
            System.out.println("✅ Ouverture de la page d'inscription");
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture Register: " + e.getMessage());
            e.printStackTrace();
        }
    }
}