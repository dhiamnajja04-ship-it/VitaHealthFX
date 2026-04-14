package org.example.controllers;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.service.ServiceVitaHealth;
import org.example.entity.User;
import org.example.App;
import java.sql.SQLException;

public class LoginController {
    private ServiceVitaHealth service = new ServiceVitaHealth();
    private TextField emailField;
    private PasswordField passwordField;
    private Label messageLabel;

    public Scene getScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #16213e);");

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 40;");

        Text title = new Text("🌟 VITAHEALTH");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: #2c3e66;");

        emailField = new TextField();
        emailField.setPromptText("📧 Email");
        emailField.setStyle("-fx-padding: 12; -fx-background-radius: 10;");

        passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Mot de passe");
        passwordField.setStyle("-fx-padding: 12; -fx-background-radius: 10;");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");

        Button loginBtn = new Button("SE CONNECTER");
        loginBtn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 25; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        card.getChildren().addAll(title, emailField, passwordField, messageLabel, loginBtn);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        return scene;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("❌ Veuillez remplir tous les champs");
            return;
        }

        try {
            User user = service.login(email, password);
            if (user != null) {
                messageLabel.setStyle("-fx-text-fill: #28a745;");
                messageLabel.setText("✅ Connexion réussie ! Redirection...");

                // Redirection après 1 seconde
                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(e -> {
                    switch (user.getRole()) {
                        case "admin":
                            App.changeScene(new AdminDashboardController(user).getScene());
                            break;
                        default:
                            messageLabel.setText("Rôle: " + user.getRole());
                    }
                });
                delay.play();
            } else {
                messageLabel.setStyle("-fx-text-fill: #dc3545;");
                messageLabel.setText("❌ Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            messageLabel.setText("❌ Erreur: " + e.getMessage());
        }
    }
}