package tn.esprit.workshopjdbc.Utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public final class NotificationManager {
    public enum Type {
        SUCCESS, INFO, WARNING, ERROR
    }

    private NotificationManager() {}

    public static void showToast(Scene ownerScene, String title, String message, Type type) {
        Stage toastStage = new Stage(StageStyle.TRANSPARENT);
        toastStage.initModality(Modality.NONE);
        Window owner = ownerScene != null ? ownerScene.getWindow() : null;
        if (owner != null) {
            toastStage.initOwner(owner);
        }

        VBox toast = new VBox(4);
        toast.getStyleClass().addAll("app-toast", toastClass(type));
        toast.setPadding(new Insets(14, 18, 14, 18));
        toast.setMinWidth(310);

        Label titleLabel = new Label(title == null || title.isBlank() ? defaultTitle(type) : title);
        titleLabel.getStyleClass().add("app-toast-title");
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("app-toast-message");
        toast.getChildren().addAll(titleLabel, messageLabel);

        Scene scene = new Scene(toast);
        scene.setFill(null);
        ThemeManager.apply(scene);
        toastStage.setScene(scene);
        toastStage.setAlwaysOnTop(true);
        toastStage.show();

        if (owner != null) {
            toastStage.setX(owner.getX() + owner.getWidth() - toast.getMinWidth() - 32);
            toastStage.setY(owner.getY() + 84);
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
        toast.setOpacity(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        PauseTransition wait = new PauseTransition(Duration.seconds(type == Type.ERROR ? 3.5 : 2.4));
        wait.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toast);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(done -> toastStage.close());
            fadeOut.play();
        });
        wait.play();
    }

    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        styleAlert(alert, title, message);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        styleAlert(alert, title, message);
        alert.getButtonTypes().setAll(
                new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        return alert.showAndWait()
                .filter(button -> button.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .isPresent();
    }

    public static void styleAlert(Alert alert, String title, String message) {
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        DialogPane pane = alert.getDialogPane();
        pane.getStyleClass().add("app-dialog");
        Scene scene = pane.getScene();
        if (scene != null) {
            ThemeManager.apply(scene);
        }
    }

    private static String toastClass(Type type) {
        return switch (type) {
            case SUCCESS -> "app-toast-success";
            case WARNING -> "app-toast-warning";
            case ERROR -> "app-toast-error";
            default -> "app-toast-info";
        };
    }

    private static String defaultTitle(Type type) {
        return switch (type) {
            case SUCCESS -> "Succes";
            case WARNING -> "Attention";
            case ERROR -> "Erreur";
            default -> "Information";
        };
    }
}
