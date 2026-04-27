package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.EventService;
import tn.esprit.workshopjdbc.Utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserEventController {

    @FXML private FlowPane eventFlowPane;
    @FXML private TextField searchField;
    @FXML private Label welcomeLabel;
    @FXML private Label descriptionLabel;

    private EventService eventService = new EventService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd");

    @FXML
    public void initialize() {
        // 1. Session check using the verified SessionManager
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Nos événements");
        }

        // 2. CSS Priority Fix: Force visibility because style.css makes labels white
        String forceDark = "-fx-text-fill: #1e293b !important;";
        if (welcomeLabel != null) {
            welcomeLabel.setStyle(forceDark + "-fx-font-weight: bold;");
        }
        if (descriptionLabel != null) {
            descriptionLabel.setStyle("-fx-text-fill: #64748b !important;");
        }
        if (searchField != null) {
            searchField.setStyle("-fx-text-fill: #1e293b; -fx-prompt-text-fill: #94a3b8; -fx-background-radius: 20;");
        }

        loadEvents();

        // 3. Search Listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, newVal) -> {
                displayEvents(eventService.searchByTitleOrDescription(newVal));
            });
        }
    }

    private void loadEvents() {
        displayEvents(eventService.findAll());
    }

    private void displayEvents(List<Event> events) {
        if (eventFlowPane == null) return;
        eventFlowPane.getChildren().clear();
        for (Event e : events) {
            VBox card = createEventCard(e);
            eventFlowPane.getChildren().add(card);
        }
    }

    private VBox createEventCard(Event e) {
        VBox card = new VBox(15);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: #FFFFFF !important; " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 25; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1;");

        Label category = new Label("HEALTH EVENT");
        category.setStyle("-fx-text-fill: #94a3b8 !important; -fx-font-weight: bold; -fx-font-size: 10px;");

        Label title = new Label(e.getTitle());
        title.setStyle("-fx-text-fill: #1e293b !important; -fx-font-weight: bold; -fx-font-size: 18px;");
        title.setWrapText(true);
        title.setMinHeight(50);

        Label date = new Label("📅 " + (e.getDate() != null ? e.getDate().format(formatter) : "Upcoming"));
        date.setStyle("-fx-text-fill: #0d9488 !important; -fx-font-weight: bold;");

        Label desc = new Label(e.getDescription());
        desc.setStyle("-fx-text-fill: #475569 !important; -fx-font-size: 13px; -fx-opacity: 1.0;");
        desc.setWrapText(true);
        desc.setMaxHeight(60);
        desc.setMinHeight(60);

        Button registerBtn = new Button("Register Now");
        registerBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        registerBtn.setOnAction(event -> handleNavigationToForm(e));

        HBox footer = new HBox(registerBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(category, title, date, desc, new Separator(), footer);
        return card;
    }

    private void handleNavigationToForm(Event e) {
        openModal("/fxml/event/EventRegistration.fxml", "Registration: " + e.getTitle(), e);
    }

    @FXML
    private void handleOpenMyParticipations() {
        openModal("/fxml/event/MyParticipationsView.fxml", "My Bookings", null);
    }

    /**
     * Helper method to open any FXML as a Popup Modal
     */
    private void openModal(String fxmlPath, String title, Event e) {
        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);
            if (fxmlLocation == null) {
                System.err.println("❌ FXML not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // If it's the registration form, pass the event
            if (e != null && loader.getController() instanceof EventRegistrationController) {
                EventRegistrationController regController = loader.getController();
                regController.setEvent(e);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}