package tn.esprit.workshopjdbc.Controllers;

import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Services.EventService;
import tn.esprit.workshopjdbc.Services.ParticipationService;

import java.awt.Desktop;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminEventController {
    @FXML private TextField titleField, latField, lngField, searchBar;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    @FXML private VBox eventContainer;

    private final EventService eventService = new EventService();
    private final ParticipationService pService = new ParticipationService();
    private Event selectedEvent = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

    // STATIC ensures the server stays alive even if the controller instance changes
    private static HttpServer persistentServer;

    @FXML
    public void initialize() {
        loadData();
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            displayEvents(eventService.searchByTitleOrDescription(newVal));
        });
    }

    /**
     * Starts a background server on port 8089.
     * Stays active for multiple clicks.
     */
    private void startCoordServer() {
        try {
            // 1. Check if the server is already running.
            // If it is, just exit the method; no need to start a new one.
            if (persistentServer != null) {
                System.out.println("📡 Server is already active on 8089. No action needed.");
                return;
            }

            // 2. Create the server
            persistentServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 8089), 0);

            persistentServer.createContext("/set-coords", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                if (query != null) {
                    String[] params = query.split("&");
                    String lat = params[0].split("=")[1];
                    String lng = params[1].split("=")[1];

                    Platform.runLater(() -> {
                        latField.setText(lat);
                        lngField.setText(lng);
                        System.out.println("📍 Sync Success: " + lat + ", " + lng);
                    });
                }

                // CORS Headers for browser compatibility
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Content-Type", "text/plain");

                String response = "OK";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });

            persistentServer.setExecutor(null);
            persistentServer.start();
            System.out.println("🚀 Background server started on 127.0.0.1:8089");

        } catch (java.net.BindException e) {
            // 3. Handle the 'Address already in use' error specifically
            System.out.println("⚠️ Port 8089 is still locked by a previous run. Using the existing connection...");
        } catch (Exception e) {
            System.err.println("❌ Unexpected Server Error: " + e.getMessage());
        }
    }

    @FXML
    private void openMapPicker() {
        try {
            // Wake up the listener
            startCoordServer();

            // Get the local file URL
            URL mapUrl = getClass().getResource("/html/map.html");
            if (mapUrl != null) {
                Desktop.getDesktop().browse(mapUrl.toURI());
            } else {
                showError("File map.html not found in /html/ directory!");
            }
        } catch (Exception e) {
            showError("Could not open system browser: " + e.getMessage());
        }
    }

    private void loadData() {
        displayEvents(eventService.findAll());
    }

    private void displayEvents(List<Event> events) {
        eventContainer.getChildren().clear();
        if (events.isEmpty()) {
            Label emptyLabel = new Label("No workshops found.");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");
            VBox empty = new VBox(emptyLabel);
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
            title.setStyle("-fx-text-fill: #1e293b;");

            Label desc = new Label(e.getDescription());
            desc.setWrapText(true);
            desc.setMaxWidth(500);
            desc.setStyle("-fx-text-fill: #64748b;");

            Label date = new Label("📅 " + e.getDate().format(formatter));
            date.setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: bold;");

            details.getChildren().addAll(title, desc, date);

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

        eventContainer.getChildren().forEach(node ->
                node.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: transparent;")
        );
        card.setStyle("-fx-background-color: #f0f9ff; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #0ea5e9; -fx-border-width: 2;");
    }

    @FXML private void handleAdd() {
        if (!validate()) return;
        Event e = new Event();
        fillEventData(e);
        eventService.add(e);
        loadData();
        handleClear();
    }

    @FXML private void handleUpdate() {
        if (selectedEvent != null && validate()) {
            fillEventData(selectedEvent);
            eventService.update(selectedEvent);
            loadData();
        }
    }

    @FXML private void handleDelete() {
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

    @FXML private void handleClear() {
        titleField.clear(); descField.clear(); latField.clear(); lngField.clear();
        datePicker.setValue(null); selectedEvent = null;
        loadData();
    }

    private void fillEventData(Event e) {
        e.setTitle(titleField.getText());
        e.setDescription(descField.getText());
        e.setDate(datePicker.getValue().atStartOfDay());
        try {
            e.setLatitude(Float.parseFloat(latField.getText()));
            e.setLongitude(Float.parseFloat(lngField.getText()));
        } catch (NumberFormatException ex) {
            e.setLatitude(0.0f);
            e.setLongitude(0.0f);
        }
    }

    private boolean validate() {
        if (titleField.getText().isEmpty() || datePicker.getValue() == null) {
            showError("Title and Date are required.");
            return false;
        }
        if (latField.getText().isEmpty() || lngField.getText().isEmpty()) {
            showError("Please select a location on the map.");
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