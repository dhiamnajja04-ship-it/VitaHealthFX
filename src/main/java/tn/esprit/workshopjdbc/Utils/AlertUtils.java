package tn.esprit.workshopjdbc.Utils;

import javafx.scene.control.Alert;

public class AlertUtils {

    public static void showInfo(String title, String message) {
        NotificationManager.showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    public static void showError(String title, String message) {
        NotificationManager.showAlert(title, message, Alert.AlertType.ERROR);
    }
    
    public static void showWarning(String title, String message) {
        NotificationManager.showAlert(title, message, Alert.AlertType.WARNING);
    }
}
