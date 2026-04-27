package com.vitahealth;

import tn.esprit.workshopjdbc.Controllers.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        Scene scene = new LoginController().getScene();
        // ✅ Forcer le light mode uniquement
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("VitaHealthFX");
        stage.show();
    }

    public static void changeScene(Scene scene) {
        if (primaryStage != null) {
            // ✅ Forcer le light mode
            scene.getStylesheets().clear();
            scene.getStylesheets().add(App.class.getResource("/css/style-light.css").toExternalForm());
            primaryStage.setScene(scene);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}