package utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class NotificationUtils {

    public static void showSuccess(String title, String text) {
        Platform.runLater(() -> {
            Notifications.create()
                    .title(title)
                    .text(text)
                    .hideAfter(Duration.seconds(4))
                    .position(Pos.BOTTOM_RIGHT)
                    .showInformation();
        });
    }

    public static void showError(String title, String text) {
        Platform.runLater(() -> {
            Notifications.create()
                    .title(title)
                    .text(text)
                    .hideAfter(Duration.seconds(6))
                    .position(Pos.BOTTOM_RIGHT)
                    .showError();
        });
    }
    
    public static void showWarning(String title, String text) {
        Platform.runLater(() -> {
            Notifications.create()
                    .title(title)
                    .text(text)
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.BOTTOM_RIGHT)
                    .showWarning();
        });
    }
}
