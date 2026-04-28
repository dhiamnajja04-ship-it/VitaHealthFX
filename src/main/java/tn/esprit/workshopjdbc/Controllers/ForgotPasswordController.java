package tn.esprit.vitahealthfx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    @FXML
    public void handleSendResetLink() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            messageLabel.setText("❌ Veuillez saisir votre email");
            return;
        }
        // Simulation d'envoi (à connecter avec EmailService)
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setText("✅ Un email de réinitialisation a été envoyé (simulation)");
    }

    @FXML
    public void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}