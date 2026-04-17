package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Services.AuthService;
import tn.esprit.workshopjdbc.Utils.UserSession;
import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        AuthService auth = new AuthService();
        if (auth.login(emailField.getText(), passwordField.getText())) {
            // Check role for redirection
            String role = UserSession.getSession().getRoles().get(0);
            if (role.contains("ADMIN")) {
                System.out.println("LOGGED AS ADMIN: Redirecting to Admin Panel...");
                // You can add an Admin FXML here later
            } else {
                navigateTo("/EventInterface.fxml");
            }
        } else {
            System.err.println("LOGIN FAILED: Verify DB records!");
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
}