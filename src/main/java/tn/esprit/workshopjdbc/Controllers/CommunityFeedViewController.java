package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import tn.esprit.workshopjdbc.Entities.ForumComment;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Entities.Story;
import tn.esprit.workshopjdbc.Services.ForumService;
import tn.esprit.workshopjdbc.Services.TranslationService;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.dao.ForumCommentDAO;
import tn.esprit.workshopjdbc.dao.StoryDAO;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

public class CommunityFeedViewController {
    
    @FXML private Circle postAvatarCircle;
    @FXML private TextField newPostInput;
    @FXML private VBox postsContainer;
    @FXML private HBox storiesHBox;
    
    private final ForumService forumService = new ForumService();
    private final ForumCommentDAO commentDAO = new ForumCommentDAO();
    private final StoryDAO storyDAO = new StoryDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private User currentUser;
    
    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadStories();
            loadPosts();
        }
    }
    
    @FXML
    private void openCreatePostDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreatePostDialog.fxml"));
            Parent root = loader.load();
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Create Clinical Entry");
            dialog.setScene(new Scene(root, 700, 600));
            dialog.setResizable(false);
            
            CreatePostDialogController controller = loader.getController();
            controller.setDialog(dialog);
            controller.setCurrentUser(currentUser);
            
            dialog.showAndWait();
            
            // Refresh posts after dialog closes
            loadPosts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadStories() {
        if (storiesHBox == null) return;
        
        storiesHBox.getChildren().clear();
        
        // Add "Create Story" button first
        VBox createStoryBox = new VBox(8);
        createStoryBox.setAlignment(javafx.geometry.Pos.CENTER);
        createStoryBox.setStyle("-fx-padding: 10;");
        createStoryBox.setOnMouseClicked(e -> openCreateStoryDialog());
        
        StackPane storyStack = new StackPane();
        Circle outerCircle = new Circle(40);
        outerCircle.setStyle("-fx-fill: linear-gradient(#f09433, #e6683c, #dc2743, #cc2366, #bc1888);");
        
        Circle innerCircle = new Circle(36);
        innerCircle.setStyle("-fx-fill: #ffffff;");
        
        Label plusLabel = new Label("+");
        plusLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #1e88e5; -fx-font-weight: bold;");
        
        storyStack.getChildren().addAll(outerCircle, innerCircle, plusLabel);
        
        Label createLabel = new Label("Create Story");
        createLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #65676b; -fx-font-weight: bold;");
        
        createStoryBox.getChildren().addAll(storyStack, createLabel);
        createStoryBox.setCursor(javafx.scene.Cursor.HAND);
        storiesHBox.getChildren().add(createStoryBox);
        
        // Load active stories
        List<Story> stories = storyDAO.findActiveStories();
        for (Story story : stories) {
            storiesHBox.getChildren().add(createStoryCard(story));
        }
    }
    
    private VBox createStoryCard(Story story) {
        VBox card = new VBox(8);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-padding: 10; -fx-cursor: hand;");
        
        StackPane storyStack = new StackPane();
        Circle outerCircle = new Circle(40);
        outerCircle.setStyle("-fx-fill: linear-gradient(#667eea, #764ba2);");
        
        Circle innerCircle = new Circle(36);
        try {
            Image img = new Image(toImageSource(story.getImageUrl()), 72, 72, true, true);
            javafx.scene.paint.ImagePattern pattern = new javafx.scene.paint.ImagePattern(img);
            innerCircle.setFill(pattern);
        } catch (Exception e) {
            innerCircle.setStyle("-fx-fill: #e3f2fd;");
        }
        
        storyStack.getChildren().addAll(outerCircle, innerCircle);
        
        Label userName = new Label(story.getUserName());
        userName.setStyle("-fx-font-size: 12px; -fx-text-fill: #65676b;");
        
        card.getChildren().addAll(storyStack, userName);
        card.setOnMouseClicked(e -> showStory(story));
        
        return card;
    }
    
    private void openCreateStoryDialog() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Story Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null) {
                showStoryCaption(selectedFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showStoryCaption(File imageFile) {
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Create Story");
        dialog.setHeaderText("Add a caption (optional)");
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");
        
        TextArea captionField = new TextArea();
        captionField.setPromptText("What's on your mind?");
        captionField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        captionField.setPrefHeight(100);
        captionField.setWrapText(true);
        captionField.setPrefRowCount(3);
        
        content.getChildren().add(captionField);
        dialog.getDialogPane().setContent(content);
        
        dialog.getDialogPane().getButtonTypes().addAll(
            javafx.scene.control.ButtonType.OK,
            javafx.scene.control.ButtonType.CANCEL
        );
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                return captionField.getText();
            }
            return null;
        });
        
        var result = dialog.showAndWait();
        if (result.isPresent()) {
            saveStory(imageFile, result.get());
        }
    }
    
    private void saveStory(File imageFile, String caption) {
        Story story = new Story();
        story.setUserId(currentUser.getId());
        story.setImageUrl(imageFile.getAbsolutePath());
        story.setCaption(caption);
        
        if (storyDAO.create(story)) {
            showInfo("Story created successfully!");
            loadStories();
        } else {
            showInfo("Failed to create story. Please try again.");
        }
    }
    
    private void showStory(Story story) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Story - " + story.getUserName());
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 0; -fx-alignment: center;");
        
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(toImageSource(story.getImageUrl())));
            imageView.setFitWidth(500);
            imageView.setFitHeight(600);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            Label errorLabel = new Label("Could not load story image");
            errorLabel.setStyle("-fx-font-size: 14px;");
            content.getChildren().add(errorLabel);
        }
        
        content.getChildren().add(imageView);
        
        if (story.getCaption() != null && !story.getCaption().isEmpty()) {
            Label captionLabel = new Label(story.getCaption());
            captionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #050505; -fx-padding: 10;");
            captionLabel.setWrapText(true);
            content.getChildren().add(captionLabel);
        }
        
        dialog.getDialogPane().setContent(new javafx.scene.control.ScrollPane(content));
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        
        storyDAO.incrementViews(story.getId());
        dialog.showAndWait();
    }
    
    private void loadPosts() {
        postsContainer.getChildren().clear();
        List<ForumPost> posts = forumService.searchPosts("", null, currentUser);
        
        for (ForumPost post : posts) {
            postsContainer.getChildren().add(createPostCard(post));
        }
    }
    
    private VBox createPostCard(ForumPost post) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setStyle("-fx-background-color: #ffffff; -fx-padding: 16 20; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");
        
        // Header with avatar, name, role, date
        HBox header = new HBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Circle avatar = new Circle(24);
        avatar.setStyle("-fx-fill: linear-gradient(#667eea, #764ba2);");
        
        VBox authorInfo = new VBox(2);
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label authorName = new Label(post.getAuthorName());
        authorName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #050505;");
        
        // Add specialty badge if available
        Label specialtyBadge = new Label("• " + post.getAuthorRole());
        specialtyBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e88e5; -fx-font-weight: normal;");
        
        nameRow.getChildren().addAll(authorName, specialtyBadge);
        
        Label timestamp = new Label(formatTime(post.getCreatedAt()) + " • 🌍");
        timestamp.setStyle("-fx-font-size: 11px; -fx-text-fill: #65676b;");
        
        authorInfo.getChildren().addAll(nameRow, timestamp);
        header.getChildren().addAll(avatar, authorInfo);
        
        // Content
        VBox content = new VBox(8);
        
        if (post.getTag() != null && !post.getTag().isEmpty()) {
            Label tagLabel = new Label(post.getTag().toUpperCase());
            tagLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #1e88e5; -fx-background-color: #e3f2fd; -fx-padding: 3 8; -fx-background-radius: 4;");
            content.getChildren().add(tagLabel);
        }
        
        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #050505;");
        title.setWrapText(true);
        
        Label postContent = new Label(post.getContent());
        postContent.setStyle("-fx-font-size: 14px; -fx-text-fill: #050505;");
        postContent.setWrapText(true);
        
        content.getChildren().addAll(title, postContent);
        
        // Image if present
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            ImageView imageView = new ImageView();
            try {
                imageView.setImage(new Image(toImageSource(post.getImageUrl())));
                imageView.setFitWidth(600);
                imageView.setFitHeight(350);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-background-radius: 8;");
                content.getChildren().add(imageView);
            } catch (Exception e) {
                // Image loading failed, skip
            }
        }
        
        // Interaction bar
        HBox interactionBar = new HBox(20);
        interactionBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        interactionBar.setStyle("-fx-padding: 8 0; -fx-border-color: #e4e6eb; -fx-border-width: 1 0 0 0;");
        
        Button likeBtn = new Button("👍 Like (" + post.getLikeCount() + ")");
        likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-size: 13px; -fx-padding: 8 12; -fx-cursor: hand;");
        likeBtn.setOnAction(e -> handleLike(post));
        
        Button commentBtn = new Button("💬 Comment (" + post.getCommentCount() + ")");
        commentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-size: 13px; -fx-padding: 8 12; -fx-cursor: hand;");
        commentBtn.setOnAction(e -> toggleCommentSection(card, post));
        
        Button shareBtn = new Button("📤 Share (" + post.getShareCount() + ")");
        shareBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-size: 13px; -fx-padding: 8 12; -fx-cursor: hand;");
        shareBtn.setOnAction(e -> handleShare(post));
        
        Button translateBtn = new Button("🌐 Translate");
        translateBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e88e5; -fx-font-size: 13px; -fx-padding: 8 12; -fx-cursor: hand;");
        translateBtn.setOnAction(e -> handleTranslate(post));
        
        interactionBar.getChildren().addAll(likeBtn, commentBtn, shareBtn, translateBtn);
        
        // Add edit/delete buttons if user is author or admin
        if (currentUser != null && (currentUser.getId() == post.getAuthorId() || "ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            Button editBtn = new Button("✏️ Edit");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-size: 11px; -fx-padding: 6 12; -fx-cursor: hand;");
            editBtn.setOnAction(e -> handleEditPost(post));

            Button deleteBtn = new Button("🗑️ Delete");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-padding: 6 12; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> handleDeletePost(post));

            interactionBar.getChildren().addAll(editBtn, deleteBtn);
        }
        
        card.getChildren().addAll(header, content, interactionBar);
        
        return card;
    }
    
    private void handleLike(ForumPost post) {
        if (currentUser != null) {
            boolean success = forumService.toggleLike(currentUser, post);
            if (success) {
                loadPosts(); // Refresh to show updated like count
            }
        }
    }
    
    private void handleShare(ForumPost post) {
        if (post != null) {
            forumService.incrementShareCount(post);
            loadPosts();
            showInfo("Post shared!");
        }
    }
    
    private void handleTranslate(ForumPost post) {
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Translate Post");
        dialog.setHeaderText("Select target language");
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");
        
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("English", "Français", "Español", "Deutsch", "Italiano", "Português", "Русский", "中文", "العربية");
        languageCombo.setValue("English");
        languageCombo.setPrefWidth(300);
        
        content.getChildren().add(languageCombo);
        dialog.getDialogPane().setContent(content);
        
        dialog.getDialogPane().getButtonTypes().addAll(
            javafx.scene.control.ButtonType.OK,
            javafx.scene.control.ButtonType.CANCEL
        );
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                return languageCombo.getValue();
            }
            return null;
        });
        
        var result = dialog.showAndWait();
        if (result.isPresent()) {
            String targetLanguage = result.get();
            performTranslation(post, targetLanguage);
        }
    }
    
    private void performTranslation(ForumPost post, String targetLanguageName) {
        // Map language names to codes
        String targetLangCode = switch (targetLanguageName) {
            case "English" -> "en";
            case "Français" -> "fr";
            case "Español" -> "es";
            case "Deutsch" -> "de";
            case "Italiano" -> "it";
            case "Português" -> "pt";
            case "Русский" -> "ru";
            case "中文" -> "zh";
            case "العربية" -> "ar";
            default -> "en";
        };
        
        // Show loading dialog
        javafx.scene.control.Alert loadingAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Translating");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Please wait, translating your post...");
        
        // Perform translation in background thread
        new Thread(() -> {
            String textToTranslate = post.getTitle() + "\n\n" + post.getContent();
            TranslationService.TranslationResult titleResult = TranslationService.translate(post.getTitle(), "auto", targetLangCode);
            TranslationService.TranslationResult contentResult = TranslationService.translate(post.getContent(), "auto", targetLangCode);
            
            javafx.application.Platform.runLater(() -> {
                loadingAlert.close();
                
                if (titleResult.success && contentResult.success) {
                    showTranslatedPost(post, titleResult.translatedText, contentResult.translatedText, targetLanguageName);
                } else {
                    showInfo("Translation failed: " + (titleResult.error != null ? titleResult.error : contentResult.error));
                }
            });
        }).start();
    }
    
    private void showTranslatedPost(ForumPost originalPost, String translatedTitle, String translatedContent, String language) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Translated Post - " + language);
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 15;");
        
        Label languageLabel = new Label("🌐 Translated to: " + language);
        languageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e88e5;");
        
        Label originalTitleLabel = new Label("Original Title:");
        originalTitleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #65676b;");
        
        Label titleLabel = new Label(originalPost.getTitle());
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #050505;");
        titleLabel.setWrapText(true);
        
        Label translatedTitleLabel = new Label("Translated Title:");
        translatedTitleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1e88e5;");
        
        Label transTitle = new Label(translatedTitle);
        transTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e88e5; -fx-font-weight: bold;");
        transTitle.setWrapText(true);
        
        javafx.scene.control.Separator sep1 = new javafx.scene.control.Separator();
        
        Label originalContentLabel = new Label("Original Content:");
        originalContentLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #65676b;");
        
        Label contentLabel = new Label(originalPost.getContent());
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #050505;");
        contentLabel.setWrapText(true);
        
        Label translatedContentLabel = new Label("Translated Content:");
        translatedContentLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1e88e5;");
        
        Label transContent = new Label(translatedContent);
        transContent.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e88e5;");
        transContent.setWrapText(true);
        
        content.getChildren().addAll(
            languageLabel,
            originalTitleLabel, titleLabel,
            translatedTitleLabel, transTitle,
            sep1,
            originalContentLabel, contentLabel,
            translatedContentLabel, transContent
        );
        
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setStyle("-fx-background-color: #f9fafb;");
        scrollPane.setFitToWidth(true);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefSize(600, 500);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        
        dialog.showAndWait();
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
            controller.setEditingPost(post);

            dialog.showAndWait();
            loadPosts();
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Could not open edit dialog");
        }
    }
    
    private void handleDeletePost(ForumPost post) {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Post");
        confirm.setHeaderText("Are you sure you want to delete this post?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            if (forumService.deletePostByAuthor(currentUser, post)) {
                showInfo("Post deleted successfully!");
                loadPosts();
            } else {
                showInfo("Failed to delete post. You may not have permission.");
            }
        }
    }
    
    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(formatter);
    }

    private String toImageSource(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return "";
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("file:")) {
            return imageUrl;
        }
        return new File(imageUrl).toURI().toString();
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
        avatar.setStyle("-fx-fill: linear-gradient(#667eea, #764ba2);");
        
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
        
        inputArea.getChildren().addAll(avatar, commentInput, postCommentBtn);
        
        // Comments list
        VBox commentsList = new VBox(8);
        commentsList.setStyle("-fx-padding: 8 0;");
        loadComments(post, commentsList);
        
        section.getChildren().addAll(inputArea, commentsList);
        return section;
    }
    
    private void loadComments(ForumPost post, VBox commentsList) {
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
        avatar.setStyle("-fx-fill: linear-gradient(#667eea, #764ba2);");
        
        VBox content = new VBox(4);
        content.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 12; -fx-padding: 8 12;");
        
        Label authorName = new Label(comment.getAuthorName());
        authorName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #050505;");
        
        Label commentText = new Label(comment.getContent());
        commentText.setStyle("-fx-font-size: 13px; -fx-text-fill: #050505;");
        commentText.setWrapText(true);
        commentText.setMaxWidth(500);
        
        content.getChildren().addAll(authorName, commentText);
        
        VBox actions = new VBox(4);
        HBox actionButtons = new HBox(12);
        actionButtons.setStyle("-fx-padding: 4 0 0 12;");
        
        Label likeBtn = new Label("👍 Like (" + comment.getLikeCount() + ")");
        likeBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #65676b; -fx-cursor: hand;");
        likeBtn.setOnMouseClicked(e -> {
            commentDAO.toggleLikeComment(comment.getId(), currentUser.getId());
            loadComments(post, commentsList);
        });
        
        Label timeLabel = new Label(formatTimeAgo(comment.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #65676b;");
        
        actionButtons.getChildren().addAll(likeBtn, timeLabel);
        
        // Add delete button if user is author or admin
        if (currentUser.getId() == comment.getAuthorId() || "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            Label deleteBtn = new Label("🗑️ Delete");
            deleteBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
            deleteBtn.setOnMouseClicked(e -> {
                if (commentDAO.delete(comment.getId())) {
                    loadComments(post, commentsList);
                    // Update post comment count
                    loadPosts();
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
                loadComments(post, commentsList);
                // Refresh posts to update comment count
                loadPosts();
            } else {
                showInfo("Failed to post comment. Please try again.");
            }
        } catch (Exception e) {
            // Check if it's a moderation error
            if (e.getMessage() != null && e.getMessage().contains("Inappropriate")) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Inappropriate Content");
                alert.setHeaderText("Your comment contains inappropriate content");
                alert.setContentText(e.getMessage() + "\n\nPlease remove any offensive language and try again.");
                alert.showAndWait();
            } else {
                showInfo("Failed to post comment: " + e.getMessage());
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
    
    private void showInfo(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // ===================== Navigation Methods =====================
    
    @FXML
    private void showCommunityFeed() {
        // Already in community feed, just refresh
        loadPosts();
        loadStories();
    }
    
    @FXML
    private void showPatientCare() {
        showInfo("Patient Care view not yet implemented");
        // TODO: Navigate to Patient Care view
    }
    
    @FXML
    private void showMedicalLibrary() {
        showInfo("Medical Library view not yet implemented");
        // TODO: Navigate to Medical Library view
    }
    
    @FXML
    private void showSettings() {
        showInfo("Settings view not yet implemented");
        // TODO: Navigate to Settings view
    }
}
