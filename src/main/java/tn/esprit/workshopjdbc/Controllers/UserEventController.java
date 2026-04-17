package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.EventService;
import tn.esprit.workshopjdbc.Utils.UserSession;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserEventController {

    @FXML private FlowPane eventFlowPane;
    @FXML private TextField searchField;
    @FXML private Label welcomeLabel;

    private EventService eventService = new EventService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd");

    @FXML
    public void initialize() {
        User currentUser = UserSession.getSession();
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Hello " + currentUser.getFirstName() + " 👋");
        }

        loadEvents();

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
        eventFlowPane.getChildren().clear();
        for (Event e : events) {
            VBox card = createEventCard(e);
            eventFlowPane.getChildren().add(card);
        }
    }

    private VBox createEventCard(Event e) {
        VBox card = new VBox(15);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 25; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5);");

        Label category = new Label("HEALTH EVENT");
        category.setFont(Font.font("System", FontWeight.BOLD, 10));
        category.setTextFill(Color.web("#94a3b8"));

        Label title = new Label(e.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#1e293b"));
        title.setWrapText(true);
        title.setMinHeight(50);

        Label date = new Label("📅 " + (e.getDate() != null ? e.getDate().format(formatter) : "Upcoming"));
        date.setTextFill(Color.web("#0d9488"));
        date.setStyle("-fx-font-weight: bold;");

        Label desc = new Label(e.getDescription());
        desc.setTextFill(Color.web("#64748b"));
        desc.setWrapText(true);
        desc.setMaxHeight(60);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button registerBtn = new Button("Register Now");
        registerBtn.setStyle("-fx-background-color: #0d9488; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand;");

        // --- THE REDIRECT LOGIC ---
        registerBtn.setOnAction(event -> handleNavigationToForm(e));

        footer.getChildren().add(registerBtn);
        card.getChildren().addAll(category, title, date, desc, new Separator(), footer);

        return card;
    }

    /**
     * This method handles the redirection to the Registration Form
     */
    private void handleNavigationToForm(Event e) {
        try {
            // 1. Load the FXML for the registration form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventRegistration.fxml"));
            Parent root = loader.load();

            // 2. Get the form's controller and pass the selected Event
            EventRegistrationController regController = loader.getController();
            regController.setEvent(e);

            // 3. Find the contentArea StackPane from the scene and swap views
            StackPane contentArea = (StackPane) eventFlowPane.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                // Fallback if lookup fails: log error
                System.err.println("❌ Navigation Error: Could not find contentArea StackPane.");
            }

        } catch (IOException ex) {
            System.err.println("❌ Failed to load EventRegistration.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}