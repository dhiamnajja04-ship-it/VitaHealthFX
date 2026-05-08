package tn.esprit.workshopjdbc.Controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ForumService;
import tn.esprit.workshopjdbc.Utils.SessionManager;

import java.time.format.DateTimeFormatter;

public class ForumModerationController {
    @FXML private TableView<ForumPost> moderationTable;
    @FXML private TableColumn<ForumPost, String> colTitle;
    @FXML private TableColumn<ForumPost, String> colCategory;
    @FXML private TableColumn<ForumPost, String> colAuthor;
    @FXML private TableColumn<ForumPost, String> colStatus;
    @FXML private TableColumn<ForumPost, Number> colReports;
    @FXML private TableColumn<ForumPost, String> colDate;
    @FXML private TextArea contentArea;
    @FXML private Label metaLabel;
    @FXML private Button publishBtn;
    @FXML private Button hideBtn;
    @FXML private Button lockBtn;
    @FXML private Button deleteBtn;
    @FXML private Button refreshBtn;
    @FXML private Label messageLabel;

    private final ForumService forumService = new ForumService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupTable();
        setupActions();
        loadQueue();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        colAuthor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthorName()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colReports.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getReportCount()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() == null ? "" : data.getValue().getCreatedAt().format(formatter)
        ));

        moderationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, post) -> showPost(post));
    }

    private void setupActions() {
        refreshBtn.setOnAction(e -> loadQueue());
        publishBtn.setOnAction(e -> moderate("PUBLISH"));
        hideBtn.setOnAction(e -> moderate("HIDE"));
        lockBtn.setOnAction(e -> moderate("LOCK"));
        deleteBtn.setOnAction(e -> moderate("DELETE"));
    }

    private void loadQueue() {
        moderationTable.setItems(FXCollections.observableArrayList(forumService.getModerationQueue(currentUser)));
        messageLabel.setText(moderationTable.getItems().size() + " element(s) a moderer.");
        if (!moderationTable.getItems().isEmpty()) {
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
        metaLabel.setText(post.getCategoryName() + " | " + post.getAuthorName() + " (" + post.getAuthorRole() + ") | " +
                post.getReportCount() + " signalement(s) | Statut: " + post.getStatus());
    }

    private void moderate(String action) {
        ForumPost post = moderationTable.getSelectionModel().getSelectedItem();
        if (post == null) {
            messageLabel.setText("Selectionnez une publication.");
            return;
        }

        boolean success = switch (action) {
            case "PUBLISH" -> forumService.publishPost(currentUser, post);
            case "HIDE" -> forumService.hidePost(currentUser, post);
            case "LOCK" -> forumService.lockPost(currentUser, post);
            case "DELETE" -> confirmDelete(post) && forumService.deletePost(currentUser, post);
            default -> false;
        };

        messageLabel.setText(success ? "Action appliquee avec succes." : "Action refusee ou impossible.");
        loadQueue();
    }

    private boolean confirmDelete(ForumPost post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression forum");
        alert.setHeaderText("Supprimer cette publication ?");
        alert.setContentText(post.getTitle());
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
