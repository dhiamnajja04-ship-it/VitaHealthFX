package tn.esprit.vitahealthfx.controller;

import tn.esprit.vitahealthfx.dao.UserDAO;
import tn.esprit.vitahealthfx.entity.User;
import tn.esprit.vitahealthfx.util.SessionManager;
import tn.esprit.vitahealthfx.util.CaptchaGenerator;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField, captchaField;
    @FXML private PasswordField passwordField;
    @FXML private Label captchaLabel, messageLabel;
    @FXML private Button loginBtn;

    private UserDAO userDAO = new UserDAO();
    private String currentCaptcha;

    @FXML public void initialize() { refreshCaptcha(); }
    @FXML public void refreshCaptcha() { currentCaptcha = CaptchaGenerator.generateCaptcha(); captchaLabel.setText(currentCaptcha); }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) { messageLabel.setText("❌ Tous les champs sont requis"); return; }
        if (!captchaField.getText().equals(currentCaptcha)) { messageLabel.setText("❌ Captcha incorrect"); refreshCaptcha(); captchaField.clear(); return; }

        User user = userDAO.authenticate(email, password);
        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            messageLabel.setText("✅ Connexion réussie !");
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(e -> redirectByRole(user));
            delay.play();
        } else { messageLabel.setText("❌ Email ou mot de passe incorrect"); }
    }

    private void redirectByRole(User user) {
        try {
            String fxml = user.getRole().equals("ADMIN") ? "/fxml/AdminDashboard.fxml" : "/fxml/PatientDashboard.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
        } catch (IOException e) { messageLabel.setText("❌ Erreur de redirection"); }
    }

    @FXML public void openRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 750));
        } catch (IOException e) { messageLabel.setText("❌ Impossible d'ouvrir la page"); }
    }

    @FXML public void handleForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ForgotPasswordView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 450, 400));
            stage.show();
        } catch (IOException e) { messageLabel.setText("❌ Impossible"); }
    }

    public Scene getScene() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            return scene;
        } catch (IOException e) { return new Scene(new Label("Erreur"), 550, 650); }
    }
}