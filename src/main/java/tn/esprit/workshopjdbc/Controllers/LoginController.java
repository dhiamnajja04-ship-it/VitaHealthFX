package tn.esprit.workshopjdbc.Controllers;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import tn.esprit.workshopjdbc.Services.ServiceVitaHealth;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Entities.Role;
import com.vitahealth.App;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    private final ServiceVitaHealth service = new ServiceVitaHealth();
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

        Text title = new Text("VITAHEALTH");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: #2c3e66;");

        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-padding: 12; -fx-background-radius: 10;");

        passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setStyle("-fx-padding: 12; -fx-background-radius: 10;");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");

        Button loginBtn = new Button("SE CONNECTER");
        loginBtn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12; -fx-background-radius: 25; -fx-cursor: hand;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        Hyperlink registerLink = new Hyperlink("Pas encore de compte ? S'inscrire");
        registerLink.setStyle("-fx-text-fill: #2c3e66; -fx-font-size: 12px;");
        registerLink.setOnAction(e -> openRegister());

        passwordField.setOnAction(e -> handleLogin());

        card.getChildren().addAll(title, emailField, passwordField, messageLabel, loginBtn, registerLink);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        return scene;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            User user = service.login(email, password);
            if (user != null) {
                SessionManager.getInstance().setCurrentUser(user);

                messageLabel.setStyle("-fx-text-fill: #28a745;");
                messageLabel.setText("Connexion reussie ! Redirection...");

                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(e -> redirectByRole(user));
                delay.play();
            } else {
                showError("Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            showError("Erreur de connexion : " + e.getMessage());
        }
    }

    private void redirectByRole(User user) {
        try {
            switch (user.getRole()) {
                case Role.ADMIN:
                    AdminDashboardController adminController = new AdminDashboardController(user);
                    Scene adminScene = adminController.getScene();

                    if (adminScene != null) {
                        // Get the current stage from the emailField
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        stage.setScene(adminScene);
                        stage.setTitle("VitaHealth - Admin Dashboard");
                        stage.centerOnScreen();
                        stage.show();
                        System.out.println("DEBUG: Admin Redirection successful.");
                    } else {
                        showError("Erreur: Impossible de générer la vue Admin.");
                    }
                    break;
                case Role.MEDECIN:
                    Parent medecinRoot = FXMLLoader.load(getClass().getResource("/fxml/MedecinView.fxml"));
                    Scene medecinScene = new Scene(medecinRoot, 1200, 800);
                    medecinScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                    Stage medecinStage = (Stage) emailField.getScene().getWindow();
                    medecinStage.setScene(medecinScene);
                    medecinStage.setTitle("Espace Medecin");
                    break;
                case Role.PATIENT:
                    Parent patientRoot = FXMLLoader.load(getClass().getResource("/fxml/PatientDashboard.fxml"));
                    Scene patientScene = new Scene(patientRoot, 1200, 800);
                    patientScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                    Stage patientStage = (Stage) emailField.getScene().getWindow();
                    patientStage.setScene(patientScene);
                    patientStage.setTitle("Espace Patient");
                    break;
                default:
                    showError("Role inconnu : " + user.getRole());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la redirection : " + e.getMessage());
        }
    }

    private void openRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la page d'inscription");
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #dc3545;");
        messageLabel.setText(message);
    }
}