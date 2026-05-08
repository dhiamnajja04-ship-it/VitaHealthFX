package tn.esprit.workshopjdbc.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.workshopjdbc.Entities.ForumCategory;
import tn.esprit.workshopjdbc.Entities.ForumComment;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ForumService;
import tn.esprit.workshopjdbc.Utils.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @FXML private VBox createPostBox;
    @FXML private ComboBox<ForumCategory> createCategoryCombo;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> languageCombo;
    @FXML private TextArea contentArea;
    @FXML private Button publishBtn;
    @FXML private Label messageLabel;

    private final ForumService forumService = new ForumService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private User currentUser;

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

        boolean connected = currentUser != null;
        createPostBox.setDisable(!connected);
        commentArea.setDisable(!connected);
        addCommentBtn.setDisable(!connected);
        usefulBtn.setDisable(!connected);
        reportBtn.setDisable(!connected);

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

        String date = post.getCreatedAt() == null ? "" : post.getCreatedAt().format(formatter);
        selectedTitleLabel.setText(post.getTitle());
        selectedMetaLabel.setText(post.getCategoryName() + " | " + post.getAuthorName() + " (" + post.getAuthorRole() + ") | " + date + " | " + post.getStatus());
        selectedContentArea.setText(post.getContent());
        commentsListView.setItems(FXCollections.observableArrayList(forumService.getComments(post)));
    }

    private void clearSelectedPost() {
        selectedTitleLabel.setText("Selectionnez une discussion");
        selectedMetaLabel.setText("");
        selectedContentArea.clear();
        commentsListView.getItems().clear();
    }

    private void createPost() {
        boolean created = forumService.createPost(
                currentUser,
                createCategoryCombo.getValue(),
                titleField.getText(),
                contentArea.getText(),
                languageCombo.getValue()
        );

        if (created) {
            messageLabel.setText("Publication ajoutee. Elle peut etre en attente si la moderation l'exige.");
            titleField.clear();
            contentArea.clear();
            loadPosts();
        } else {
            messageLabel.setText("Impossible de publier: verifiez la categorie, le titre et le contenu.");
        }
    }

    private void addComment() {
        ForumPost post = postsListView.getSelectionModel().getSelectedItem();
        boolean created = forumService.addComment(currentUser, post, commentArea.getText());
        if (created) {
            commentArea.clear();
            showPost(post);
            loadPosts();
            postsListView.getSelectionModel().select(post);
        } else {
            messageLabel.setText("Impossible d'ajouter le commentaire.");
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
}
