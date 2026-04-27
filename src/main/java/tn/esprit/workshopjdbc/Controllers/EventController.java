package tn.esprit.workshopjdbc.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Services.EventService;
import java.io.IOException;

public class EventController {
    @FXML private TextField searchBar;
    @FXML private ListView<Event> eventList;

    private EventService eventService = new EventService();
    private ObservableList<Event> obsEvents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadDataFromDatabase();
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            obsEvents.setAll(eventService.searchByTitleOrDescription(newVal == null ? "" : newVal));
        });
    }

    private void loadDataFromDatabase() {
        obsEvents.setAll(eventService.findAll());
        eventList.setItems(obsEvents);
    }

    @FXML
    private void handleJoinEvent() {
        Event selected = eventList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            // FIXED: Ensure this path matches your file name exactly (e.g., EventRegistration.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event/EventRegistration.fxml"));
            Parent root = loader.load();

            EventRegistrationController regController = loader.getController();
            regController.setEvent(selected);

            // Access the contentArea from the current scene
            StackPane contentArea = (StackPane) eventList.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                System.err.println("Error: Could not find contentArea StackPane!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}