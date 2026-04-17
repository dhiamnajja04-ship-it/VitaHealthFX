package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Entities.Participation;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.ParticipationService;
import tn.esprit.workshopjdbc.Utils.UserSession;

import java.io.IOException;
import java.time.LocalDateTime;

public class EventRegistrationController {

    @FXML private Label eventTitleLabel;
    @FXML private TextField nameField, phoneField, emergencyField;
    @FXML private TextArea noteField;

    private Event selectedEvent;
    private ParticipationService pService = new ParticipationService();

    @FXML
    public void initialize() {
        // --- INPUT CONTROL: Real-time numeric filter ---
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                phoneField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            // Limit to 8 digits for standard Tunisian numbers
            if (newVal.length() > 8) {
                phoneField.setText(oldVal);
            }
        });
    }

    public void setEvent(Event event) {
        this.selectedEvent = event;
        eventTitleLabel.setText(event.getTitle());
    }

    @FXML
    private void handleRegister() {
        // 1. Reset styles
        resetStyles();

        // 2. Run Validations
        if (!validateForm()) {
            return;
        }

        User user = UserSession.getSession();
        if (user == null) {
            showAlert("Session Error", "Please log in again.", Alert.AlertType.ERROR);
            return;
        }

        // 3. Check if already registered
        if (pService.exists(selectedEvent.getId(), user.getId())) {
            showAlert("Duplicate", "You are already registered for this workshop.", Alert.AlertType.WARNING);
            return;
        }

        // 4. Create and Save
        Participation p = new Participation();
        p.setEvent(selectedEvent);
        p.setUser(user);
        p.setParticipantName(nameField.getText().trim());
        p.setPhone(phoneField.getText().trim());
        p.setEmergencyContact(emergencyField.getText().trim());
        p.setNote(noteField.getText().trim());
        p.setCreatedAt(LocalDateTime.now());

        pService.add(p);

        showAlert("Success", "Registration confirmed!", Alert.AlertType.INFORMATION);
        handleCancel(); // Go back to Explore view
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        // Validate Name
        if (nameField.getText().trim().isEmpty()) {
            setErrorStyle(nameField);
            errors.append("- Name is required\n");
            isValid = false;
        }

        // Validate Phone (Tunisian format: 8 digits starting with 2,4,5,7,9)
        String phone = phoneField.getText().trim();
        if (!phone.matches("^[24579]\\d{7}$")) {
            setErrorStyle(phoneField);
            errors.append("- Phone must be a valid 8-digit Tunisian number\n");
            isValid = false;
        }

        if (!isValid) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.ERROR);
        }

        return isValid;
    }

    private void setErrorStyle(Control field) {
        field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px; -fx-background-radius: 8; -fx-border-radius: 8;");
    }

    private void resetStyles() {
        String baseStyle = "-fx-background-radius: 8; -fx-border-color: transparent;";
        nameField.setStyle(baseStyle);
        phoneField.setStyle(baseStyle);
    }

    @FXML
    private void handleCancel() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/UserEventView.fxml"));
            StackPane contentArea = (StackPane) nameField.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}