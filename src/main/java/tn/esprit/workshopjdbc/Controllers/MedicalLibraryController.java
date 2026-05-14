package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class MedicalLibraryController {
    
    @FXML private TableView<ResourceData> resourcesTable;
    @FXML private TableColumn<ResourceData, String> colTitle;
    @FXML private TableColumn<ResourceData, String> colCategory;
    @FXML private TableColumn<ResourceData, String> colAuthor;
    @FXML private TableColumn<ResourceData, String> colDate;
    @FXML private TableColumn<ResourceData, Integer> colDownloads;
    
    @FXML
    public void initialize() {
        setupTable();
    }
    
    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDownloads.setCellValueFactory(new PropertyValueFactory<>("downloads"));
        
        // Sample data
        resourcesTable.getItems().addAll(
            new ResourceData("Hypertension Management Guidelines 2024", "Clinical Guidelines", "Dr. Sarah Chen", "2024-05-01", 1247),
            new ResourceData("COVID-19 Long-term Effects Study", "Research Papers", "Dr. Michael Ross", "2024-04-28", 892),
            new ResourceData("Pediatric Asthma Treatment Protocol", "Clinical Guidelines", "Dr. Emma Wilson", "2024-04-25", 756),
            new ResourceData("Rare Disease Case Collection", "Case Studies", "Dr. James Martinez", "2024-04-20", 543)
        );
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
        // Already here
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
            Stage stage = (Stage) resourcesTable.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/community-feed.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class ResourceData {
        private String title;
        private String category;
        private String author;
        private String date;
        private int downloads;
        
        public ResourceData(String title, String category, String author, String date, int downloads) {
            this.title = title;
            this.category = category;
            this.author = author;
            this.date = date;
            this.downloads = downloads;
        }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getDownloads() { return downloads; }
        public void setDownloads(int downloads) { this.downloads = downloads; }
    }
}
