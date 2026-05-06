package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import tn.esprit.workshopjdbc.Entities.Participation;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ParticipationService;
import tn.esprit.workshopjdbc.Utils.SessionManager;

import java.util.List;
import java.util.Optional;

public class MyParticipationsController {

    @FXML private VBox participationContainer;
    private ParticipationService pService = new ParticipationService();

    @FXML
    public void initialize() {
        loadMyParticipations();
    }

    private void loadMyParticipations() {
        participationContainer.getChildren().clear();
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) return;

        List<Participation> mylist = pService.findByUser(currentUser.getId());

        if (mylist.isEmpty()) {
            Label emptyLabel = new Label("You haven't joined any events yet.");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 20;");
            participationContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Participation p : mylist) {
            participationContainer.getChildren().add(createParticipationRow(p));
        }
    }

    private HBox createParticipationRow(Participation p) {
        HBox row = new HBox(15);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-alignment: CENTER_LEFT;");

        VBox details = new VBox(5);
        Label eventTitle = new Label(p.getEvent().getTitle());
        eventTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");

        Label info = new Label("Registered as: " + p.getParticipantName() + " | Phone: " + p.getPhone());
        info.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        details.getChildren().addAll(eventTitle, info);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action Buttons
        Button editBtn = new Button("Edit Info");
        editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0f172a; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEdit(p));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> handleCancel(p));

        row.getChildren().addAll(details, spacer, editBtn, cancelBtn);
        return row;
    }

    private void handleEdit(Participation p) {
        // Logic to show a TextInputDialog or a small custom popup to change Name/Phone
        TextInputDialog dialog = new TextInputDialog(p.getPhone());
        dialog.setTitle("Update Contact Info");
        dialog.setHeaderText("Updating registration for: " + p.getEvent().getTitle());
        dialog.setContentText("New Phone Number:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPhone -> {
            p.setPhone(newPhone);
            pService.update(p); // Assuming your service has an update method
            loadMyParticipations();
        });
    }

    private void handleCancel(Participation p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Registration");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to leave the event: " + p.getEvent().getTitle());

        if (alert.showAndWait().get() == ButtonType.OK) {
            pService.delete(p.getId());
            loadMyParticipations();
        }
    }
}