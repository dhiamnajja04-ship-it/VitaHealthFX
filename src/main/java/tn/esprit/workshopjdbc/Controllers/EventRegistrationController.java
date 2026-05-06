package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Entities.Participation;
import tn.esprit.workshopjdbc.Services.ParticipationService;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.Utils.UserSession;
import tn.esprit.workshopjdbc.Entities.User;

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
        // Validation: Only allow 8 digits for phone (Tunisian format)
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                phoneField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (newVal.length() > 8) {
                phoneField.setText(oldVal);
            }
        });

        // UI Fix: Ensure the text fields don't have white text on white background
        String fieldStyle = "-fx-text-fill: #1e293b; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-background-radius: 8; -fx-border-radius: 8;";
        nameField.setStyle(fieldStyle);
        phoneField.setStyle(fieldStyle);
        emergencyField.setStyle(fieldStyle);
        noteField.setStyle(fieldStyle);
    }

    public void setEvent(Event event) {
        this.selectedEvent = event;
        if (eventTitleLabel != null) {
            eventTitleLabel.setText(event.getTitle());
        }
    }

    @FXML
    private void handleRegister() {
        resetStyles();

        // 1. Validate inputs
        if (!validateForm()) {
            showAlert("Input Error", "Please verify your information. Phone must be 8 digits.", Alert.AlertType.WARNING);
            return;
        }

        // 2. Get User from Session
        User user = SessionManager.getInstance().getCurrentUser();

        // DEBUG: Uncomment this if you are testing without a login system
        // if(user == null) { user = new User(1, "Test", "User"); }

        if (user == null) {
            showAlert("Session Error", "Session expired. Please log in again.", Alert.AlertType.ERROR);
            return;
        }

        // 3. Check for existing participation
        if (pService.isUserParticipating(selectedEvent.getId(), user.getId())) {
            showAlert("Already Registered", "You have already joined this workshop.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // 4. Create and Save Participation
            Participation p = new Participation();
            p.setEvent(selectedEvent);
            p.setUser(user);
            p.setParticipantName(nameField.getText().trim());
            p.setPhone(phoneField.getText().trim());
            p.setEmergencyContact(emergencyField.getText().trim());
            p.setNote(noteField.getText().trim());
            p.setCreatedAt(LocalDateTime.now());

            pService.add(p);

            showAlert("Success", "Your spot is reserved for " + selectedEvent.getTitle() + "!", Alert.AlertType.INFORMATION);

            // 5. Close the window
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not save registration: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (nameField.getText().trim().isEmpty()) {
            setErrorStyle(nameField);
            isValid = false;
        }
        // Validates Tunisian phone: 8 digits starting with 2, 4, 5, 7, or 9
        if (!phoneField.getText().trim().matches("^[24579]\\d{7}$")) {
            setErrorStyle(phoneField);
            isValid = false;
        }
        return isValid;
    }

    private void setErrorStyle(Control field) {
        field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px; -fx-background-radius: 8; -fx-text-fill: #1e293b;");
    }

    private void resetStyles() {
        String baseStyle = "-fx-text-fill: #1e293b; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-background-radius: 8;";
        nameField.setStyle(baseStyle);
        phoneField.setStyle(baseStyle);
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        // Since we opened this in a popup Stage, we close the Stage
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}