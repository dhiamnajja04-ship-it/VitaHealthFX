package tn.esprit.workshopjdbc.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.workshopjdbc.Entities.Event;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class GlobalEventController {

    @FXML private FlowPane globalEventFlowPane;
    private final String TM_API_KEY = "3LbqS5jnZrQckiPZfIjH1UsezT20awQU";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd");

    @FXML
    public void initialize() {
        fetchGlobalEvents();
    }

    private void fetchGlobalEvents() {
        globalEventFlowPane.getChildren().clear();
        Label loadingLabel = new Label("Searching for global health events...");
        globalEventFlowPane.getChildren().add(loadingLabel);

        String urlString = "https://app.ticketmaster.com/discovery/v2/events.json?classificationName=health&apikey=" + TM_API_KEY;

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    Scanner sc = new Scanner(url.openStream());
                    StringBuilder res = new StringBuilder();
                    while (sc.hasNext()) res.append(sc.nextLine());
                    sc.close();

                    JSONObject json = new JSONObject(res.toString());
                    if (json.has("_embedded")) {
                        JSONArray eventsJson = json.getJSONObject("_embedded").getJSONArray("events");

                        Platform.runLater(() -> globalEventFlowPane.getChildren().clear());

                        for (int i = 0; i < eventsJson.length(); i++) {
                            Event e = mapJsonToEvent(eventsJson.getJSONObject(i));
                            Platform.runLater(() -> globalEventFlowPane.getChildren().add(createGlobalCard(e)));
                        }
                    } else {
                        Platform.runLater(() -> {
                            globalEventFlowPane.getChildren().clear();
                            globalEventFlowPane.getChildren().add(new Label("No global health events found."));
                        });
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    globalEventFlowPane.getChildren().clear();
                    globalEventFlowPane.getChildren().add(new Label("Error connecting to Ticketmaster."));
                });
            }
        }).start();
    }

    private Event mapJsonToEvent(JSONObject obj) {
        Event e = new Event();
        e.setTitle(obj.getString("name"));
        e.setDescription(obj.has("info") ? obj.getString("info") : "Global Event - View details on the website.");
        e.setUrl(obj.getString("url"));

        // Conversion: LocalDate String -> LocalDateTime
        if (obj.has("dates") && obj.getJSONObject("dates").has("start")) {
            String dateStr = obj.getJSONObject("dates").getJSONObject("start").getString("localDate");
            e.setDate(LocalDate.parse(dateStr).atStartOfDay());
        }

        if (obj.has("_embedded") && obj.getJSONObject("_embedded").has("venues")) {
            JSONArray venues = obj.getJSONObject("_embedded").getJSONArray("venues");
            if (venues.length() > 0) {
                JSONObject venue = venues.getJSONObject(0);
                if (venue.has("location")) {
                    JSONObject loc = venue.getJSONObject("location");

                    // FIX: Explicitly cast double to float for your Entity types
                    e.setLatitude((float) Double.parseDouble(loc.getString("latitude")));
                    e.setLongitude((float) Double.parseDouble(loc.getString("longitude")));
                }
            }
        }
        return e;
    }

    private VBox createGlobalCard(Event e) {
        VBox card = new VBox(12);
        card.setPrefWidth(300);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; " +
                "-fx-padding: 20; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #026cdf; -fx-border-width: 0 0 4 0; -fx-cursor: hand;");

        Label tag = new Label("WORLD EVENT");
        tag.setStyle("-fx-text-fill: #026cdf; -fx-font-weight: bold; -fx-font-size: 10px;");

        Label title = new Label(e.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1e293b;");
        title.setWrapText(true);
        title.setMinHeight(40);

        Label dateLabel = new Label("📅 " + (e.getDate() != null ? e.getDate().format(formatter) : "TBD"));
        dateLabel.setStyle("-fx-text-fill: #64748b;");

        Button btn = new Button("View Discovery Details");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #026cdf; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnAction(event -> handleViewDetails(e));

        card.getChildren().addAll(tag, title, dateLabel, new Separator(), btn);
        card.setOnMouseClicked(event -> handleViewDetails(e));

        return card;
    }

    private void handleViewDetails(Event e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event/UserEventDetails.fxml"));
            Parent root = loader.load();

            UserEventDetailsController controller = loader.getController();
            controller.setEventData(e);

            Pane grid = (Pane) globalEventFlowPane.getScene().lookup("#workshopGrid");
            if (grid != null) {
                grid.getChildren().setAll(root);
            } else {
                globalEventFlowPane.getScene().setRoot(root);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    private void handleBackToLocal() {
        try {
            // Load the local workshops view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event/UserEventView.fxml"));
            Parent localView = loader.load();

            // Find the main dashboard container to swap content back
            Pane grid = (Pane) globalEventFlowPane.getScene().lookup("#workshopGrid");

            if (grid != null) {
                grid.getChildren().setAll(localView);
            } else {
                // Fallback if lookup fails: try to access parent of current VBox
                VBox parent = (VBox) globalEventFlowPane.getParent();
                parent.getScene().setRoot(localView);
            }
        } catch (IOException e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }
}