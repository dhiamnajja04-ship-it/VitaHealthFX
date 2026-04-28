package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Entities.Participation;
import tn.esprit.workshopjdbc.Services.EventService;
import tn.esprit.workshopjdbc.Services.ParticipationService;
import tn.esprit.workshopjdbc.Utils.ExcelExporter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ParticipationController {
    @FXML private VBox eventListContainer;
    @FXML private VBox participationListContainer;
    @FXML private Label selectedEventLabel;

    private ParticipationService pService = new ParticipationService();
    private EventService eService = new EventService();
    private Event currentSelectedEvent = null;

    @FXML
    public void initialize() {
        loadEvents();
    }

    private void loadEvents() {
        eventListContainer.getChildren().clear();
        List<Event> events = eService.findEventsWithParticipants();

        if (events.isEmpty()) {
            Label msg = new Label("No active participations.");
            msg.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 20; -fx-font-style: italic;");
            eventListContainer.getChildren().add(msg);
            participationListContainer.getChildren().clear();
            selectedEventLabel.setText("Attendees: None");
            return;
        }

        for (Event e : events) {
            Button btn = new Button(e.getTitle());
            btn.setMaxWidth(Double.MAX_VALUE);

            String baseStyle = "-fx-background-color: transparent; -fx-alignment: CENTER_LEFT; -fx-padding: 12 15; -fx-cursor: hand; -fx-text-fill: #475569; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;";
            String activeStyle = "-fx-background-color: #f0f9ff; -fx-alignment: CENTER_LEFT; -fx-padding: 12 15; -fx-text-fill: #0ea5e9; -fx-font-weight: bold; -fx-border-color: #0ea5e9; -fx-border-width: 0 0 1 2;";

            btn.setStyle(baseStyle);

            if (currentSelectedEvent != null && e.getId() == currentSelectedEvent.getId()) {
                btn.setStyle(activeStyle);
            }

            btn.setOnAction(event -> {
                currentSelectedEvent = e;
                loadEvents();
                showParticipantsForEvent(e);
            });
            eventListContainer.getChildren().add(btn);
        }
    }

    private void showParticipantsForEvent(Event event) {
        participationListContainer.getChildren().clear();

        // --- TITLE & EXPORT HEADER ---
        HBox titleBar = new HBox(15);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-padding: 0 0 15 0;");

        Label title = new Label("Attendees for: " + event.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("📥 Export Excel");
        exportBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");

        List<Participation> list = pService.findByEvent(event.getId());

        exportBtn.setOnAction(e -> {
            if (list.isEmpty()) {
                showAlert("Empty", "No data to export.", Alert.AlertType.WARNING);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Attendees List");
            fileChooser.setInitialFileName("Attendees_" + event.getTitle().replaceAll("\\s+", "_") + ".xlsx");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
            File file = fileChooser.showSaveDialog(participationListContainer.getScene().getWindow());
            if (file != null) {
                try {
                    ExcelExporter.exportParticipantsFromList(list, file.getAbsolutePath());
                    showAlert("Success", "Excel file exported successfully!", Alert.AlertType.INFORMATION);
                } catch (IOException ex) {
                    showAlert("Error", "Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        titleBar.getChildren().addAll(title, spacer, exportBtn);
        participationListContainer.getChildren().add(titleBar);

        // --- DATA ROWS ---
        participationListContainer.getChildren().add(createHeader());

        for (Participation p : list) {
            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 15; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0; -fx-background-color: white;");

            VBox nameBox = new VBox(2);
            Label name = new Label(p.getParticipantName());
            name.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
            Label date = new Label("Registered on " + p.getCreatedAt().toLocalDate());
            date.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            nameBox.setPrefWidth(200);
            nameBox.getChildren().addAll(name, date);

            Label phone = new Label(p.getPhone());
            phone.setPrefWidth(150);
            phone.setStyle("-fx-text-fill: #64748b;");

            Region rowSpacer = new Region();
            HBox.setHgrow(rowSpacer, Priority.ALWAYS);

            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 5; -fx-cursor: hand;");

            delBtn.setOnAction(ev -> {
                pService.delete(p.getId());
                loadEvents();
                showParticipantsForEvent(event);
            });

            row.getChildren().addAll(nameBox, phone, rowSpacer, delBtn);
            participationListContainer.getChildren().add(row);
        }
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setStyle("-fx-background-color: #f8fafc; -fx-padding: 10 15; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;");
        Label hName = new Label("PARTICIPANT NAME"); hName.setPrefWidth(200);
        Label hPhone = new Label("CONTACT"); hPhone.setPrefWidth(150);
        String s = "-fx-font-weight: bold; -fx-text-fill: #94a3b8; -fx-font-size: 11px;";
        hName.setStyle(s); hPhone.setStyle(s);
        header.getChildren().addAll(hName, hPhone);
        return header;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}