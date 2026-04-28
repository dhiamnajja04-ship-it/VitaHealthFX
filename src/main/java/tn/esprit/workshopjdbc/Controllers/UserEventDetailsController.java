package tn.esprit.workshopjdbc.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.Event;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class UserEventDetailsController {
    @FXML private Label titleLabel, dateLabel, descLabel;
    @FXML private ImageView mapPreview;
    @FXML private Button registerBtn;

    @FXML private Label weatherLabel;
    @FXML private ImageView weatherIcon;

    private Event currentEvent;
    private final DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

    /**
     * Sets up the details view. Handles both local and Ticketmaster events.
     */
    public void setEventData(Event event) {
        this.currentEvent = event;
        titleLabel.setText(event.getTitle());
        descLabel.setText(event.getDescription());

        if (event.getDate() != null) {
            dateLabel.setText("📅 " + event.getDate().format(fullDateFormatter));
        }

        // --- DYNAMIC BUTTON LOGIC ---
        // If event has a URL, it's from Ticketmaster. Change text/style to show it's external.
        if (event.getUrl() != null && !event.getUrl().isEmpty()) {
            registerBtn.setText("Register on Ticketmaster ↗");
            registerBtn.setStyle("-fx-background-color: #026cdf; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 18;");
        } else {
            registerBtn.setText("Register Now");
            registerBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 18;");
        }

        // Map Preview (Works for both local and global coordinates)
        String lat = String.valueOf(event.getLatitude());
        String lng = String.valueOf(event.getLongitude());
        String mapUrl = String.format(
                "https://static-maps.yandex.ru/1.x/?lang=en_US&ll=%s,%s&z=15&l=map&size=450,300&pt=%s,%s,pm2rdm",
                lng, lat, lng, lat
        );

        Image img = new Image(mapUrl, true);
        mapPreview.setImage(img);

        Rectangle clip = new Rectangle(360, 220);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        mapPreview.setClip(clip);

        // Fetch Weather for the event location
        fetchWeather(event.getLatitude(), event.getLongitude());
    }

    /**
     * Logic for the Register button. Redirects to web for API events,
     * opens local modal for DB events.
     */
    @FXML
    private void handleRegistration() {
        if (currentEvent.getUrl() != null && !currentEvent.getUrl().isEmpty()) {
            // REDIRECT TO TICKETMASTER
            try {
                Desktop.getDesktop().browse(new URI(currentEvent.getUrl()));
            } catch (Exception e) {
                System.err.println("❌ Redirection error: " + e.getMessage());
            }
        } else {
            // LOCAL REGISTRATION MODAL
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event/EventRegistration.fxml"));
                Parent root = loader.load();

                EventRegistrationController regController = loader.getController();
                regController.setEvent(currentEvent);

                Stage stage = new Stage();
                stage.setTitle("Register: " + currentEvent.getTitle());
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException e) {
                System.err.println("❌ Local Registration error: " + e.getMessage());
            }
        }
    }

    private void fetchWeather(double lat, double lon) {
        String apiKey = "7517dbec48ff43a6e4fb81206a0604bb";
        String urlString = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                lat, lon, apiKey
        );

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == 200) {
                    Scanner sc = new Scanner(url.openStream());
                    StringBuilder response = new StringBuilder();
                    while (sc.hasNext()) response.append(sc.nextLine());
                    sc.close();

                    JSONObject data = new JSONObject(response.toString());
                    double temp = data.getJSONObject("main").getDouble("temp");
                    String description = data.getJSONArray("weather").getJSONObject(0).getString("description");
                    String iconCode = data.getJSONArray("weather").getJSONObject(0).getString("icon");

                    Platform.runLater(() -> {
                        if (weatherLabel != null) weatherLabel.setText(String.format("%.1f°C - %s", temp, description));
                        if (weatherIcon != null) {
                            weatherIcon.setImage(new Image("https://openweathermap.org/img/wn/" + iconCode + "@2x.png", true));
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> { if (weatherLabel != null) weatherLabel.setText("Weather unavailable"); });
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        try {
            // Determine which view to go back to (You can improve this by checking the event source)
            String fxmlPath = (currentEvent.getUrl() != null) ? "/fxml/event/GlobalEventView.fxml" : "/fxml/event/UserEventView.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent listView = loader.load();

            FlowPane grid = (FlowPane) titleLabel.getScene().lookup("#workshopGrid");
            if (grid != null) {
                if (listView instanceof Region) {
                    ((Region) listView).prefWidthProperty().bind(grid.widthProperty().subtract(50));
                }
                grid.getChildren().clear();
                grid.getChildren().add(listView);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleOpenInGoogleMaps() {
        try {
            String uri = String.format("https://www.google.com/maps/search/?api=1&query=%f,%f",
                    currentEvent.getLatitude(), currentEvent.getLongitude());
            Desktop.getDesktop().browse(new URI(uri));
        } catch (Exception e) { e.printStackTrace(); }
    }
}