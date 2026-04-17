package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Services.EventService;
import tn.esprit.workshopjdbc.Services.ParticipationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminEventController {
    @FXML private TextField titleField, latField, lngField, searchBar;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    @FXML private VBox eventContainer;

    private EventService eventService = new EventService();
    private ParticipationService pService = new ParticipationService();
    private Event selectedEvent = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

    @FXML
    public void initialize() {
        loadData();
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            displayEvents(eventService.searchByTitleOrDescription(newVal));
        });
    }

    private void loadData() {
        displayEvents(eventService.findAll());
    }

    private void displayEvents(List<Event> events) {
        eventContainer.getChildren().clear();
        if (events.isEmpty()) {
            VBox empty = new VBox(new Label("No events found."));
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(50));
            eventContainer.getChildren().add(empty);
            return;
        }

        for (Event e : events) {
            HBox card = new HBox(20);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-cursor: hand;");

            VBox details = new VBox(8);
            HBox.setHgrow(details, Priority.ALWAYS);
            Label title = new Label(e.getTitle());
            title.setFont(Font.font("System", FontWeight.BOLD, 18));
            title.setTextFill(Color.web("#1e293b"));

            Label desc = new Label(e.getDescription());
            desc.setTextFill(Color.web("#64748b"));
            desc.setWrapText(true);
            desc.setMaxWidth(500);

            HBox meta = new HBox(15);
            Label date = new Label("📅 " + e.getDate().format(formatter));
            date.setTextFill(Color.web("#0ea5e9"));
            date.setStyle("-fx-font-weight: bold;");

            meta.getChildren().addAll(date);
            details.getChildren().addAll(title, desc, meta);

            VBox statusBox = new VBox(10);
            statusBox.setAlignment(Pos.CENTER_RIGHT);
            boolean isUpcoming = e.getDate().isAfter(LocalDateTime.now());
            Label statusBadge = new Label(isUpcoming ? "UPCOMING" : "PAST");
            statusBadge.setStyle("-fx-background-color: " + (isUpcoming ? "#dcfce7;" : "#f1f5f9;") +
                    "-fx-text-fill: " + (isUpcoming ? "#166534;" : "#475569;") +
                    "-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 10px;");

            int pCount = pService.findByEvent(e.getId()).size();
            Label participants = new Label(pCount + " Attendees");
            participants.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

            statusBox.getChildren().addAll(statusBadge, participants);
            card.getChildren().addAll(details, statusBox);
            card.setOnMouseClicked(event -> selectEvent(e, card));
            eventContainer.getChildren().add(card);
        }
    }

    private void selectEvent(Event e, HBox card) {
        selectedEvent = e;
        titleField.setText(e.getTitle());
        descField.setText(e.getDescription());
        datePicker.setValue(e.getDate().toLocalDate());
        latField.setText(String.valueOf(e.getLatitude()));
        lngField.setText(String.valueOf(e.getLongitude()));

        eventContainer.getChildren().forEach(node -> node.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: transparent;"));
        card.setStyle("-fx-background-color: #f0f9ff; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #0ea5e9; -fx-border-width: 2;");
    }

    @FXML
    private void handleAdd() {
        if (!validate()) return;

        // NEW: Check for duplicates before calling service
        String title = titleField.getText();
        LocalDateTime dateTime = datePicker.getValue().atStartOfDay();

        if (eventService.exists(title, dateTime)) {
            showError("A workshop with this title already exists on this date!");
            return;
        }

        Event e = new Event();
        fillEventData(e);
        eventService.add(e);
        loadData();
        handleClear();
    }

    @FXML
    private void handleUpdate() {
        if (selectedEvent != null && validate()) {
            fillEventData(selectedEvent);
            eventService.update(selectedEvent);
            loadData();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedEvent != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete workshop?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    eventService.delete(selectedEvent.getId());
                    loadData();
                    handleClear();
                }
            });
        }
    }

    @FXML
    private void handleClear() {
        titleField.clear(); descField.clear(); latField.clear(); lngField.clear();
        datePicker.setValue(null); selectedEvent = null;
        eventContainer.getChildren().forEach(n -> n.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12;"));
    }

    private void fillEventData(Event e) {
        e.setTitle(titleField.getText());
        e.setDescription(descField.getText());
        e.setDate(datePicker.getValue().atStartOfDay());
        e.setLatitude(Float.parseFloat(latField.getText()));
        e.setLongitude(Float.parseFloat(lngField.getText()));
    }

    private boolean validate() {
        if (titleField.getText().isEmpty() || datePicker.getValue() == null) {
            showError("Title and Date are required.");
            return false;
        }
        try {
            Float.parseFloat(latField.getText());
            Float.parseFloat(lngField.getText());
        } catch (Exception e) {
            showError("Lat/Lng must be valid numbers.");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}