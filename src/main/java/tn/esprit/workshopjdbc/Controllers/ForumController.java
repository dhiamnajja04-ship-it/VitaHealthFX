package tn.esprit.workshopjdbc.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.workshopjdbc.Entities.ForumCategory;
import tn.esprit.workshopjdbc.Entities.ForumComment;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.FileUploadService;
import tn.esprit.workshopjdbc.Services.ForumService;
import tn.esprit.workshopjdbc.Utils.SessionManager;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ForumController {
    @FXML private ComboBox<ForumCategory> categoryFilterCombo;
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button refreshBtn;
    @FXML private ListView<ForumPost> postsListView;

    @FXML private Label selectedTitleLabel;
    @FXML private Label selectedMetaLabel;
    @FXML private TextArea selectedContentArea;
    @FXML private ListView<ForumComment> commentsListView;
    @FXML private TextArea commentArea;
    @FXML private Button addCommentBtn;
    @FXML private Button usefulBtn;
    @FXML private Button reportBtn;
    @FXML private Button translateBtn;
    @FXML private HBox postActionBox;
    @FXML private Button editPostBtn;
    @FXML private Button deletePostBtn;

    @FXML private VBox createPostBox;
    @FXML private ComboBox<ForumCategory> createCategoryCombo;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> languageCombo;
    @FXML private TextArea contentArea;
    @FXML private Button publishBtn;
    @FXML private Button uploadImageBtn;
    @FXML private Button uploadVideoBtn;
    @FXML private Label uploadStatusLabel;
    @FXML private Label messageLabel;

    private final ForumService forumService = new ForumService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private User currentUser;
    private ForumPost selectedPost;
    private ForumComment selectedComment;
    private String uploadedImagePath;
    private String uploadedVideoPath;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupLists();
        setupControls();
        loadCategories();
        loadPosts();
    }

    private void setupLists() {
        postsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ForumPost post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setText(null);
                } else {
                    setText(post.getTitle() + "\n" +
                            post.getCategoryName() + " | " + post.getAuthorName() + " | " +
                            post.getCommentCount() + " commentaires | " + post.getUsefulCount() + " utiles");
                }
            }
        });

        commentsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ForumComment comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) {
                    setText(null);
                } else {
                    String date = comment.getCreatedAt() == null ? "" : comment.getCreatedAt().format(formatter);
                    setText(comment.getAuthorName() + " (" + comment.getAuthorRole() + ") - " + date + "\n" + comment.getContent());
                }
            }
        });

        postsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, post) -> showPost(post));
        commentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, comment) -> selectedComment = comment);
    }

    private void setupControls() {
        languageCombo.setItems(FXCollections.observableArrayList("fr", "en", "ar"));
        languageCombo.setValue("fr");

        searchBtn.setOnAction(e -> loadPosts());
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            categoryFilterCombo.getSelectionModel().clearSelection();
            loadPosts();
        });
        publishBtn.setOnAction(e -> createPost());
        addCommentBtn.setOnAction(e -> addComment());
        usefulBtn.setOnAction(e -> markUseful());
        reportBtn.setOnAction(e -> reportPost());
        translateBtn.setOnAction(e -> translateSelectedPost());
        editPostBtn.setOnAction(e -> editPost());
        deletePostBtn.setOnAction(e -> deletePost());
        uploadImageBtn.setOnAction(e -> uploadImage());
        uploadVideoBtn.setOnAction(e -> uploadVideo());

        boolean connected = currentUser != null;
        createPostBox.setDisable(!connected);
        commentArea.setDisable(!connected);
        addCommentBtn.setDisable(!connected);
        usefulBtn.setDisable(!connected);
        reportBtn.setDisable(!connected);
        editPostBtn.setDisable(true);
        deletePostBtn.setDisable(true);

        if (!connected) {
            messageLabel.setText("Connectez-vous pour publier, commenter ou signaler.");
        }
    }

    private void loadCategories() {
        List<ForumCategory> categories = forumService.getCategories();
        categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
        createCategoryCombo.setItems(FXCollections.observableArrayList(categories));
        if (!categories.isEmpty()) {
            createCategoryCombo.setValue(categories.get(0));
        }
    }

    private void loadPosts() {
        ForumCategory selectedCategory = categoryFilterCombo.getValue();
        String keyword = searchField.getText();
        postsListView.setItems(FXCollections.observableArrayList(forumService.searchPosts(keyword, selectedCategory, currentUser)));
        if (!postsListView.getItems().isEmpty()) {
            postsListView.getSelectionModel().selectFirst();
        } else {
            clearSelectedPost();
        }
    }

    private void showPost(ForumPost post) {
        if (post == null) {
            clearSelectedPost();
            return;
        }

        selectedPost = post;
        String date = post.getCreatedAt() == null ? "" : post.getCreatedAt().format(formatter);
        selectedTitleLabel.setText(post.getTitle());
        selectedMetaLabel.setText(post.getCategoryName() + " | " + post.getAuthorName() + " (" + post.getAuthorRole() + ") | " + date + " | " + post.getStatus());

        String content = post.getContent();
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            content += "\n\n[Image: " + post.getImageUrl() + "]";
        }
        if (post.getVideoUrl() != null && !post.getVideoUrl().isEmpty()) {
            content += "\n\n[Video: " + post.getVideoUrl() + "]";
        }
        selectedContentArea.setText(content);
        commentsListView.setItems(FXCollections.observableArrayList(forumService.getComments(post)));

        boolean canModify = currentUser != null && (currentUser.getId() == post.getAuthorId() || "ADMIN".equalsIgnoreCase(currentUser.getRole()));
        editPostBtn.setDisable(!canModify);
        deletePostBtn.setDisable(!canModify);
    }

    private void clearSelectedPost() {
        selectedPost = null;
        selectedComment = null;
        selectedTitleLabel.setText("Selectionnez une discussion");
        selectedMetaLabel.setText("");
        selectedContentArea.clear();
        commentsListView.getItems().clear();
        editPostBtn.setDisable(true);
        deletePostBtn.setDisable(true);
    }

    private void createPost() {
        try {
            boolean created = forumService.createPost(
                    currentUser,
                    createCategoryCombo.getValue(),
                    titleField.getText(),
                    contentArea.getText(),
                    languageCombo.getValue(),
                    uploadedImagePath,
                    uploadedVideoPath,
                    null
            );

            if (created) {
                messageLabel.setText("Publication ajoutee. Elle peut etre en attente si la moderation l'exige.");
                titleField.clear();
                contentArea.clear();
                uploadedImagePath = null;
                uploadedVideoPath = null;
                uploadStatusLabel.setText("");
                loadPosts();
            } else {
                messageLabel.setText("Impossible de publier: verifiez la categorie, le titre et le contenu.");
            }
        } catch (IllegalArgumentException e) {
            showModerationAlert("Post Moderation", e.getMessage());
            messageLabel.setText("Votre publication contient du contenu inapproprié.");
        }
    }

    private void addComment() {
        ForumPost post = postsListView.getSelectionModel().getSelectedItem();
        try {
            boolean created = forumService.addComment(currentUser, post, commentArea.getText());
            if (created) {
                commentArea.clear();
                showPost(post);
                loadPosts();
                postsListView.getSelectionModel().select(post);
                messageLabel.setText("Commentaire ajouté avec succès.");
            } else {
                messageLabel.setText("Impossible d'ajouter le commentaire.");
            }
        } catch (IllegalArgumentException e) {
            // Show moderation alert
            showModerationAlert("Comment Moderation", e.getMessage());
            messageLabel.setText("Votre commentaire contient du contenu inapproprié.");
        }
    }

    private void showModerationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Content Not Acceptable");
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void editPost() {
        if (selectedPost == null) return;

        // FIX: changed Dialog<String> to Dialog<ButtonType> to match showAndWait() return type
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Discussion");
        dialog.setHeaderText("Edit your discussion");

        VBox content = new VBox(10);
        content.setPrefWidth(400);

        TextField titleField = new TextField(selectedPost.getTitle());
        titleField.setPromptText("Title");

        TextArea contentField = new TextArea(selectedPost.getContent());
        contentField.setWrapText(true);
        contentField.setPrefHeight(200);

        content.getChildren().addAll(
                new Label("Title:"),
                titleField,
                new Label("Content:"),
                contentField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (forumService.updatePost(currentUser, selectedPost, titleField.getText(), contentField.getText(),
                    selectedPost.getImageUrl(), selectedPost.getVideoUrl(), selectedPost.getTag())) {
                messageLabel.setText("Discussion mise a jour avec succes.");
                loadPosts();
                postsListView.getSelectionModel().select(selectedPost);
            } else {
                messageLabel.setText("Echec de la mise a jour de la discussion.");
            }
        }
    }

    private void deletePost() {
        if (selectedPost == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Discussion");
        alert.setHeaderText("Are you sure you want to delete this discussion?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (forumService.deletePostByAuthor(currentUser, selectedPost)) {
                messageLabel.setText("Discussion supprimee avec succes.");
                loadPosts();
            } else {
                messageLabel.setText("Echec de la suppression de la discussion.");
            }
        }
    }

    private void markUseful() {
        ForumPost post = postsListView.getSelectionModel().getSelectedItem();
        if (forumService.markUseful(post)) {
            loadPosts();
        }
    }

    private void reportPost() {
        ForumPost post = postsListView.getSelectionModel().getSelectedItem();
        if (post == null) return;

        TextInputDialog dialog = new TextInputDialog("Contenu inapproprie ou dangereux");
        dialog.setTitle("Signaler une discussion");
        dialog.setHeaderText("Pourquoi souhaitez-vous signaler cette discussion ?");
        dialog.showAndWait().ifPresent(reason -> {
            if (forumService.reportPost(currentUser, post, reason)) {
                messageLabel.setText("Signalement envoye a la moderation.");
            } else {
                messageLabel.setText("Impossible d'envoyer le signalement.");
            }
        });
    }

    private void translateSelectedPost() {
        ForumPost post = postsListView.getSelectionModel().getSelectedItem();
        if (post == null) return;
        selectedContentArea.setText(forumService.translatePreview(post, "en"));
        messageLabel.setText("Traduction simulee affichee. Une API de traduction pourra etre branchee ensuite.");
    }

    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String imagePath = FileUploadService.uploadImage(selectedFile);
            if (imagePath != null) {
                uploadedImagePath = imagePath;
                uploadStatusLabel.setText("Image uploaded: " + selectedFile.getName());
                messageLabel.setText("");
            } else {
                uploadStatusLabel.setText("Failed to upload image (max 5 MB, jpg/png/gif/webp)");
                messageLabel.setText("");
            }
        }
    }

    private void uploadVideo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a video");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov", "*.mkv", "*.webm")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String videoPath = FileUploadService.uploadVideo(selectedFile);
            if (videoPath != null) {
                uploadedVideoPath = videoPath;
                uploadStatusLabel.setText("Video uploaded: " + selectedFile.getName());
                messageLabel.setText("");
            } else {
                uploadStatusLabel.setText("Failed to upload video (max 50 MB, mp4/avi/mov/mkv/webm)");
                messageLabel.setText("");
            }
        }
    }

    public void editComment() {
        if (selectedComment == null) return;

        // FIX: changed Dialog<String> to Dialog<ButtonType> to match showAndWait() return type
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Edit your comment");

        TextArea contentField = new TextArea(selectedComment.getContent());
        contentField.setWrapText(true);
        contentField.setPrefHeight(150);

        dialog.getDialogPane().setContent(contentField);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (forumService.updateComment(currentUser, selectedComment, contentField.getText())) {
                messageLabel.setText("Commentaire mis a jour avec succes.");
                showPost(selectedPost);
            } else {
                messageLabel.setText("Echec de la mise a jour du commentaire.");
            }
        }
    }

    public void deleteComment() {
        if (selectedComment == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Comment");
        alert.setHeaderText("Are you sure you want to delete this comment?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (forumService.deleteComment(currentUser, selectedComment)) {
                messageLabel.setText("Commentaire supprime avec succes.");
                showPost(selectedPost);
            } else {
                messageLabel.setText("Echec de la suppression du commentaire.");
            }
        }
    }
}