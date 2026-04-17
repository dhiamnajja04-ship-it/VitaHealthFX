package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.workshopjdbc.Entities.Participation;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ParticipationService;
import tn.esprit.workshopjdbc.Utils.UserSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private FlowPane myEventsContainer;
    @FXML private VBox emptyState;

    private ParticipationService pService = new ParticipationService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        User user = UserSession.getSession();
        if (user != null) {
            welcomeLabel.setText("Hello " + user.getFirstName() + " 👋");
            loadEvents(user.getId());
        }
    }

    private void loadEvents(int userId) {
        List<Participation> joined = pService.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId() == userId)
                .collect(Collectors.toList());

        myEventsContainer.getChildren().clear();

        if (joined.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            for (Participation p : joined) {
                myEventsContainer.getChildren().add(createCard(p));
            }
        }
    }

    private VBox createCard(Participation p) {
        VBox card = new VBox(12);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(300);

        Label badge = new Label("REGISTERED WORKSHOP");
        badge.getStyleClass().add("badge-teal");

        Label title = new Label(p.getEvent().getTitle());
        title.getStyleClass().add("event-title");
        title.setWrapText(true);
        title.setMinHeight(50);

        HBox footer = new HBox(15);
        footer.getChildren().addAll(
                createInfoBox("DATE", "📅 " + p.getEvent().getDate().toLocalDate()),
                new Separator(Orientation.VERTICAL),
                createInfoBox("PHONE", "📞 " + p.getPhone())
        );

        // --- ACTION BUTTONS (The "UD" in CRUD) ---
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 5 0 0 0;");

        Button editBtn = new Button("Edit Info");
        editBtn.getStyleClass().add("btn-edit"); // Make sure this is in your CSS
        editBtn.setOnAction(e -> handleEdit(p));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-cancel"); // Make sure this is in your CSS
        cancelBtn.setOnAction(e -> handleCancel(p));

        actions.getChildren().addAll(editBtn, cancelBtn);

        card.getChildren().addAll(badge, title, new Separator(), footer, actions);
        return card;
    }

    private VBox createInfoBox(String label, String value) {
        Label l = new Label(label); l.getStyleClass().add("meta-label");
        Label v = new Label(value); v.getStyleClass().add("meta-value");
        return new VBox(2, l, v);
    }

    // --- CRUD OPERATIONAL METHODS ---

    private void handleEdit(Participation p) {
        // Using a standard dialog to update the phone number/note
        TextInputDialog dialog = new TextInputDialog(p.getPhone());
        dialog.setTitle("Update Registration");
        dialog.setHeaderText("Editing registration for: " + p.getEvent().getTitle());
        dialog.setContentText("Enter new contact phone:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPhone -> {
            if (!newPhone.trim().isEmpty()) {
                p.setPhone(newPhone);
                pService.update(p); // Calls the update method in Service
                refreshDashboard(); // Refresh UI to show changes
            }
        });
    }

    private void handleCancel(Participation p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Registration");
        alert.setHeaderText("Confirm Cancellation");
        alert.setContentText("Are you sure you want to withdraw from " + p.getEvent().getTitle() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            pService.delete(p.getId()); // Calls the delete method in Service
            refreshDashboard(); // Refresh UI to remove the card
        }
    }

    @FXML
    private void handleExplore() {
        // Logic to switch view back to the event list
        System.out.println("Navigating to explore events...");
    }
}