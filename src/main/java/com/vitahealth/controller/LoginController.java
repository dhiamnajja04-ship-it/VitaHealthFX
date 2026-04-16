package com.vitahealth.controller;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.entity.User;
import com.vitahealth.App;
import com.vitahealth.util.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    private final UserDAO userDAO = new UserDAO();
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
        loginBtn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12; -fx-background-radius: 25; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        Hyperlink registerLink = new Hyperlink("📝 Pas encore de compte ? S'inscrire");
        registerLink.setStyle("-fx-text-fill: #2c3e66; -fx-font-size: 12px;");
        registerLink.setOnAction(e -> openRegister());

        passwordField.setOnAction(e -> handleLogin());

        card.getChildren().addAll(title, emailField, passwordField, messageLabel, loginBtn, registerLink);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 1200, 800);
        // ✅ Forcer le light mode
        scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());
        return scene;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("❌ Veuillez remplir tous les champs");
            return;
        }

        User user = userDAO.authenticate(email, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);

            messageLabel.setStyle("-fx-text-fill: #28a745;");
            messageLabel.setText("✅ Connexion réussie ! Redirection...");

            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(e -> redirectByRole(user));
            delay.play();
        } else {
            showError("❌ Email ou mot de passe incorrect");
        }
    }

    private void redirectByRole(User user) {
        try {
            String fxmlFile;
            String title;

            switch (user.getRole().toUpperCase()) {
                case "ADMIN":
                    fxmlFile = "/fxml/AdminDashboard.fxml";
                    title = "VitaHealthFX - Administration";
                    break;
                case "DOCTOR":
                    fxmlFile = "/fxml/DoctorDashboard.fxml";
                    title = "VitaHealthFX - Espace Médecin";
                    break;
                case "PATIENT":
                    fxmlFile = "/fxml/PatientDashboard.fxml";
                    title = "VitaHealthFX - Espace Patient";
                    break;
                default:
                    fxmlFile = "/fxml/HomeView.fxml";
                    title = "VitaHealthFX";
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(root, 1200, 800);
            // ✅ Forcer le light mode
            scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("❌ Erreur lors de la redirection : " + e.getMessage());
        }
    }

    private void openRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene scene = new Scene(root, 1200, 800);
            // ✅ Forcer le light mode
            scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Inscription");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("❌ Impossible d'ouvrir la page d'inscription");
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #dc3545;");
        messageLabel.setText(message);
    }
}