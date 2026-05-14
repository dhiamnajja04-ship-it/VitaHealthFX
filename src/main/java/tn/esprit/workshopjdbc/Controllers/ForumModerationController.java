package tn.esprit.workshopjdbc.Controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ForumService;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.dao.ForumCategoryDAO;
import tn.esprit.workshopjdbc.dao.ForumCommentDAO;
import tn.esprit.workshopjdbc.dao.ForumPostDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumModerationController {
    @FXML private TableView<ForumPost> moderationTable;
    @FXML private TableColumn<ForumPost, String> colTitle;
    @FXML private TableColumn<ForumPost, String> colCategory;
    @FXML private TableColumn<ForumPost, String> colAuthor;
    @FXML private TableColumn<ForumPost, String> colStatus;
    @FXML private TableColumn<ForumPost, Number> colReports;
    @FXML private TableColumn<ForumPost, Number> colComments;
    @FXML private TableColumn<ForumPost, String> colDate;
    @FXML private TextArea contentArea;
    @FXML private Label metaLabel;
    @FXML private Button publishBtn;
    @FXML private Button hideBtn;
    @FXML private Button lockBtn;
    @FXML private Button deleteBtn;
    @FXML private Button editBtn;
    @FXML private Button refreshBtn;
    @FXML private Label messageLabel;
    @FXML private ComboBox<String> filterCombo;
    
    // Stats labels
    @FXML private Label totalPostsLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label pendingReviewsLabel;
    @FXML private Label reportedPostsLabel;
    
    // Charts
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> activityBarChart;

    private final ForumService forumService = new ForumService();
    private final ForumPostDAO postDAO = new ForumPostDAO();
    private final ForumCommentDAO commentDAO = new ForumCommentDAO();
    private final ForumCategoryDAO categoryDAO = new ForumCategoryDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupTable();
        setupActions();
        setupFilter();
        loadStats();
        loadCharts();
        loadAllPosts();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        colAuthor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthorName()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colReports.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getReportCount()));
        
        if (colComments != null) {
            colComments.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCommentCount()));
        }
        
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() == null ? "" : data.getValue().getCreatedAt().format(formatter)
        ));

        // Custom cell factory for status column to render as styled badge
        colStatus.setCellFactory(col -> new TableCell<ForumPost, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(status);
                    // Set styles based on status
                    String styleClass = switch (status) {
                        case "PUBLISHED" -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-background-radius: 12;";
                        case "PENDING_REVIEW" -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 12;";
                        case "HIDDEN" -> "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-background-radius: 12;";
                        case "LOCKED" -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 12;";
                        default -> "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 12;";
                    };
                    setStyle(styleClass + " -fx-padding: 6 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                }
            }
        });

        moderationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, post) -> showPost(post));
    }

    private void setupActions() {
        refreshBtn.setOnAction(e -> {
            loadStats();
            loadCharts();
            loadAllPosts();
        });
        publishBtn.setOnAction(e -> moderate("PUBLISH"));
        hideBtn.setOnAction(e -> moderate("HIDE"));
        lockBtn.setOnAction(e -> moderate("LOCK"));
        deleteBtn.setOnAction(e -> moderate("DELETE"));
        
        if (editBtn != null) {
            editBtn.setOnAction(e -> editPost());
        }
    }
    
    private void setupFilter() {
        if (filterCombo != null) {
            filterCombo.setItems(FXCollections.observableArrayList(
                "All Posts", "Published", "Pending Review", "Hidden", "Locked", "Reported"
            ));
            filterCombo.setValue("All Posts");
            filterCombo.setOnAction(e -> filterPosts());
        }
    }
    
    private void filterPosts() {
        String filter = filterCombo.getValue();
        List<ForumPost> allPosts = postDAO.search("", null, true);
        
        List<ForumPost> filtered = switch (filter) {
            case "Published" -> allPosts.stream().filter(p -> "PUBLISHED".equals(p.getStatus())).toList();
            case "Pending Review" -> allPosts.stream().filter(p -> "PENDING_REVIEW".equals(p.getStatus())).toList();
            case "Hidden" -> allPosts.stream().filter(p -> "HIDDEN".equals(p.getStatus())).toList();
            case "Locked" -> allPosts.stream().filter(p -> "LOCKED".equals(p.getStatus())).toList();
            case "Reported" -> allPosts.stream().filter(p -> p.getReportCount() > 0).toList();
            default -> allPosts;
        };
        
        moderationTable.setItems(FXCollections.observableArrayList(filtered));
        messageLabel.setText(filtered.size() + " post(s) found.");
    }
    
    private void loadStats() {
        // Get all posts
        List<ForumPost> allPosts = postDAO.search("", null, true);
        
        // Calculate stats
        int totalPosts = allPosts.size();
        int pendingReviews = (int) allPosts.stream().filter(p -> "PENDING_REVIEW".equals(p.getStatus())).count();
        int reportedPosts = (int) allPosts.stream().filter(p -> p.getReportCount() > 0).count();
        
        // Count total comments across all posts
        int totalComments = allPosts.stream().mapToInt(ForumPost::getCommentCount).sum();
        
        // Update labels
        if (totalPostsLabel != null) totalPostsLabel.setText(String.valueOf(totalPosts));
        if (totalCommentsLabel != null) totalCommentsLabel.setText(String.valueOf(totalComments));
        if (pendingReviewsLabel != null) pendingReviewsLabel.setText(String.valueOf(pendingReviews));
        if (reportedPostsLabel != null) reportedPostsLabel.setText(String.valueOf(reportedPosts));
    }
    
    private void loadCharts() {
        loadStatusPieChart();
        loadActivityBarChart();
    }
    
    private void loadStatusPieChart() {
        if (statusPieChart == null) return;
        
        List<ForumPost> allPosts = postDAO.search("", null, true);
        
        // Count posts by status
        Map<String, Integer> statusCounts = new HashMap<>();
        for (ForumPost post : allPosts) {
            String status = post.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        
        // Create pie chart data
        statusPieChart.getData().clear();
        statusCounts.forEach((status, count) -> {
            PieChart.Data slice = new PieChart.Data(status + " (" + count + ")", count);
            statusPieChart.getData().add(slice);
        });
        
        // Style the chart
        statusPieChart.setLegendVisible(true);
        statusPieChart.setLabelsVisible(true);
    }
    
    private void loadActivityBarChart() {
        if (activityBarChart == null) return;
        
        List<ForumPost> allPosts = postDAO.search("", null, true);
        
        // Count posts and comments by category
        Map<String, Integer> postsByCategory = new HashMap<>();
        Map<String, Integer> commentsByCategory = new HashMap<>();
        
        for (ForumPost post : allPosts) {
            String category = post.getCategoryName();
            postsByCategory.put(category, postsByCategory.getOrDefault(category, 0) + 1);
            commentsByCategory.put(category, commentsByCategory.getOrDefault(category, 0) + post.getCommentCount());
        }
        
        // Create bar chart series
        XYChart.Series<String, Number> postsSeries = new XYChart.Series<>();
        postsSeries.setName("Posts");
        
        XYChart.Series<String, Number> commentsSeries = new XYChart.Series<>();
        commentsSeries.setName("Comments");
        
        // Add data to series
        postsByCategory.forEach((category, count) -> {
            postsSeries.getData().add(new XYChart.Data<>(category, count));
            commentsSeries.getData().add(new XYChart.Data<>(category, commentsByCategory.getOrDefault(category, 0)));
        });
        
        // Update chart
        activityBarChart.getData().clear();
        activityBarChart.getData().addAll(postsSeries, commentsSeries);
        activityBarChart.setLegendVisible(true);
    }

    private void loadAllPosts() {
        List<ForumPost> posts = postDAO.search("", null, true);
        moderationTable.setItems(FXCollections.observableArrayList(posts));
        messageLabel.setText(posts.size() + " post(s) in total.");
        if (!posts.isEmpty()) {
            moderationTable.getSelectionModel().selectFirst();
        } else {
            contentArea.clear();
            metaLabel.setText("");
        }
    }

    private void showPost(ForumPost post) {
        if (post == null) {
            contentArea.clear();
            metaLabel.setText("");
            return;
        }

        contentArea.setText(post.getContent());
        metaLabel.setText("Title: " + post.getTitle() + "\n" +
                "Category: " + post.getCategoryName() + " | Author: " + post.getAuthorName() + " (" + post.getAuthorRole() + ")\n" +
                "Reports: " + post.getReportCount() + " | Comments: " + post.getCommentCount() + 
                " | Likes: " + post.getLikeCount() + " | Status: " + post.getStatus());
    }
    
    private void editPost() {
        ForumPost post = moderationTable.getSelectionModel().getSelectedItem();
        if (post == null) {
            messageLabel.setText("Please select a post to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreatePostDialog.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Post");
            dialog.setScene(new Scene(root, 760, 620));
            dialog.setResizable(false);

            CreatePostDialogController controller = loader.getController();
            controller.setDialog(dialog);
            controller.setCurrentUser(currentUser);
            controller.setEditingPost(post);

            dialog.showAndWait();
            loadStats();
            loadCharts();
            loadAllPosts();
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Could not open edit dialog");
        }
    }

    private void moderate(String action) {
        ForumPost post = moderationTable.getSelectionModel().getSelectedItem();
        if (post == null) {
            messageLabel.setText("Please select a post.");
            return;
        }

        boolean success = switch (action) {
            case "PUBLISH" -> forumService.publishPost(currentUser, post);
            case "HIDE" -> forumService.hidePost(currentUser, post);
            case "LOCK" -> forumService.lockPost(currentUser, post);
            case "DELETE" -> confirmDelete(post) && forumService.deletePost(currentUser, post);
            default -> false;
        };

        messageLabel.setText(success ? "Action applied successfully." : "Action denied or failed.");
        loadStats();
        loadCharts();
        loadAllPosts();
    }

    private boolean confirmDelete(ForumPost post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Post");
        alert.setHeaderText("Are you sure you want to delete this post?");
        alert.setContentText("Title: " + post.getTitle() + "\nThis action cannot be undone.");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}