package tn.esprit.workshopjdbc;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.workshopjdbc.Controllers.LoginController;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // We initialize his LoginController and use his custom Scene builder
        LoginController loginController = new LoginController();

        primaryStage.setScene(loginController.getScene());
        primaryStage.setTitle("VitaHealth - Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}