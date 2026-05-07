package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.SessionManager;

public class AnalyticsController {
    
    @FXML private LineChart<String, Number> engagementChart;
    @FXML private PieChart contentPieChart;
    @FXML private TableView<SystemActivity> activitiesTable;
    @FXML private TableColumn<SystemActivity, String> colAction;
    @FXML private TableColumn<SystemActivity, String> colUser;
    @FXML private TableColumn<SystemActivity, String> colDate;
    @FXML private TableColumn<SystemActivity, String> colStatus;
    
    @FXML
    public void initialize() {
        setupEngagementChart();
        setupPieChart();
        setupTable();
    }
    
    private void setupEngagementChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Active Users");
        series.getData().add(new XYChart.Data<>("Jan", 8200));
        series.getData().add(new XYChart.Data<>("Feb", 9100));
        series.getData().add(new XYChart.Data<>("Mar", 10500));
        series.getData().add(new XYChart.Data<>("Apr", 11200));
        series.getData().add(new XYChart.Data<>("May", 12482));
        engagementChart.getData().add(series);
    }
    
    private void setupPieChart() {
        contentPieChart.getData().addAll(
            new PieChart.Data("Research", 35),
            new PieChart.Data("Clinical Cases", 25),
            new PieChart.Data("Questions", 20),
            new PieChart.Data("News", 15),
            new PieChart.Data("Other", 5)
        );
    }
    
    private void setupTable() {
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        activitiesTable.getItems().addAll(
            new SystemActivity("New post published", "Dr. Smith", "2024-05-07 10:30", "Success"),
            new SystemActivity("Comment flagged", "Moderator", "2024-05-07 10:15", "Review"),
            new SystemActivity("User registered", "New User", "2024-05-07 09:45", "Success"),
            new SystemActivity("Report submitted", "Patient A", "2024-05-07 09:30", "Pending")
        );
    }
    
    @FXML
    private void showDashboard() {
        loadAdminDashboardTab(0);
    }

    @FXML
    private void showUsers() {
        loadAdminDashboardTab(1);
    }

    @FXML
    private void showForumModeration() {
        loadView("/fxml/forum/ForumModerationView.fxml");
    }

    @FXML
    private void showEvents() {
        loadAdminDashboardTab(3);
    }

    @FXML
    private void showAnalytics() {
        // Already here
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        loadView("/fxml/LoginView.fxml");
    }
    
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) activitiesTable.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/community-feed.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAdminDashboardTab(int tabIndex) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        AdminDashboardController controller = new AdminDashboardController(currentUser);
        Scene scene = controller.getScene();
        if (scene != null) {
            controller.selectTab(tabIndex);
            Stage stage = (Stage) activitiesTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
    
    public static class SystemActivity {
        private String action;
        private String user;
        private String date;
        private String status;
        
        public SystemActivity(String action, String user, String date, String status) {
            this.action = action;
            this.user = user;
            this.date = date;
            this.status = status;
        }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
