package tn.esprit.vitahealthfx;

import tn.esprit.vitahealthfx.controller.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setScene(new LoginController().getScene());
        stage.setTitle("🏥 VITA - Plateforme Médicale");
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}