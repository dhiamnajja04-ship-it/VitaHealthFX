package tn.esprit.workshopjdbc.Controllers;

import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.workshopjdbc.Entities.Role;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.OtpVerificationService;
import tn.esprit.workshopjdbc.Services.SocialAuthService;
import tn.esprit.workshopjdbc.Utils.NotificationManager;
import tn.esprit.workshopjdbc.Utils.ThemeManager;
import tn.esprit.workshopjdbc.dao.UserDAO;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextField cinField;

    @FXML private RadioButton patientRadio;
    @FXML private RadioButton doctorRadio;
    @FXML private ToggleGroup roleGroup;

    @FXML private VBox patientFieldsBox;
    @FXML private VBox doctorFieldsBox;
    @FXML private TextField poidsField;
    @FXML private TextField tailleField;
    @FXML private TextField glycemieField;
    @FXML private TextField tensionField;
    @FXML private TextField maladieField;
    @FXML private TextField specialiteField;
    @FXML private TextField diplomeField;

    @FXML private Button registerButton;
    @FXML private Button loginButton;
    @FXML private Button sendOtpButton;
    @FXML private Button verifyOtpButton;
    @FXML private Button googleRegisterButton;
    @FXML private Button facebookRegisterButton;
    @FXML private Button themeToggleButton;
    @FXML private CheckBox robotCheckBox;
    @FXML private TextField otpCodeField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private UserDAO userDAO;
    private OtpVerificationService otpService;
    private SocialAuthService socialAuthService;
    private boolean phoneVerified;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s]{2,50}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+ ]{8,20}$");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        otpService = new OtpVerificationService();
        socialAuthService = new SocialAuthService();

        if (roleGroup == null) {
            roleGroup = new ToggleGroup();
            patientRadio.setToggleGroup(roleGroup);
            doctorRadio.setToggleGroup(roleGroup);
        }
        patientRadio.setSelected(true);
        updateRoleFields();

        registerButton.setOnAction(e -> handleRegister());
        loginButton.setOnAction(e -> openLogin());
        if (sendOtpButton != null) sendOtpButton.setOnAction(e -> handleSendOtp());
        if (verifyOtpButton != null) verifyOtpButton.setOnAction(e -> handleVerifyOtp());
        if (googleRegisterButton != null) googleRegisterButton.setOnAction(e -> startSocialAuth(SocialAuthService.Provider.GOOGLE));
        if (facebookRegisterButton != null) facebookRegisterButton.setOnAction(e -> startSocialAuth(SocialAuthService.Provider.FACEBOOK));
        if (themeToggleButton != null) {
            themeToggleButton.setText(ThemeManager.toggleText());
            themeToggleButton.setOnAction(e -> {
                ThemeManager.toggle(themeToggleButton.getScene());
                themeToggleButton.setText(ThemeManager.toggleText());
            });
        }
        patientRadio.setOnAction(e -> updateRoleFields());
        doctorRadio.setOnAction(e -> updateRoleFields());

        firstNameField.textProperty().addListener((obs, old, val) -> validateFirstName());
        lastNameField.textProperty().addListener((obs, old, val) -> validateLastName());
        emailField.textProperty().addListener((obs, old, val) -> validateEmail());
        passwordField.textProperty().addListener((obs, old, val) -> validatePassword());
        confirmPasswordField.textProperty().addListener((obs, old, val) -> validateConfirmPassword());
        phoneField.textProperty().addListener((obs, old, val) -> {
            phoneVerified = false;
            if (otpCodeField != null) otpCodeField.clear();
            validatePhone();
        });

        setupTooltips();
    }

    private void setupTooltips() {
        installTooltip(firstNameField, "Prenom : 2-50 lettres");
        installTooltip(lastNameField, "Nom : 2-50 lettres");
        installTooltip(emailField, "Email valide");
        installTooltip(passwordField, "6+ caracteres, 1 majuscule, 1 minuscule, 1 chiffre");
        installTooltip(confirmPasswordField, "Doit correspondre au mot de passe");
        installTooltip(phoneField, "Telephone : 8-20 chiffres");
        installTooltip(cinField, "CIN ou identifiant national");
        installTooltip(specialiteField, "Specialite obligatoire pour un medecin");
        installTooltip(diplomeField, "Diplome obligatoire pour un medecin");
    }

    private void installTooltip(Control control, String text) {
        Tooltip tip = new Tooltip(text);
        tip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        Tooltip.install(control, tip);
    }

    private void updateRoleFields() {
        boolean patient = patientRadio.isSelected();
        patientFieldsBox.setVisible(patient);
        patientFieldsBox.setManaged(patient);
        doctorFieldsBox.setVisible(!patient);
        doctorFieldsBox.setManaged(!patient);
        animateRoleBox(patient ? patientFieldsBox : doctorFieldsBox);
        errorLabel.setText("");
        successLabel.setText("");
    }

    private void animateRoleBox(VBox box) {
        if (box == null || !box.isVisible()) return;
        box.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(240), box);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(240), box);
        slide.setFromY(10);
        slide.setToY(0);
        fade.play();
        slide.play();
    }

    private boolean validateFirstName() {
        String v = firstNameField.getText().trim();
        if (v.isEmpty()) { setFieldError(firstNameField, "Prenom requis"); return false; }
        if (!NAME_PATTERN.matcher(v).matches()) { setFieldError(firstNameField, "Prenom invalide"); return false; }
        clearFieldError(firstNameField);
        return true;
    }

    private boolean validateLastName() {
        String v = lastNameField.getText().trim();
        if (v.isEmpty()) { setFieldError(lastNameField, "Nom requis"); return false; }
        if (!NAME_PATTERN.matcher(v).matches()) { setFieldError(lastNameField, "Nom invalide"); return false; }
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
        if (!PASSWORD_PATTERN.matcher(v).matches()) {
            setFieldError(passwordField, "Mot de passe: 6 caracteres, 1 maj, 1 min, 1 chiffre");
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

    private boolean validatePhone() {
        String v = phoneField.getText().trim();
        if (v.isEmpty()) { setFieldError(phoneField, "Telephone requis"); return false; }
        if (!PHONE_PATTERN.matcher(v).matches()) { setFieldError(phoneField, "Telephone invalide"); return false; }
        clearFieldError(phoneField);
        return true;
    }

    private boolean validateCommonBusinessFields() {
        boolean ok = validatePhone();
        if (cinField.getText().trim().isEmpty()) {
            setFieldError(cinField, "CIN requis");
            ok = false;
        } else {
            clearFieldError(cinField);
        }
        return ok;
    }

    private boolean validatePatientFields() {
        if (!patientRadio.isSelected()) return true;
        boolean ok = true;
        ok &= validateOptionalDouble(poidsField, "Poids invalide");
        ok &= validateOptionalDouble(tailleField, "Taille invalide");
        ok &= validateOptionalDouble(glycemieField, "Glycemie invalide");
        return ok;
    }

    private boolean validateDoctorFields() {
        if (!doctorRadio.isSelected()) return true;
        boolean ok = true;
        if (specialiteField.getText().trim().isEmpty()) {
            setFieldError(specialiteField, "Specialite requise");
            ok = false;
        } else {
            clearFieldError(specialiteField);
        }
        if (diplomeField.getText().trim().isEmpty()) {
            setFieldError(diplomeField, "Diplome requis");
            ok = false;
        } else {
            clearFieldError(diplomeField);
        }
        return ok;
    }

    private boolean validateOptionalDouble(TextField field, String message) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            clearFieldError(field);
            return true;
        }
        try {
            if (Double.parseDouble(value.replace(',', '.')) < 0) {
                setFieldError(field, message);
                return false;
            }
            clearFieldError(field);
            return true;
        } catch (NumberFormatException e) {
            setFieldError(field, message);
            return false;
        }
    }

    private boolean isFormValid() {
        boolean ok = validateFirstName();
        ok &= validateLastName();
        ok &= validateEmail();
        ok &= validatePassword();
        ok &= validateConfirmPassword();
        ok &= validateCommonBusinessFields();
        ok &= validatePatientFields();
        ok &= validateDoctorFields();
        if (!phoneVerified) {
            showError("Veuillez verifier votre numero telephone avec le code OTP.");
            ok = false;
        }
        if (robotCheckBox == null || !robotCheckBox.isSelected()) {
            showError("Confirmez d'abord: Je ne suis pas un robot.");
            ok = false;
        }
        return ok;
    }

    private void handleSendOtp() {
        if (!validatePhone()) return;
        OtpVerificationService.OtpResult result = otpService.sendCode(phoneField.getText().trim(), "l'inscription");
        NotificationManager.Type type = result.sent()
                ? NotificationManager.Type.SUCCESS
                : result.dryRun() ? NotificationManager.Type.INFO : NotificationManager.Type.ERROR;
        NotificationManager.showToast(registerButton.getScene(), "Verification telephone", result.message(), type);
        if (result.sent() || result.dryRun()) {
            successLabel.setText(result.message());
            errorLabel.setText("");
        } else {
            showError(result.message());
        }
    }

    private void handleVerifyOtp() {
        String code = otpCodeField == null ? "" : otpCodeField.getText().trim();
        phoneVerified = otpService.verify(phoneField.getText().trim(), code);
        if (phoneVerified) {
            successLabel.setText("Numero telephone verifie.");
            errorLabel.setText("");
            NotificationManager.showToast(registerButton.getScene(), "Verification telephone", "Code OTP valide.", NotificationManager.Type.SUCCESS);
        } else {
            showError("Code OTP incorrect ou expire.");
        }
    }

    private void startSocialAuth(SocialAuthService.Provider provider) {
        NotificationManager.showToast(registerButton != null ? registerButton.getScene() : null,
                "Inscription sociale",
                "Ouverture de " + provider.label() + " dans votre navigateur...", NotificationManager.Type.INFO);
        socialAuthService.authenticateAsync(provider, "register").thenAccept(result ->
                Platform.runLater(() -> handleSocialAuthResult(result)));
    }

    private void handleSocialAuthResult(SocialAuthService.AuthResult result) {
        if (!result.success()) {
            NotificationManager.showAlert("Inscription " + result.provider().label(), result.message(), Alert.AlertType.WARNING);
            return;
        }
        prefillFromSocial(result.user());
        NotificationManager.showToast(registerButton != null ? registerButton.getScene() : null,
                "Inscription sociale",
                "Profil " + result.provider().label() + " valide. Completez telephone, OTP et role.",
                NotificationManager.Type.SUCCESS);
    }

    public void prefillFromSocial(SocialAuthService.OAuthUser profile) {
        if (profile == null) return;
        if (emailField != null) emailField.setText(profile.email());
        if (firstNameField != null && profile.firstName() != null && !profile.firstName().isBlank()) {
            firstNameField.setText(profile.firstName());
        }
        if (lastNameField != null && profile.lastName() != null && !profile.lastName().isBlank()) {
            lastNameField.setText(profile.lastName());
        }
        if (successLabel != null) {
            successLabel.setText("Profil " + profile.provider() + " verifie. Completez le telephone et le code OTP.");
        }
    }

    private void handleRegister() {
        errorLabel.setText("");
        successLabel.setText("");

        if (!isFormValid()) {
            showError("Veuillez corriger les erreurs du formulaire");
            return;
        }

        String role = patientRadio.isSelected() ? Role.PATIENT : Role.DOCTOR;

        User newUser = new User();
        newUser.setFirstName(firstNameField.getText().trim());
        newUser.setLastName(lastNameField.getText().trim());
        newUser.setEmail(emailField.getText().trim());
        newUser.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
        newUser.setRole(role);
        newUser.setVerified(false);
        newUser.setPhone(phoneField.getText().trim());
        newUser.setCin(cinField.getText().trim());

        if (Role.PATIENT.equals(role)) {
            newUser.setPoids(parseNullableDouble(poidsField.getText()));
            newUser.setTaille(parseNullableDouble(tailleField.getText()));
            newUser.setGlycemie(parseNullableDouble(glycemieField.getText()));
            newUser.setTension(tensionField.getText().trim());
            newUser.setMaladie(maladieField.getText().trim());
        } else {
            newUser.setSpecialite(specialiteField.getText().trim());
            newUser.setDiplome(diplomeField.getText().trim());
        }

        registerButton.setDisable(true);
        registerButton.setText("Inscription en cours...");

        try {
            boolean created = userDAO.ajouter(newUser);
            if (created) {
                otpService.clear();
                successLabel.setText("Inscription reussie. Redirection vers la connexion...");
                errorLabel.setText("");

                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(e -> openLogin());
                delay.play();
            } else {
                showError("Cet email existe deja. Veuillez en choisir un autre.");
                resetRegisterButton();
            }
        } catch (SQLException e) {
            showError("Erreur technique : " + e.getMessage());
            resetRegisterButton();
        }
    }

    private Double parseNullableDouble(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return Double.parseDouble(value.trim().replace(',', '.'));
    }

    private void resetRegisterButton() {
        registerButton.setDisable(false);
        registerButton.setText("S'INSCRIRE");
    }

    private void openLogin() {
        try {
            LoginController loginController = new LoginController();
            Scene scene = loginController.getScene();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la connexion");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
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
