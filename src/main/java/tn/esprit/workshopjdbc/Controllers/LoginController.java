package tn.esprit.workshopjdbc.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ServiceVitaHealth;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.Utils.ThemeManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    private final ServiceVitaHealth service = new ServiceVitaHealth();
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    public Scene getScene() {
        HBox root = new HBox();
        root.getStyleClass().add("auth-shell");

        VBox brandPanel = new VBox(18);
        brandPanel.setAlignment(Pos.CENTER_LEFT);
        brandPanel.setPrefWidth(520);
        brandPanel.getStyleClass().add("auth-brand-panel");

        Label brand = new Label("VitaHealthFX");
        brand.getStyleClass().add("auth-brand-title");
        Label offer = new Label("Plateforme medicale desktop pour patients, medecins et administration.");
        offer.setWrapText(true);
        offer.getStyleClass().add("auth-brand-copy");
        Label bullets = new Label("Rendez-vous | Prescriptions | Forum sante | Analytics");
        bullets.setWrapText(true);
        bullets.getStyleClass().add("auth-brand-meta");
        brandPanel.getChildren().addAll(brand, offer, bullets);

        VBox rightPane = new VBox(18);
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setFillWidth(false);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        Button themeBtn = new Button(ThemeManager.toggleText());
        themeBtn.getStyleClass().add("theme-toggle");

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(420);
        card.getStyleClass().add("auth-card");

        Text title = new Text("Connexion");
        title.getStyleClass().add("auth-title");

        Label subtitle = new Label("Accedez a votre espace VitaHealth");
        subtitle.getStyleClass().add("auth-subtitle");

        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("auth-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.getStyleClass().add("auth-field");
        passwordField.setOnAction(e -> handleLogin());

        messageLabel = new Label();
        messageLabel.getStyleClass().add("auth-message");

        Button loginBtn = new Button("Se connecter");
        loginBtn.getStyleClass().add("auth-primary-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        Button forgotPasswordBtn = new Button("Mot de passe oublie ?");
        forgotPasswordBtn.getStyleClass().add("auth-link-button");
        forgotPasswordBtn.setOnAction(e -> handleForgotPassword());

        Hyperlink registerLink = new Hyperlink("Creer un compte");
        registerLink.getStyleClass().add("auth-register-link");
        registerLink.setOnAction(e -> openRegister());

        HBox links = new HBox(14, forgotPasswordBtn, registerLink);
        links.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, subtitle, emailField, passwordField, messageLabel, loginBtn, links);
        rightPane.getChildren().addAll(themeBtn, card);
        root.getChildren().addAll(brandPanel, rightPane);

        Scene scene = new Scene(root, 1200, 800);
        ThemeManager.apply(scene);
        themeBtn.setOnAction(e -> {
            ThemeManager.toggle(scene);
            themeBtn.setText(ThemeManager.toggleText());
        });
        animate(card);
        animate(brandPanel);
        return scene;
    }

    private void animate(Region node) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(420), node);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(420), node);
        slide.setFromY(18);
        slide.setToY(0);
        fade.play();
        slide.play();
    }

    @FXML
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
                messageLabel.getStyleClass().remove("auth-error");
                messageLabel.getStyleClass().add("auth-success");
                messageLabel.setText("Connexion reussie. Redirection...");

                PauseTransition delay = new PauseTransition(Duration.seconds(0.6));
                delay.setOnFinished(e -> redirectByRole(user));
                delay.play();
            } else {
                showError("Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            showError("Erreur de connexion : " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForgotPasswordView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            ThemeManager.apply(scene);
            stage.setScene(scene);
            stage.setTitle("VitaHealth - Mot de passe oublie");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la page de recuperation");
        }
    }

    private void redirectByRole(User user) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            switch (user.getRole()) {
                case "ADMIN" -> {
                    AdminDashboardController adminController = new AdminDashboardController(user);
                    Scene adminScene = adminController.getScene();
                    if (adminScene == null) {
                        showError("Impossible de generer la vue Admin.");
                        return;
                    }
                    ThemeManager.applyModern(adminScene);
                    stage.setScene(adminScene);
                    stage.setTitle("VitaHealth - Admin Dashboard");
                }
                case "DOCTOR" -> {
                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/DoctorDashboard.fxml"));
                    Scene scene = new Scene(root, 1200, 800);
                    ThemeManager.apply(scene);
                    stage.setScene(scene);
                    stage.setTitle("Espace Medecin");
                }
                case "PATIENT" -> {
                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/PatientDashboard.fxml"));
                    Scene scene = new Scene(root, 1200, 800);
                    ThemeManager.apply(scene);
                    stage.setScene(scene);
                    stage.setTitle("Espace Patient");
                }
                default -> {
                    showError("Role inconnu : " + user.getRole());
                    return;
                }
            }
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la redirection : " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        openRegister();
    }

    private void openRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));
            Scene scene = new Scene(root, 1200, 800);
            ThemeManager.apply(scene);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la page d'inscription");
        }
    }

    private void showError(String message) {
        messageLabel.getStyleClass().remove("auth-success");
        if (!messageLabel.getStyleClass().contains("auth-error")) {
            messageLabel.getStyleClass().add("auth-error");
        }
        messageLabel.setText(message);
    }
}
