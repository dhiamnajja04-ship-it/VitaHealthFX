package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class PatientCareController {
    
    @FXML private TableView<PatientData> patientsTable;
    @FXML private TableColumn<PatientData, String> colPatientName;
    @FXML private TableColumn<PatientData, String> colCondition;
    @FXML private TableColumn<PatientData, String> colStatus;
    @FXML private TableColumn<PatientData, String> colLastVisit;
    @FXML private TableColumn<PatientData, String> colDoctor;
    @FXML private TableColumn<PatientData, String> colActions;
    
    @FXML
    public void initialize() {
        setupTable();
    }
    
    private void setupTable() {
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLastVisit.setCellValueFactory(new PropertyValueFactory<>("lastVisit"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        colActions.setCellValueFactory(new PropertyValueFactory<>("actions"));
        
        patientsTable.getItems().addAll(
            new PatientData("Ahmed Ben Salah", "Hypertension", "Stable", "2024-05-01", "Dr. Fatma Tounsi", "View"),
            new PatientData("Fatima Zohra", "Diabetes Type 2", "Monitoring", "2024-04-28", "Dr. Fatma Tounsi", "View"),
            new PatientData("Mohamed Ali", "Post Surgery", "Recovering", "2024-05-05", "Dr. Karim Ben", "View"),
            new PatientData("Leila Trabelsi", "Asthma", "Stable", "2024-04-15", "Dr. Fatma Tounsi", "View"),
            new PatientData("Samir Gharbi", "Heart Disease", "Critical", "2024-05-06", "Dr. Karim Ben", "View")
        );
    }
    
    @FXML
    private void showCommunityFeed() {
        loadView("/fxml/CommunityFeed.fxml");
    }
    
    @FXML
    private void showPatientCare() {
        // Already here
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
        loadView("/fxml/forum/SettingsView.fxml");
    }
    
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) patientsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/community-feed.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class PatientData {
        private String name;
        private String condition;
        private String status;
        private String lastVisit;
        private String doctor;
        private String actions;
        
        public PatientData(String name, String condition, String status, String lastVisit, String doctor, String actions) {
            this.name = name;
            this.condition = condition;
            this.status = status;
            this.lastVisit = lastVisit;
            this.doctor = doctor;
            this.actions = actions;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLastVisit() { return lastVisit; }
        public void setLastVisit(String lastVisit) { this.lastVisit = lastVisit; }
        public String getDoctor() { return doctor; }
        public void setDoctor(String doctor) { this.doctor = doctor; }
        public String getActions() { return actions; }
        public void setActions(String actions) { this.actions = actions; }
    }
}
