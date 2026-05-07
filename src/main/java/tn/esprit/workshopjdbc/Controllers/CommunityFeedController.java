package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.ForumComment;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.Role;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ForumService;
import tn.esprit.workshopjdbc.Services.GroqChatbotService;
import tn.esprit.workshopjdbc.Services.GroqContentModerationService;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.dao.ForumCommentDAO;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

public class CommunityFeedController {

    @FXML private TextField searchField;
    @FXML private Button notificationsBtn;
    @FXML private Button messagesBtn;
    @FXML private Circle userAvatarCircle;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private VBox postsContainer;
    @FXML private VBox contentContainer;

    @FXML private Button dashboardBtn;
    @FXML private Button communityFeedBtn;
    @FXML private Button appointmentsBtn;
    @FXML private Button eventsBtn;
    @FXML private Button usersBtn;
    @FXML private Button moderationBtn;
    @FXML private Button newClinicalEntryBtn;
    @FXML private Button logoutBtn;

    // Chatbot UI elements
    @FXML private VBox chatHistoryBox;
    @FXML private TextField chatInputField;
    @FXML private Button sendChatBtn;
    @FXML private Button clearChatBtn;

    private final ForumService forumService = new ForumService();
    private final ForumCommentDAO commentDAO = new ForumCommentDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        setupUserProfile();
        setupGlobalNavigation();
        setupChatbot();

