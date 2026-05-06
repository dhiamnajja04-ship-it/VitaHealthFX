package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.workshopjdbc.Utils.SessionManager; // Use your existing session manager
import tn.esprit.workshopjdbc.Entities.User;
import java.io.IOException;

public class MainDashboardController {

    @FXML private BorderPane mainPane;
    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;
    @FXML private Label userNameLabel;
    @FXML private Button adminPanelBtn;

    @FXML
    public void initialize() {
        // Use the SessionManager we established earlier
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            
            // Match the role logic from your LoginController
            boolean isAdmin = "ADMIN".equals(user.getRole());
            if (adminPanelBtn != null) {
                adminPanelBtn.setVisible(isAdmin);
            }
        }
        showUserDashboard(); // Load default view
    }

    // --- NAVIGATION LOGIC ---

    private void loadInterface(String fxmlPath) {
        try {
            // IMPORTANT: If your FXMLs are in src/main/resources/fxml/, use /fxml/path
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            System.out.println("DEBUG: Successfully loaded " + fxmlPath);
        } catch (IOException e) {
            System.err.println("FATAL: Could not load " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void showUserDashboard() {
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        // Put your actual path here
        loadInterface("/fxml/AdminDashboard.fxml"); 
    }

    @FXML
    private void showUserEventView() {
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        loadInterface("/fxml/UserEventView.fxml");
    }

    @FXML
    private void toggleAdminMode() {
        boolean isVisible = sidebar.isVisible();
        sidebar.setVisible(!isVisible);
        sidebar.setManaged(!isVisible);
        if (!isVisible) showAdminEvents();
    }

    @FXML
    private void showAdminEvents() {
        // Matches the "Manage Events" button in your sidebar
        loadInterface("/fxml/AdminEventView.fxml"); 
    }

    @FXML
    private void showAdminParticipations() {
        // Matches the "Participations" button in your sidebar
        loadInterface("/fxml/ParticipationLogs.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        // Use your login redirect logic here (System.exit(0) is a bit harsh!)
        System.exit(0); 
    }
}