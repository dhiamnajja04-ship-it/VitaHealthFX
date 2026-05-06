package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Utilisation d'un chemin absolu depuis la racine des ressources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Erreur : Le fichier main-view.fxml est introuvable dans src/main/resources/");
            }

            Scene scene = new Scene(loader.load(), 1050, 750);

            // Ajout du fichier CSS
            String cssPath = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            stage.setTitle("VitalHealth - Système de Gestion Médicale");
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ Application démarrée avec succès.");
        } catch (Exception e) {
            System.err.println("❌ Erreur critique lors du chargement de l'application :");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}