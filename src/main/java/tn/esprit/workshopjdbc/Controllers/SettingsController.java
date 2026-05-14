package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SettingsController {
    
    @FXML private BorderPane rootPane;
    
    @FXML
    public void initialize() {
        // Initialize settings view
    }
    
    // Navigation methods
    @FXML
    private void showCommunityFeed() {
        loadView("/fxml/CommunityFeed.fxml");
    }
    
    @FXML
    private void showPatientCare() {
        loadView("/fxml/forum/PatientCareView.fxml");
    }
    
    @FXML
    private void showMedicalLibrary() {
        loadView("/fxml/forum/MedicalLibraryView.fxml");
    }
    
    @FXML
    private void showAnalytics() {
        loadView("/fxml/forum/AnalyticsView.fxml");
    }
    
    @FXML
    private void showSettings() {
        // Already here
    }
    
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/community-feed.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