        if (isAdmin()) {
            Platform.runLater(this::showAnalyticsDashboard);
        } else {
            loadPosts();
        }
    }

    private void setupChatbot() {
        if (sendChatBtn != null && chatInputField != null) {
            sendChatBtn.setOnAction(e -> sendChatMessage());
            chatInputField.setOnKeyPressed(e -> {
                if (e.getCode().toString().equals("ENTER")) {
                    sendChatMessage();
                }
            });
            if (clearChatBtn != null) {
                clearChatBtn.setOnAction(e -> clearChat());
            }
            // Display initial greeting
            addChatMessageToUI("Assistant", "Hello! I'm VitaHealth Assistant. How can I help you today?", true);
        }
    }

    private void sendChatMessage() {
        if (chatInputField == null || chatInputField.getText().isEmpty()) return;

        String userMessage = chatInputField.getText().trim();
        chatInputField.clear();

        // Add user message to chat
        addChatMessageToUI("You", userMessage, false);

        // Check for quick replies
        String quickReply = GroqChatbotService.getQuickReply(userMessage);
        if (quickReply != null) {
            addChatMessageToUI("Assistant", quickReply, true);
            return;
        }

        // Send to chatbot API
        new Thread(() -> {
            GroqChatbotService.ChatbotResponse response = GroqChatbotService.chat(userMessage);
            Platform.runLater(() -> {
                if (response.success) {
                    addChatMessageToUI("Assistant", response.reply, true);
                } else {
                    if (response.error != null && response.error.contains("inappropriate")) {
                        // Show moderation alert
                        showModerationAlert(response.error, response.reply);
                        addChatMessageToUI("Assistant", response.reply, true);
                    } else {
                        addChatMessageToUI("Assistant", "Error: " + response.error, true);
                    }
                }
            });
        }).start();
    }

    private void addChatMessageToUI(String sender, String message, boolean isAssistant) {
        if (chatHistoryBox == null) return;

        VBox messageBox = new VBox(4);
        messageBox.setStyle(isAssistant ?
                "-fx-padding: 10; -fx-background-color: #e8f5e9; -fx-background-radius: 8;" :
                "-fx-padding: 10; -fx-background-color: #e3f2fd; -fx-background-radius: 8;");

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " +
                (isAssistant ? "#2e7d32" : "#1565c0") + ";");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);

        messageBox.getChildren().addAll(senderLabel, messageLabel);
        chatHistoryBox.getChildren().add(messageBox);

        // Auto-scroll to bottom
        if (chatHistoryBox.getParent() instanceof javafx.scene.control.ScrollPane) {
            Platform.runLater(() -> {
                ((javafx.scene.control.ScrollPane) chatHistoryBox.getParent()).setVvalue(1.0);
            });
        }
    }

    private void clearChat() {
        if (chatHistoryBox != null) {
            chatHistoryBox.getChildren().clear();
        }
        GroqChatbotService.resetConversation();
        addChatMessageToUI("Assistant", "Chat cleared. How can I help you?", true);
    }

    private void showModerationAlert(String error, String suggestion) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Content Moderation Alert");
        alert.setHeaderText("Inappropriate Content Detected");
        alert.setContentText(error + "\n\n" + suggestion);
        alert.showAndWait();
    }

    private void setupUserProfile() {
        userNameLabel.setText(currentUser.getFullName());
        userRoleLabel.setText(currentUser.getRole() != null ? currentUser.getRole() : "Medecin");
    }

    private void setupGlobalNavigation() {
        boolean admin = isAdmin();
        boolean doctor = isDoctor();

        if (communityFeedBtn != null) {
            communityFeedBtn.setText(admin ? "Clinical Analytics" : "Forum");
        }
        if (appointmentsBtn != null) {
            appointmentsBtn.setText(doctor ? "Patients et soins" : "Rendez-vous et soins");
        }
        setVisible(usersBtn, admin);
        setVisible(moderationBtn, admin);
        setVisible(newClinicalEntryBtn, !admin);
        setVisible(eventsBtn, !doctor || admin);
    }

    private VBox createPostInputCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("modern-card");

        HBox row = new HBox(12);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle avatar = new Circle(22);
        avatar.getStyleClass().add("user-avatar");

        TextField input = new TextField();
        input.setPromptText("What's on your mind, " + firstName() + "?");
        input.getStyleClass().add("post-input-field");
        input.setPrefWidth(400);
        input.setOnMouseClicked(e -> openCreatePostDialog());
        input.setEditable(false);

        row.getChildren().addAll(avatar, input);

        HBox actions = new HBox(18);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button liveBtn = new Button("Live Video");
        liveBtn.getStyleClass().add("post-action-button");
        liveBtn.setOnAction(e -> showInfo("Live Video - Coming soon!"));

        Button mediaBtn = new Button("Media");
        mediaBtn.getStyleClass().add("post-action-button");
        mediaBtn.setOnAction(e -> openCreatePostDialog());

        Button feelingBtn = new Button("Feeling/Activity");
        feelingBtn.getStyleClass().add("post-action-button");
        feelingBtn.setOnAction(e -> showInfo("Feeling/Activity - Coming soon!"));

        actions.getChildren().addAll(liveBtn, mediaBtn, feelingBtn);
        card.getChildren().addAll(row, actions);
        return card;
    }

    private void loadPosts() {
        if (postsContainer == null) return;

        postsContainer.getChildren().clear();
        postsContainer.getChildren().add(createPostInputCard());

        List<ForumPost> posts = forumService.searchPosts("", null, currentUser);
        for (ForumPost post : posts) {
            postsContainer.getChildren().add(createPostCard(post));
        }
    }

    private VBox createPostCard(ForumPost post) {
        VBox card = new VBox();
        card.getStyleClass().add("post-card");
        card.setSpacing(12);

        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle avatar = new Circle(24);
        avatar.getStyleClass().add("avatar-doctor");

        VBox authorInfo = new VBox(2);
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label authorName = new Label(post.getAuthorName());
        authorName.getStyleClass().add("post-author");

        Label specialtyBadge = new Label("- " + post.getAuthorRole());
        specialtyBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e88e5; -fx-font-weight: normal;");
        nameRow.getChildren().addAll(authorName, specialtyBadge);

        Label timestamp = new Label(formatTime(post.getCreatedAt()) + " - Public");
        timestamp.getStyleClass().add("post-meta");
        authorInfo.getChildren().addAll(nameRow, timestamp);
        header.getChildren().addAll(avatar, authorInfo);

        VBox content = new VBox(8);
        if (post.getTag() != null && !post.getTag().isEmpty()) {
            Label tagLabel = new Label(post.getTag().toUpperCase());
            tagLabel.getStyleClass().add("post-tag");
            content.getChildren().add(tagLabel);
        }

        Label title = new Label(post.getTitle());
        title.getStyleClass().add("post-title");
        title.setWrapText(true);

        Label postContent = new Label(post.getContent());
        postContent.getStyleClass().add("post-content");
        postContent.setWrapText(true);
        content.getChildren().addAll(title, postContent);

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            ImageView imageView = new ImageView();
            try {
                imageView.setImage(new Image(toImageSource(post.getImageUrl())));
                imageView.setFitWidth(600);
                imageView.setFitHeight(350);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("post-image");
                content.getChildren().add(imageView);
            } catch (Exception ignored) {
                // If the local media path is no longer available, the feed still renders.
            }
        }

        HBox interactionBar = new HBox(20);
        interactionBar.getStyleClass().add("interaction-bar");
        interactionBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button likeBtn = new Button("Like " + post.getLikeCount());
        likeBtn.getStyleClass().add("interaction-button");
        likeBtn.setOnAction(e -> handleLike(post));

        Button commentBtn = new Button("Comment " + post.getCommentCount());
        commentBtn.getStyleClass().add("interaction-button");
        commentBtn.setOnAction(e -> toggleCommentSection(card, post));

        Button shareBtn = new Button("Share " + post.getShareCount());
        shareBtn.getStyleClass().add("interaction-button");
        shareBtn.setOnAction(e -> handleShare(post));

        interactionBar.getChildren().addAll(likeBtn, commentBtn, shareBtn);

        // Add edit/delete buttons if user is author or admin
        if (currentUser != null && (currentUser.getId() == post.getAuthorId() || "ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Button editBtn = new Button("✏️ Edit");
            editBtn.getStyleClass().add("interaction-button");
            editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6 12;");
            editBtn.setOnAction(e -> handleEditPost(post));

            Button deleteBtn = new Button("🗑️ Delete");
            deleteBtn.getStyleClass().add("interaction-button");
            deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 6 12;");
            deleteBtn.setOnAction(e -> handleDeletePost(post));

            interactionBar.getChildren().addAll(spacer, editBtn, deleteBtn);
        }

        card.getChildren().addAll(header, content, interactionBar);
        return card;
    }

    private void handleLike(ForumPost post) {
        if (currentUser != null) {
            forumService.toggleLike(currentUser, post);
            loadPosts();
        }
    }

    private void handleShare(ForumPost post) {
        if (currentUser != null) {
            forumService.incrementShareCount(post);
            loadPosts();
            showInfo("Post shared!");
        }
    }

    private void handleEditPost(ForumPost post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreatePostDialog.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Edit Clinical Entry");
            dialog.setScene(new Scene(root, 760, 620));
            dialog.setResizable(false);

            CreatePostDialogController controller = loader.getController();
            controller.setDialog(dialog);
            controller.setCurrentUser(currentUser);
            controller.setEditingPost(post); // Pass the post to edit

            dialog.showAndWait();
            loadPosts(); // Refresh after editing
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open edit dialog");
        }
    }

    private void handleDeletePost(ForumPost post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Post");
        confirm.setHeaderText("Are you sure you want to delete this post?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            if (forumService.deletePostByAuthor(currentUser, post)) {
                showInfo("Post deleted successfully!");
                loadPosts();
            } else {
                showError("Failed to delete post. You may not have permission.");
            }
        }
    }

    @FXML
    private void showCommunityFeed() {
        setActiveNav(communityFeedBtn);
        if (isAdmin()) {
            showAnalyticsDashboard();
            return;
        }
        loadPosts();
    }

    @FXML
    private void showDashboard() {
        setActiveNav(dashboardBtn);
        if (selectEmbeddedDashboardTab(0)) return;
        loadRoleDashboard(0);
    }

    @FXML
    private void showCareSpace() {
        setActiveNav(appointmentsBtn);
        if (selectEmbeddedDashboardTab(isDoctor() ? 1 : 0)) return;
        loadRoleDashboard(isDoctor() ? 1 : 0);
    }

    @FXML
    private void showEvents() {
        setActiveNav(eventsBtn);
        if (isAdmin()) {
            loadRoleDashboard(3);
        } else if (isDoctor()) {
            showInfo("Les ateliers ne sont pas dans l'espace medecin actuel.");
        } else {
            if (selectEmbeddedDashboardTab(4)) return;
            loadRoleDashboard(4);
        }
    }

    @FXML
    private void showUsers() {
        if (!isAdmin()) {
            showInfo("This area is reserved for administrators.");
            return;
        }
        setActiveNav(usersBtn);
        loadRoleDashboard(1);
    }

    @FXML
    private void showForumModeration() {
        if (!isAdmin()) {
            showInfo("This area is reserved for administrators.");
            return;
        }
        setActiveNav(moderationBtn);
        loadFullScene("/fxml/forum/ForumModerationView.fxml");
    }

    @FXML
    private void openCreatePostDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreatePostDialog.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Create Clinical Entry");
            dialog.setScene(new Scene(root, 760, 620));
            dialog.setResizable(false);

            CreatePostDialogController controller = loader.getController();
            controller.setDialog(dialog);
            controller.setCurrentUser(currentUser);

            dialog.showAndWait();
            loadPosts();
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Could not open create post dialog");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("VitaHealth - Login");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRoleDashboard(int targetTab) {
        if (isAdmin()) {
            AdminDashboardController controller = new AdminDashboardController(currentUser);
            Scene scene = controller.getScene();
            if (scene != null) {
                controller.selectTab(targetTab);
                Stage stage = (Stage) contentContainer.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
            return;
        }

        if (isDoctor()) {
            loadDoctorDashboard(targetTab);
        } else {
            loadPatientDashboard(targetTab);
        }
    }

    private void loadPatientDashboard(int targetTab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PatientDashboard.fxml"));
            Parent root = loader.load();
            PatientController controller = loader.getController();
            controller.selectTab(targetTab);
            replaceScene(root);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Could not load patient dashboard.");
        }
    }

    private void loadDoctorDashboard(int targetTab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DoctorDashboard.fxml"));
            Parent root = loader.load();
            DoctorController controller = loader.getController();
            controller.selectTab(targetTab);
            replaceScene(root);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Could not load doctor dashboard.");
        }
    }

    private void showAnalyticsDashboard() {
        loadFullScene("/fxml/forum/AnalyticsView.fxml");
    }

    private void loadFullScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            replaceScene(root);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Could not load scene: " + fxmlPath);
        }
    }

    private void replaceScene(Parent root) {
        Stage stage = (Stage) contentContainer.getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/css/community-feed.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private boolean selectEmbeddedDashboardTab(int index) {
        if (contentContainer == null || contentContainer.getScene() == null) return false;

        TabPane tabPane = findTabPane(contentContainer.getScene().getRoot());
        if (tabPane == null || tabPane.getTabs().isEmpty()) return false;
        if (!isDashboardTabPane(tabPane)) return false;
        if (index < 0 || index >= tabPane.getTabs().size()) return false;

        tabPane.getSelectionModel().select(index);
        return true;
    }

    private boolean isDashboardTabPane(TabPane tabPane) {
        return tabPane.getTabs().stream()
                .map(tab -> tab.getText() == null ? "" : tab.getText().toLowerCase())
                .anyMatch(text -> text.contains("forum"));
    }

    private TabPane findTabPane(Node node) {
        if (node instanceof TabPane tabPane) {
            return tabPane;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                TabPane found = findTabPane(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void setActiveNav(Button activeBtn) {
        Button[] navButtons = {dashboardBtn, communityFeedBtn, appointmentsBtn, eventsBtn, usersBtn, moderationBtn};
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("active-nav");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active-nav");
        }
    }

    private boolean isAdmin() {
        return currentUser != null && Role.ADMIN.equalsIgnoreCase(currentUser.getRole());
    }

    private boolean isDoctor() {
        return currentUser != null && Role.DOCTOR.equalsIgnoreCase(currentUser.getRole());
    }

    private void setVisible(Button button, boolean visible) {
        if (button == null) return;
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private String firstName() {
        if (currentUser == null || currentUser.getFullName() == null || currentUser.getFullName().isBlank()) {
            return "";
        }
        return currentUser.getFullName().split(" ")[0];
    }

    private String formatTime(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(formatter);
    }

    private String toImageSource(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return "";
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("file:")) {
            return imageUrl;
        }
        return new File(imageUrl).toURI().toString();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void toggleCommentSection(VBox card, ForumPost post) {
        // Check if comment section already exists
        VBox existingCommentSection = null;
        for (javafx.scene.Node node : card.getChildren()) {
            if (node.getUserData() != null && "commentSection".equals(node.getUserData())) {
                existingCommentSection = (VBox) node;
                break;
            }
        }
        
        if (existingCommentSection != null) {
            // Toggle visibility
            existingCommentSection.setVisible(!existingCommentSection.isVisible());
            existingCommentSection.setManaged(existingCommentSection.isVisible());
        } else {
            // Create new comment section
            VBox commentSection = createCommentSection(post);
            commentSection.setUserData("commentSection");
            card.getChildren().add(commentSection);
        }
    }
    
    private VBox createCommentSection(ForumPost post) {
        VBox section = new VBox(12);
        section.setStyle("-fx-padding: 12 0 0 0; -fx-border-color: #e4e6eb; -fx-border-width: 1 0 0 0;");
        
        // Comment input area
        HBox inputArea = new HBox(8);
        inputArea.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        inputArea.setStyle("-fx-padding: 8 0;");
        
        Circle avatar = new Circle(18);
        avatar.getStyleClass().add("user-avatar");
        
        TextField commentInput = new TextField();
        commentInput.setPromptText("Write a comment...");
        commentInput.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 18; -fx-padding: 8 12; -fx-font-size: 13px;");
        commentInput.setPrefWidth(450);
        
        Button postCommentBtn = new Button("Post");
        postCommentBtn.setStyle("-fx-background-color: #1877f2; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 16; -fx-background-radius: 6; -fx-cursor: hand;");
        postCommentBtn.setOnAction(e -> {
            String content = commentInput.getText().trim();
            if (!content.isEmpty()) {
                postComment(post, content, section);
                commentInput.clear();
            }
        });
        
        // Allow Enter key to post comment
        commentInput.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                String content = commentInput.getText().trim();
                if (!content.isEmpty()) {
                    postComment(post, content, section);
                    commentInput.clear();
                }
            }
        });
        
        inputArea.getChildren().addAll(avatar, commentInput, postCommentBtn);
        
        // Comments list
        VBox commentsList = new VBox(8);
        commentsList.setStyle("-fx-padding: 8 0;");
        loadCommentsForPost(post, commentsList);
        
        section.getChildren().addAll(inputArea, commentsList);
        return section;
    }
    
    private void loadCommentsForPost(ForumPost post, VBox commentsList) {
        commentsList.getChildren().clear();
        List<ForumComment> comments = commentDAO.findByPost(post.getId());
        
        if (comments.isEmpty()) {
            Label noComments = new Label("No comments yet. Be the first to comment!");
            noComments.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px; -fx-padding: 8;");
            commentsList.getChildren().add(noComments);
        } else {
            for (ForumComment comment : comments) {
                commentsList.getChildren().add(createCommentCard(comment, post, commentsList));
            }
        }
    }
    
    private HBox createCommentCard(ForumComment comment, ForumPost post, VBox commentsList) {
        HBox card = new HBox(8);
        card.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        card.setStyle("-fx-padding: 8;");
        
        Circle avatar = new Circle(16);
        avatar.getStyleClass().add("avatar-doctor");
        
        VBox content = new VBox(4);
        content.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 12; -fx-padding: 8 12;");
        
        HBox nameAndRole = new HBox(6);
        nameAndRole.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label authorName = new Label(comment.getAuthorName());
        authorName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #050505;");
        
        Label roleLabel = new Label("• " + comment.getAuthorRole());
        roleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e88e5;");
        
        nameAndRole.getChildren().addAll(authorName, roleLabel);
        
        Label commentText = new Label(comment.getContent());
        commentText.setStyle("-fx-font-size: 13px; -fx-text-fill: #050505;");
        commentText.setWrapText(true);
        commentText.setMaxWidth(500);
        
        content.getChildren().addAll(nameAndRole, commentText);
        
        VBox actions = new VBox(4);
        HBox actionButtons = new HBox(12);
        actionButtons.setStyle("-fx-padding: 4 0 0 12;");
        
        Label likeBtn = new Label("👍 Like (" + comment.getLikeCount() + ")");
        likeBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #65676b; -fx-cursor: hand;");
        likeBtn.setOnMouseClicked(e -> {
            commentDAO.toggleLikeComment(comment.getId(), currentUser.getId());
            loadCommentsForPost(post, commentsList);
        });
        
        Label timeLabel = new Label(formatTimeAgo(comment.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #65676b;");
        
        actionButtons.getChildren().addAll(likeBtn, timeLabel);
        
        // Add delete button if user is author or admin
        if (currentUser.getId() == comment.getAuthorId() || "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            Label deleteBtn = new Label("🗑️ Delete");
            deleteBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
            deleteBtn.setOnMouseClicked(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Comment");
                confirm.setHeaderText("Are you sure you want to delete this comment?");
                confirm.setContentText("This action cannot be undone.");
                
                if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
                    if (commentDAO.delete(comment.getId())) {
                        loadCommentsForPost(post, commentsList);
                        loadPosts(); // Refresh to update comment count
                    }
                }
            });
            actionButtons.getChildren().add(deleteBtn);
        }
        
        actions.getChildren().add(actionButtons);
        
        VBox contentWrapper = new VBox(2);
        contentWrapper.getChildren().addAll(content, actions);
        
        card.getChildren().addAll(avatar, contentWrapper);
        return card;
    }
    
    private void postComment(ForumPost post, String content, VBox section) {
        ForumComment comment = new ForumComment();
        comment.setPostId(post.getId());
        comment.setAuthorId(currentUser.getId());
        comment.setContent(content);
        comment.setStatus("PUBLISHED");
        
        try {
            if (commentDAO.create(comment)) {
                // Reload comments
                VBox commentsList = (VBox) section.getChildren().get(1);
                loadCommentsForPost(post, commentsList);
                // Refresh posts to update comment count
                loadPosts();
            } else {
                showError("Failed to post comment. Please try again.");
            }
        } catch (Exception e) {
            // Check if it's a moderation error
            if (e.getMessage() != null && e.getMessage().contains("Inappropriate")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Inappropriate Content");
                alert.setHeaderText("Your comment contains inappropriate content");
                alert.setContentText(e.getMessage() + "\n\nPlease remove any offensive language and try again.");
                alert.showAndWait();
            } else {
                showError("Failed to post comment: " + e.getMessage());
            }
        }
    }
    
    private String formatTimeAgo(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        Duration duration = Duration.between(dateTime, java.time.LocalDateTime.now());
        long seconds = duration.getSeconds();
        
        if (seconds < 60) return "Just now";
        if (seconds < 3600) return (seconds / 60) + "m ago";
        if (seconds < 86400) return (seconds / 3600) + "h ago";
        if (seconds < 604800) return (seconds / 86400) + "d ago";
        
        return dateTime.format(formatter);
    }
}
