package com.vitahealth.controller;

import com.vitahealth.dao.UserDAO;
import com.vitahealth.model.User;
import com.vitahealth.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    private UserDAO userDAO;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @FXML
    public void initialize() {
        System.out.println("=== LoginController initialisé ===");
        userDAO = new UserDAO();

        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> openRegister());
        passwordField.setOnAction(e -> handleLogin());

        // Validation en temps réel
        emailField.textProperty().addListener((obs, old, val) -> validateEmail());
        passwordField.textProperty().addListener((obs, old, val) -> validatePassword());

        // Styles CSS
        loginButton.getStyleClass().add("btn-primary");
        registerButton.getStyleClass().add("btn-outline");
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
        } else {
            emailField.setStyle("");
        }
    }

    private void validatePassword() {
        String password = passwordField.getText();
        if (!password.isEmpty() && password.length() < 6) {
            passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
        } else {
            passwordField.setStyle("");
        }
    }

    private boolean isFormValid() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            errorLabel.setText("Veuillez saisir votre email");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorLabel.setText("Email invalide");
            return false;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Veuillez saisir votre mot de passe");
            return false;
        }
        if (password.length() < 6) {
            errorLabel.setText("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }
        return true;
    }

    private void handleLogin() {
        errorLabel.setText("");

        if (!isFormValid()) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Désactiver le bouton pendant l'authentification
        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours...");

        User user = userDAO.authenticate(email, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            errorLabel.setText("✅ Connexion réussie !");
            errorLabel.setStyle("-fx-text-fill: #27ae60;");

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> redirectToDashboard(user));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            loginButton.setDisable(false);
            loginButton.setText("SE CONNECTER");
            errorLabel.setText("❌ Email ou mot de passe incorrect");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void redirectToDashboard(User user) {
        try {
            String fxmlFile;
            int width, height;
            String role = user.getRole().toUpperCase();

            switch (role) {
                case "ADMIN":
                    fxmlFile = "/fxml/AdminDashboard.fxml";
                    width = 1200;
                    height = 700;
                    break;
                case "DOCTOR":
                    fxmlFile = "/fxml/DoctorDashboard.fxml";
                    width = 1100;
                    height = 700;
                    break;
                case "PATIENT":
                    fxmlFile = "/fxml/PatientDashboard.fxml";
                    width = 1000;
                    height = 700;
                    break;
                default:
                    fxmlFile = "/fxml/HomeView.fxml";
                    width = 1200;
                    height = 700;
            }

            Parent dashboard = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(dashboard, width, height);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - " + role);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur technique: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            loginButton.setDisable(false);
            loginButton.setText("SE CONNECTER");
        }
    }

    private void openRegister() {
        try {
            Parent registerView = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene scene = new Scene(registerView, 500, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Inscription");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}