package com.vitahealth.controller;

import com.vitahealth.dao.UserDAO;
import com.vitahealth.entity.Role;
import com.vitahealth.entity.User;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private RadioButton patientRadio;
    @FXML private RadioButton doctorRadio;
    @FXML private ToggleGroup roleGroup;
    @FXML private Button registerButton;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private UserDAO userDAO;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]{2,50}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();

        roleGroup = new ToggleGroup();
        patientRadio.setToggleGroup(roleGroup);
        doctorRadio.setToggleGroup(roleGroup);
        patientRadio.setSelected(true);

        registerButton.setOnAction(e -> handleRegister());
        loginButton.setOnAction(e -> openLogin());

        firstNameField.textProperty().addListener((obs, old, val) -> validateFirstName());
        lastNameField.textProperty().addListener((obs, old, val) -> validateLastName());
        emailField.textProperty().addListener((obs, old, val) -> validateEmail());
        passwordField.textProperty().addListener((obs, old, val) -> validatePassword());
        confirmPasswordField.textProperty().addListener((obs, old, val) -> validateConfirmPassword());

        setupTooltips();
    }

    private void setupTooltips() {
        installTooltip(firstNameField, "📝 Prénom : 2-50 lettres", "#2c3e50");
        installTooltip(lastNameField, "📝 Nom : 2-50 lettres", "#2c3e50");
        installTooltip(emailField, "📧 Email valide", "#2c3e50");
        installTooltip(passwordField, "🔒 6+ caractères, 1 maj, 1 min, 1 chiffre", "#2c3e50");
        installTooltip(confirmPasswordField, "✅ Doit correspondre", "#2c3e50");
    }

    private void installTooltip(Control control, String text, String color) {
        Tooltip tip = new Tooltip(text);
        tip.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        Tooltip.install(control, tip);
    }

    private boolean validateFirstName() {
        String v = firstNameField.getText().trim();
        if (v.isEmpty()) { setFieldError(firstNameField, "Prénom requis"); return false; }
        if (!NAME_PATTERN.matcher(v).matches()) { setFieldError(firstNameField, "Prénom invalide (2-50 lettres)"); return false; }
        clearFieldError(firstNameField);
        return true;
    }

    private boolean validateLastName() {
        String v = lastNameField.getText().trim();
        if (v.isEmpty()) { setFieldError(lastNameField, "Nom requis"); return false; }
        if (!NAME_PATTERN.matcher(v).matches()) { setFieldError(lastNameField, "Nom invalide (2-50 lettres)"); return false; }
        clearFieldError(lastNameField);
        return true;
    }

    private boolean validateEmail() {
        String v = emailField.getText().trim();
        if (v.isEmpty()) { setFieldError(emailField, "Email requis"); return false; }
        if (!EMAIL_PATTERN.matcher(v).matches()) { setFieldError(emailField, "Email invalide"); return false; }
        clearFieldError(emailField);
        return true;
    }

    private boolean validatePassword() {
        String v = passwordField.getText();
        if (v.isEmpty()) { setFieldError(passwordField, "Mot de passe requis"); return false; }
        if (v.length() < 6) { setFieldError(passwordField, "6 caractères minimum"); return false; }
        if (!PASSWORD_PATTERN.matcher(v).matches()) {
            setFieldError(passwordField, "1 maj, 1 min, 1 chiffre requis");
            return false;
        }
        clearFieldError(passwordField);
        validateConfirmPassword();
        return true;
    }

    private boolean validateConfirmPassword() {
        String confirm = confirmPasswordField.getText();
        if (confirm.isEmpty()) { setFieldError(confirmPasswordField, "Confirmation requise"); return false; }
        if (!passwordField.getText().equals(confirm)) {
            setFieldError(confirmPasswordField, "Les mots de passe ne correspondent pas");
            return false;
        }
        clearFieldError(confirmPasswordField);
        return true;
    }

    private boolean isFormValid() {
        boolean ok = validateFirstName();
        ok &= validateLastName();
        ok &= validateEmail();
        ok &= validatePassword();
        ok &= validateConfirmPassword();
        return ok;
    }

    private void handleRegister() {
        errorLabel.setText("");
        successLabel.setText("");

        if (!isFormValid()) {
            showError("❌ Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        String role = patientRadio.isSelected() ? Role.PATIENT : Role.MEDECIN;

        User newUser = new User();
        newUser.setFirstName(firstNameField.getText().trim());
        newUser.setLastName(lastNameField.getText().trim());
        newUser.setEmail(emailField.getText().trim());
        newUser.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
        newUser.setRole(role);
        newUser.setVerified(false);

        registerButton.setDisable(true);
        registerButton.setText("⏳ Inscription en cours...");

        try {
            boolean created = userDAO.ajouter(newUser);
            if (created) {
                successLabel.setText("✅ Inscription réussie ! Redirection vers la connexion...");
                successLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                errorLabel.setText("");

                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(e -> openLogin());
                delay.play();
            } else {
                showError("❌ Cet email existe déjà. Veuillez en choisir un autre.");
                registerButton.setDisable(false);
                registerButton.setText("S'INSCRIRE");
            }
        } catch (SQLException e) {
            showError("❌ Erreur technique : " + e.getMessage());
            registerButton.setDisable(false);
            registerButton.setText("S'INSCRIRE");
        }
    }

    private void openLogin() {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loginView, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 13px;");
        successLabel.setText("");
    }

    private void setFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 12px;");
        showError(message);
    }

    private void clearFieldError(Control field) {
        field.setStyle("");
    }
}