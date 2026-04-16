package com.vitahealth;

import com.vitahealth.controller.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setScene(new LoginController().getScene());
        stage.setTitle("VitaHealthFX");
        stage.show();
    }

    public static void changeScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}