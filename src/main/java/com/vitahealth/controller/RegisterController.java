package com.vitahealth.controller;

import com.vitahealth.dao.UserDAO;
import com.vitahealth.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
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

    // Patterns pour la validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-zÀ-ÿ\\s]{2,50}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();

        roleGroup = new ToggleGroup();
        patientRadio.setToggleGroup(roleGroup);
        doctorRadio.setToggleGroup(roleGroup);
        patientRadio.setSelected(true);

        registerButton.setOnAction(e -> handleRegister());
        loginButton.setOnAction(e -> openLogin());

        // Ajouter des listeners pour la validation en temps réel
        firstNameField.textProperty().addListener((obs, old, val) -> validateFirstName());
        lastNameField.textProperty().addListener((obs, old, val) -> validateLastName());
        emailField.textProperty().addListener((obs, old, val) -> validateEmail());
        passwordField.textProperty().addListener((obs, old, val) -> validatePassword());
        confirmPasswordField.textProperty().addListener((obs, old, val) -> validateConfirmPassword());

        // ✅ AJOUTER LES TOOLTIPS
        setupTooltips();

        // Styles CSS
        registerButton.getStyleClass().add("btn-primary");
        loginButton.getStyleClass().add("btn-outline");

        System.out.println("✅ RegisterController initialisé");
    }

    /**
     * ✅ Configuration des Tooltips pour guider l'utilisateur
     */
    private void setupTooltips() {
        // Tooltip pour le prénom
        Tooltip firstNameTip = new Tooltip("📝 Le prénom doit contenir :\n• 2 à 50 caractères\n• Uniquement des lettres\n• Espaces acceptés");
        firstNameTip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(firstNameField, firstNameTip);

        // Tooltip pour le nom
        Tooltip lastNameTip = new Tooltip("📝 Le nom doit contenir :\n• 2 à 50 caractères\n• Uniquement des lettres\n• Espaces acceptés");
        lastNameTip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(lastNameField, lastNameTip);

        // Tooltip pour l'email
        Tooltip emailTip = new Tooltip("📧 Format email valide :\n• Exemple: nom@domaine.com\n• Exemple: prenom.nom@email.fr");
        emailTip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(emailField, emailTip);

        // Tooltip pour le mot de passe
        Tooltip passwordTip = new Tooltip("🔒 Mot de passe sécurisé :\n• Minimum 6 caractères\n• Au moins 1 majuscule\n• Au moins 1 minuscule\n• Au moins 1 chiffre\n• Exemple: MonPassword123");
        passwordTip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(passwordField, passwordTip);

        // Tooltip pour la confirmation
        Tooltip confirmTip = new Tooltip("✅ Confirmation :\n• Doit être identique au mot de passe\n• Vérifie qu'il n'y a pas de faute de frappe");
        confirmTip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(confirmPasswordField, confirmTip);

        // Tooltip pour le rôle Patient
        Tooltip patientTip = new Tooltip("👤 Patient :\n• Accès à votre espace patient\n• Prendre des rendez-vous\n• Suivre votre profil santé");
        patientTip.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(patientRadio, patientTip);

        // Tooltip pour le rôle Médecin
        Tooltip doctorTip = new Tooltip("👨‍⚕️ Médecin :\n• Accès à votre espace médecin\n• Gérer vos rendez-vous\n• Accès aux dossiers patients");
        doctorTip.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(doctorRadio, doctorTip);

        // Tooltip pour le bouton d'inscription
        Tooltip registerTip = new Tooltip("📝 Créer votre compte\nTous les champs sont obligatoires");
        registerTip.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(registerButton, registerTip);

        // Tooltip pour le lien de connexion
        Tooltip loginTip = new Tooltip("🔑 Déjà un compte ?\nCliquez ici pour vous connecter");
        loginTip.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(loginButton, loginTip);
    }

    private void validateFirstName() {
        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty()) {
            setFieldError(firstNameField, "Prénom requis");
        } else if (!NAME_PATTERN.matcher(firstName).matches()) {
            setFieldError(firstNameField, "Prénom invalide (2-50 lettres)");
        } else {
            clearFieldError(firstNameField);
        }
    }

    private void validateLastName() {
        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty()) {
            setFieldError(lastNameField, "Nom requis");
        } else if (!NAME_PATTERN.matcher(lastName).matches()) {
            setFieldError(lastNameField, "Nom invalide (2-50 lettres)");
        } else {
            clearFieldError(lastNameField);
        }
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            setFieldError(emailField, "Email requis");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            setFieldError(emailField, "Email invalide (ex: nom@domaine.com)");
        } else {
            clearFieldError(emailField);
        }
    }

    private void validatePassword() {
        String password = passwordField.getText();
        if (password.isEmpty()) {
            setFieldError(passwordField, "Mot de passe requis");
        } else if (password.length() < 6) {
            setFieldError(passwordField, "6 caractères minimum");
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            setFieldError(passwordField, "Doit contenir: 1 maj, 1 min, 1 chiffre");
        } else {
            clearFieldError(passwordField);
            validateConfirmPassword();
        }
    }

    private void validateConfirmPassword() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (confirm.isEmpty()) {
            setFieldError(confirmPasswordField, "Confirmation requise");
        } else if (!password.equals(confirm)) {
            setFieldError(confirmPasswordField, "Les mots de passe ne correspondent pas");
        } else {
            clearFieldError(confirmPasswordField);
        }
    }

    private void setFieldError(Control field, String message) {
        field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 10px;");
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
    }

    private void clearFieldError(Control field) {
        field.setStyle("");
        if (errorLabel.getText() != null && errorLabel.getText().contains("Veuillez")) {
            errorLabel.setText("");
        }
    }

    private boolean isFormValid() {
        validateFirstName();
        validateLastName();
        validateEmail();
        validatePassword();
        validateConfirmPassword();

        return !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty() &&
                EMAIL_PATTERN.matcher(emailField.getText().trim()).matches() &&
                PASSWORD_PATTERN.matcher(passwordField.getText()).matches() &&
                passwordField.getText().equals(confirmPasswordField.getText());
    }

    private void handleRegister() {
        errorLabel.setText("");
        successLabel.setText("");

        if (!isFormValid()) {
            errorLabel.setText("❌ Veuillez corriger les erreurs dans le formulaire");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = patientRadio.isSelected() ? "PATIENT" : "DOCTOR";

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);

        // Désactiver le bouton pendant l'inscription
        registerButton.setDisable(true);
        registerButton.setText("Inscription en cours...");

        boolean created = userDAO.createUser(newUser);

        if (created) {
            successLabel.setText("✅ Inscription réussie ! Redirection vers la connexion...");
            successLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px;");
            errorLabel.setText("");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        registerButton.setDisable(false);
                        registerButton.setText("S'INSCRIRE");
                        openLogin();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            registerButton.setDisable(false);
            registerButton.setText("S'INSCRIRE");
            errorLabel.setText("❌ Cet email existe déjà. Veuillez en choisir un autre.");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        }
    }

    private void openLogin() {
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loginView, 450, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}