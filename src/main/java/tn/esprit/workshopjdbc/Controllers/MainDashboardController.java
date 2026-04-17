package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.UserSession;
import java.io.IOException;

public class MainDashboardController {

    @FXML private BorderPane mainPane; // Matches fx:id="mainPane"
    @FXML private StackPane contentArea; // Matches fx:id="contentArea"
    @FXML private VBox sidebar; // Matches fx:id="sidebar"
    @FXML private Label userNameLabel; // Matches fx:id="userNameLabel"
    @FXML private Button adminPanelBtn; // Matches fx:id="adminPanelBtn"

    @FXML
    public void initialize() {
        User user = UserSession.getSession();
        if (user != null) {
            // Set name with a fallback just in case
            String name = (user.getFirstName() != null) ? user.getFirstName() : "User";
            userNameLabel.setText(name);

            // RBAC: Only show Admin Toggle if user has ROLE_ADMIN
            boolean isAdmin = user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN");
            if (adminPanelBtn != null) {
                adminPanelBtn.setVisible(isAdmin);
                adminPanelBtn.setManaged(isAdmin);
            }
        }

        // Start on the clean User Dashboard by default
        showUserDashboard();
    }

    // --- USER SIDE NAVIGATION (NAVBAR) ---

    @FXML
    private void showUserDashboard() {
        hideSidebar();
        loadInterface("/UserDashboard.fxml");
    }

    @FXML
    private void showUserEventView() {
        hideSidebar();
        loadInterface("/UserEventView.fxml");
    }

    // --- ADMIN SIDE NAVIGATION (SIDEBAR) ---

    @FXML
    private void toggleAdminMode() {
        showSidebar();
        showAdminEvents(); // Lands on Event Management by default
    }

    @FXML
    private void showAdminEvents() {
        loadInterface("/AdminEventDashboard.fxml");
    }

    @FXML
    private void showAdminParticipations() {
        loadInterface("/AdminParticipation.fxml");
    }

    @FXML
    private void showUsers() {
        loadInterface("/AdminUserManagement.fxml");
    }

    // --- CORE LOGIC HELPERS ---

    private void showSidebar() {
        if (sidebar != null) {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
            // Ensure mainPane knows to show the left component
            mainPane.setLeft(sidebar);
        }
    }

    private void hideSidebar() {
        if (sidebar != null) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
            // Remove from layout entirely for full-width user views
            mainPane.setLeft(null);
        }
    }

    private void loadInterface(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            System.out.println("Successfully loaded: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Could not load FXML at: " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("FXML Path is null or file not found: " + fxmlPath);
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.clearSession();
        // Closes app for now, or redirect to login
        System.exit(0);
    }
}