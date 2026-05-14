package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.ForumCategory;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ForumService;
import tn.esprit.workshopjdbc.dao.ForumSchemaInitializer;

import java.util.List;

public class CreatePostDialogController {
    
    @FXML private Circle userAvatarCircle;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private TextField headlineField;
    @FXML private ComboBox<ForumCategory> categoryCombo;
    @FXML private TextArea contentArea;
    @FXML private TextField tagField;
    @FXML private Label attachedFilesLabel;
    @FXML private Button saveDraftBtn;
    @FXML private Button publishBtn;
    @FXML private Button closeBtn;
    
    private final ForumService forumService = new ForumService();
    private User currentUser;
    private Stage dialog;
    private String selectedImagePath = "";
    private String selectedVideoPath = "";
    private ForumPost editingPost = null; // Track if we're editing an existing post
    
    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            userRoleLabel.setText(user.getRole());
        }
        loadCategories();
    }
    
    public void setEditingPost(ForumPost post) {
        this.editingPost = post;
        if (post != null) {
            // Pre-fill form with existing post data
            headlineField.setText(post.getTitle());
            contentArea.setText(post.getContent());
            tagField.setText(post.getTag() != null ? post.getTag() : "");
            selectedImagePath = post.getImageUrl() != null ? post.getImageUrl() : "";
            selectedVideoPath = post.getVideoUrl() != null ? post.getVideoUrl() : "";
            updateAttachedFilesLabel();
            
            // Select the correct category
            for (ForumCategory category : categoryCombo.getItems()) {
                if (category.getId() == post.getCategoryId()) {
                    categoryCombo.setValue(category);
                    break;
                }
            }
            
            // Update button text
            if (publishBtn != null) {
                publishBtn.setText("Update Post");
            }
        }
    }
    
    @FXML
    public void initialize() {
        ForumSchemaInitializer.ensureSchema();
    }
    
    private void loadCategories() {
        List<ForumCategory> categories = forumService.getCategories();
        categoryCombo.getItems().addAll(categories);
        if (!categories.isEmpty()) {
            categoryCombo.setValue(categories.get(0));
        }
    }
    
    @FXML
    private void closeDialog() {
        if (dialog != null) {
            dialog.close();
        }
    }
    
    @FXML
    private void saveDraft() {
        showAlert("Draft Saved", "Your draft has been saved successfully.");
    }
    
    @FXML
    private void publishPost() {
        if (currentUser == null) {
            showAlert("Error", "You must be logged in to publish a post.");
            return;
        }
        
        // Validation
        if (headlineField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a headline.");
            return;
        }
        
        if (contentArea.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter content for your post.");
            return;
        }
        
        if (categoryCombo.getValue() == null) {
            showAlert("Validation Error", "Please select a category.");
            return;
        }
        
        boolean success;
        
        try {
            if (editingPost != null) {
                // Update existing post
                success = forumService.updatePost(
                        currentUser,
                        editingPost,
                        headlineField.getText().trim(),
                        contentArea.getText().trim(),
                        selectedImagePath,
                        selectedVideoPath,
                        tagField.getText().trim()
                );
                
                if (success) {
                    showAlert("Success", "Your post has been updated successfully!");
                    clearForm();
                    closeDialog();
                } else {
                    showAlert("Error", "Failed to update your post. Please try again.");
                }
            } else {
                // Create new post
                success = forumService.createPost(
                        currentUser,
                        categoryCombo.getValue(),
                        headlineField.getText().trim(),
                        contentArea.getText().trim(),
                        "fr",
                        selectedImagePath,
                        selectedVideoPath,
                        tagField.getText().trim()
                );
                
                if (success) {
                    showAlert("Success", "Your clinical entry has been submitted for review. An admin will review and publish it soon!");
                    clearForm();
                    closeDialog();
                } else {
                    showAlert("Error", "Failed to publish your post. Please try again.");
                }
            }
        } catch (IllegalArgumentException e) {
            // Content moderation failed - show error to user
            showModerationAlert("Inappropriate Content", e.getMessage());
        }
    }
    
    private void showModerationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Your post contains inappropriate content");
        alert.setContentText(message + "\n\nPlease remove any offensive language and try again.");
        alert.showAndWait();
    }
    
    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        Stage stage = dialog != null ? dialog : new Stage();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            updateAttachedFilesLabel();
            showAlert("Success", "Image selected: " + selectedFile.getName());
        }
    }
    
    @FXML
    private void browseVideo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Video");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov", "*.mkv", "*.wmv")
        );
        
        Stage stage = dialog != null ? dialog : new Stage();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            selectedVideoPath = selectedFile.getAbsolutePath();
            updateAttachedFilesLabel();
            showAlert("Success", "Video selected: " + selectedFile.getName());
        }
    }
    
    // Rich text formatting methods
    @FXML
    private void formatBold() {
        insertAtCursor("**");
    }
    
    @FXML
    private void formatItalic() {
        insertAtCursor("_");
    }
    
    @FXML
    private void formatUnderline() {
        insertAtCursor("__");
    }
    
    @FXML
    private void formatBullet() {
        insertAtCursor("\n- ");
    }
    
    @FXML
    private void formatNumber() {
        insertAtCursor("\n1. ");
    }
    
    @FXML
    private void formatCode() {
        insertAtCursor("`");
    }
    
    @FXML
    private void insertLink() {
        insertAtCursor("[link](url)");
    }
    
    @FXML
    private void insertImage() {
        insertAtCursor("![alt](url)");
    }
    
    @FXML
    private void insertVideo() {
        insertAtCursor("[video](url)");
    }
    
    private void insertAtCursor(String text) {
        int caretPos = contentArea.getCaretPosition();
        contentArea.insertText(caretPos, text);
    }
    
    private void clearForm() {
        headlineField.clear();
        contentArea.clear();
        selectedImagePath = "";
        selectedVideoPath = "";
        updateAttachedFilesLabel();
        tagField.clear();
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.setValue(categoryCombo.getItems().get(0));
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateAttachedFilesLabel() {
        if (attachedFilesLabel == null) return;

        int count = 0;
        if (!selectedImagePath.isBlank()) count++;
        if (!selectedVideoPath.isBlank()) count++;
        attachedFilesLabel.setText(count == 0 ? "" : count + " file(s) attached");
    }
}
