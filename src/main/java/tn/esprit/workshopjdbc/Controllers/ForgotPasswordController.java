package tn.esprit.workshopjdbc.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.OtpVerificationService;
import tn.esprit.workshopjdbc.Services.ServiceVitaHealth;
import tn.esprit.workshopjdbc.Utils.CaptchaGenerator;
import tn.esprit.workshopjdbc.Utils.NotificationManager;

import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField captchaField;
    @FXML private TextField otpCodeField;
    @FXML private Label captchaLabel;
    @FXML private Label messageLabel;
    @FXML private Button verifyEmailBtn;
    @FXML private Button resetPasswordBtn;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;

    private ServiceVitaHealth service;
    private OtpVerificationService otpService;
    private String generatedCaptcha;
    private String userPhone;
    private int userId;

    @FXML
    public void initialize() {
        service = new ServiceVitaHealth();
        otpService = new OtpVerificationService();
        showStep1();
        generateNewCaptcha();
    }

    private void showStep1() {
        setStepVisible(step1Box, true);
        setStepVisible(step2Box, false);
        setStepVisible(step3Box, false);
        setMessage("", "#334155");
    }

    private void showStep2() {
        setStepVisible(step1Box, false);
        setStepVisible(step2Box, true);
        setStepVisible(step3Box, false);
        if (otpCodeField != null) {
            otpCodeField.clear();
            otpCodeField.requestFocus();
        }
    }

    private void showStep3() {
        setStepVisible(step1Box, false);
        setStepVisible(step2Box, false);
        setStepVisible(step3Box, true);
        if (newPasswordField != null) newPasswordField.requestFocus();
    }

    private void setStepVisible(VBox box, boolean visible) {
        if (box != null) {
            box.setVisible(visible);
            box.setManaged(visible);
        }
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
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String userCaptcha = captchaField.getText() == null ? "" : captchaField.getText().trim();

        if (email.isBlank()) {
            setMessage("Email obligatoire.", "#ef4444");
            return;
        }

        if (!generatedCaptcha.equals(userCaptcha)) {
            setMessage("CAPTCHA incorrect. Veuillez reessayer.", "#ef4444");
            generateNewCaptcha();
            return;
        }

        try {
            User user = service.getUserByEmail(email);
            if (user == null) {
                setMessage("Aucun compte trouve avec cet email.", "#ef4444");
                return;
            }
            if (user.getPhone() == null || user.getPhone().trim().isBlank()) {
                setMessage("Ce compte n'a pas de numero telephone. Contactez l'administrateur.", "#ef4444");
                return;
            }

            userId = user.getId();
            userPhone = user.getPhone().trim();
            OtpVerificationService.OtpResult result =
                    otpService.sendCode(userPhone, "la reinitialisation du mot de passe");

            if (!result.sent() && !result.dryRun()) {
                setMessage(result.message(), "#ef4444");
                NotificationManager.showToast(scene(), "OTP mot de passe", result.message(), NotificationManager.Type.ERROR);
                return;
            }

            setMessage(result.message(), "#16a34a");
            NotificationManager.Type type = result.dryRun()
                    ? NotificationManager.Type.WARNING
                    : NotificationManager.Type.SUCCESS;
            NotificationManager.showToast(scene(), "OTP mot de passe", result.message(), type);
            showStep2();
        } catch (SQLException e) {
            setMessage("Erreur base de donnees: " + e.getMessage(), "#ef4444");
        } catch (Exception e) {
            setMessage("Erreur inattendue: " + e.getMessage(), "#ef4444");
        }
    }

    @FXML
    private void handleVerifyCode() {
        String code = otpCodeField == null || otpCodeField.getText() == null
                ? ""
                : otpCodeField.getText().trim();

        if (otpService.verify(userPhone, code)) {
            setMessage("Code OTP verifie. Creez votre nouveau mot de passe.", "#16a34a");
            NotificationManager.showToast(scene(), "OTP mot de passe", "Code valide.", NotificationManager.Type.SUCCESS);
            showStep3();
        } else {
            setMessage("Code OTP incorrect ou expire.", "#ef4444");
            NotificationManager.showToast(scene(), "OTP mot de passe", "Code incorrect ou expire.", NotificationManager.Type.ERROR);
        }
    }

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword == null || newPassword.length() < 4) {
            setMessage("Le mot de passe doit contenir au moins 4 caracteres.", "#ef4444");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            setMessage("Les mots de passe ne correspondent pas.", "#ef4444");
            return;
        }

        try {
            boolean success = service.resetPassword(userId, newPassword);
            if (success) {
                otpService.clear();
                setMessage("Mot de passe reinitialise avec succes.", "#16a34a");
                NotificationManager.showToast(scene(), "Mot de passe", "Mot de passe reinitialise.", NotificationManager.Type.SUCCESS);

                Thread redirect = new Thread(() -> {
                    try {
                        Thread.sleep(1600);
                        Platform.runLater(this::handleBackToLogin);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                redirect.setDaemon(true);
                redirect.start();
            } else {
                setMessage("Erreur lors de la reinitialisation.", "#ef4444");
            }
        } catch (SQLException e) {
            setMessage("Erreur base de donnees: " + e.getMessage(), "#ef4444");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            LoginController loginController = new LoginController();
            Scene scene = loginController.getScene();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.close();
        }
    }

    private Scene scene() {
        return emailField == null ? null : emailField.getScene();
    }

    private void setMessage(String text, String color) {
        if (messageLabel != null) {
            messageLabel.setText(text);
            messageLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }
}
