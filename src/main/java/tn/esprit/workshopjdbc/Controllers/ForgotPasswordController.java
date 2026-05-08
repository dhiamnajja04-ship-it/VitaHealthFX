package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;  // ← IMPORT AJOUTÉ
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ServiceVitaHealth;
import tn.esprit.workshopjdbc.Utils.CaptchaGenerator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField captchaField;
    @FXML private Label captchaLabel;
    @FXML private Label messageLabel;
    @FXML private Button verifyEmailBtn;
    @FXML private Button resetPasswordBtn;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Panels pour les étapes
    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;

    private ServiceVitaHealth service;
    private String generatedCaptcha;
    private String verificationCode;
    private String userEmail;
    private int userId;

    @FXML
    public void initialize() {
        service = new ServiceVitaHealth();
        showStep1();
        generateNewCaptcha();
    }

    private void showStep1() {
        if (step1Box != null) step1Box.setVisible(true);
        if (step2Box != null) step2Box.setVisible(false);
        if (step3Box != null) step3Box.setVisible(false);
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.setStyle("-fx-text-fill: #333;");
        }
    }

    private void showStep2() {
        if (step1Box != null) step1Box.setVisible(false);
        if (step2Box != null) step2Box.setVisible(true);
        if (step3Box != null) step3Box.setVisible(false);
    }

    private void showStep3() {
        if (step1Box != null) step1Box.setVisible(false);
        if (step2Box != null) step2Box.setVisible(false);
        if (step3Box != null) step3Box.setVisible(true);
    }

    private void generateNewCaptcha() {
        generatedCaptcha = CaptchaGenerator.generateCaptcha(6);
        if (captchaLabel != null) captchaLabel.setText(generatedCaptcha);
        if (captchaField != null) captchaField.clear();
    }

    @FXML
    private void refreshCaptcha() {
        generateNewCaptcha();
    }

    @FXML
    private void handleVerifyEmail() {
        String email = emailField.getText().trim();
        String userCaptcha = captchaField.getText().trim();

        // 1. Vérifier CAPTCHA
        if (!generatedCaptcha.equals(userCaptcha)) {
            messageLabel.setText("❌ CAPTCHA incorrect ! Veuillez réessayer.");
            messageLabel.setStyle("-fx-text-fill: red;");
            generateNewCaptcha();
            return;
        }

        // 2. Vérifier si l'email existe
        try {
            User user = service.getUserByEmail(email);
            if (user == null) {
                messageLabel.setText("❌ Aucun compte trouvé avec cet email !");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            userEmail = email;
            userId = user.getId();
            
            // 3. Générer un code de vérification
            verificationCode = generateVerificationCode();
            
            // 4. Simuler l'envoi d'email
            sendSimulatedEmail(userEmail, verificationCode);
            
            messageLabel.setText("✅ Code envoyé à " + email + " (Simulation: " + verificationCode + ")");
            messageLabel.setStyle("-fx-text-fill: green;");
            showStep2();

        } catch (SQLException e) {
            messageLabel.setText("❌ Erreur: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            messageLabel.setText("❌ Erreur inattendue: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleVerifyCode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Code de vérification");
        dialog.setHeaderText("Entrez le code reçu par email");
        dialog.setContentText("Code à 6 chiffres:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && result.get().equals(verificationCode)) {
            messageLabel.setText("✅ Code vérifié ! Créez votre nouveau mot de passe.");
            messageLabel.setStyle("-fx-text-fill: green;");
            showStep3();
        } else {
            messageLabel.setText("❌ Code incorrect ! Veuillez réessayer.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Vérifier la longueur du mot de passe
        if (newPassword == null || newPassword.length() < 4) {
            messageLabel.setText("❌ Le mot de passe doit contenir au moins 4 caractères !");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Vérifier la confirmation
        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("❌ Les mots de passe ne correspondent pas !");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Réinitialiser le mot de passe
        try {
            boolean success = service.resetPassword(userId, newPassword);
            if (success) {
                messageLabel.setText("✅ Mot de passe réinitialisé avec succès !");
                messageLabel.setStyle("-fx-text-fill: green;");
                
                // Rediriger vers login après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> handleBackToLogin());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                messageLabel.setText("❌ Erreur lors de la réinitialisation !");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            messageLabel.setText("❌ Erreur: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendSimulatedEmail(String email, String code) {
        System.out.println("📧 === SIMULATION D'ENVOI D'EMAIL ===");
        System.out.println("   À: " + email);
        System.out.println("   Sujet: Code de réinitialisation - VitaHealth");
        System.out.println("   Message: Votre code de vérification est: " + code);
        System.out.println("   Ce code est valable 15 minutes.");
        System.out.println("====================================");
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("VitaHealth - Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: fermer la fenêtre
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.close();
        }
    }
}