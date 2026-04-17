package tn.esprit.workshopjdbc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.UserSession;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // --- PREVENTING AUTH FOR TESTING ---
        User godModeUser = new User();
        godModeUser.setId(8);
        godModeUser.setFirstName("admin");
        godModeUser.setLastName("admin");
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_ADMIN"); // Gives you CRUD powers
        godModeUser.setRoles(roles);
        godModeUser.setMaladie("Diabetes"); // Gives you Patient fields
        UserSession.setSession(godModeUser);
        // ------------------------------------

        Parent root = FXMLLoader.load(getClass().getResource("/MainDashboard.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("VitaHealth - Event Management (Admin Mode)");
        primaryStage.show();
    }
}