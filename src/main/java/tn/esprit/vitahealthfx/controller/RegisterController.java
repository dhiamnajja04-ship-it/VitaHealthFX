package tn.esprit.vitahealthfx.controller;

import tn.esprit.vitahealthfx.dao.UserDAO;
import tn.esprit.vitahealthfx.entity.User;
import tn.esprit.vitahealthfx.util.CaptchaGenerator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField firstNameField, lastNameField, emailField, phoneField, captchaField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label captchaLabel, messageLabel;
    @FXML private CheckBox robotCheckBox;
    @FXML private Button registerBtn;

    private UserDAO userDAO = new UserDAO();
    private String currentCaptcha;

    // Patterns de validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]{2,50}$");

    @FXML
    public void initialize() {
        refreshCaptcha();
        roleCombo.getItems().addAll("PATIENT", "DOCTOR");
        roleCombo.setValue("PATIENT");
    }

    @FXML
    public void refreshCaptcha() {
        currentCaptcha = CaptchaGenerator.generateCaptcha();
        captchaLabel.setText(currentCaptcha);
    }

    @FXML
    public void handleRegister() {
        // 1. Vérifier "I'm not a robot"
        if (!robotCheckBox.isSelected()) {
            messageLabel.setText("🤖 Veuillez confirmer que vous n'êtes pas un robot");
            return;
        }

        // 2. Captcha
        if (!captchaField.getText().equals(currentCaptcha)) {
            messageLabel.setText("❌ Captcha incorrect");
            refreshCaptcha();
            captchaField.clear();
            return;
        }

        // 3. Récupération des valeurs
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleCombo.getValue();

        // 4. Validation des champs
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            messageLabel.setText("❌ Tous les champs sont obligatoires");
            return;
        }

        // 5. Validation prénom/nom (lettres seulement)
        if (!NAME_PATTERN.matcher(firstName).matches()) {
            messageLabel.setText("❌ Prénom invalide (2-50 lettres)");
            return;
        }
        if (!NAME_PATTERN.matcher(lastName).matches()) {
            messageLabel.setText("❌ Nom invalide (2-50 lettres)");
            return;
        }

        // 6. Validation email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            messageLabel.setText("❌ Email invalide (ex: nom@domaine.com)");
            return;
        }

        // 7. Validation téléphone
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            messageLabel.setText("❌ Téléphone invalide (8-15 chiffres, + optionnel)");
            return;
        }

        // 8. Validation mot de passe (min 6 caractères)
        if (password.length() < 6) {
            messageLabel.setText("❌ Mot de passe (minimum 6 caractères)");
            return;
        }

        // 9. Confirmation mot de passe
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("❌ Les mots de passe ne correspondent pas");
            return;
        }

        // 10. Création de l'utilisateur
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole(role);

        if (userDAO.register(user)) {
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            messageLabel.setText("✅ Inscription réussie ! Redirection...");
            
            // Redirection vers login après 2 secondes
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                javafx.application.Platform.runLater(() -> openLogin());
            }).start();
        } else {
            messageLabel.setText("❌ Cet email est déjà utilisé");
        }
    }

    @FXML
    public void openLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) registerBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 550, 650));
            stage.setTitle("Connexion - VITA");
        } catch (IOException e) {
            messageLabel.setText("❌ Impossible d'ouvrir la page de connexion");
        }
    }
}