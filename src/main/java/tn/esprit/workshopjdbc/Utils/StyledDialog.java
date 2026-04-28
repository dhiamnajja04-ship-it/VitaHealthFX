package tn.esprit.workshopjdbc.Utils;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.dao.UserDAO;

public class StyledDialog {
    
    private static final String PRIMARY_COLOR = "#2c3e66";
    private static final String SECONDARY_COLOR = "#27ae60";
    private static final String BACKGROUND = "#f5f7fa";
    
    /**
     * Dialogue pour les statistiques utilisateur
     */
    public static void showUserStatsDialog(User user, double bmi, String bmiCategory) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Statistiques - " + user.getFullName());
        
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: " + BACKGROUND + "; -fx-background-radius: 10;");
        mainContainer.setPadding(new Insets(20));
        
        Label titleLabel = new Label("📊 " + user.getFirstName() + " " + user.getLastName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_COLOR + ";");
        
        Separator separator = new Separator();
        
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(10, 0, 10, 0));
        
        infoBox.getChildren().addAll(
            createInfoRow("📧 Email:", user.getEmail() != null ? user.getEmail() : "Non renseigné"),
            createInfoRow("🩺 Rôle:", user.getRole()),
            createInfoRow("✅ Vérifié:", user.isVerified() ? "Oui" : "Non"),
            createInfoRow("📱 Téléphone:", user.getPhone() != null ? user.getPhone() : "Non renseigné")
        );
        
        if ("PATIENT".equals(user.getRole())) {
            infoBox.getChildren().addAll(
                createInfoRow("⚖️ Poids:", user.getPoids() != null ? user.getPoids() + " kg" : "Non renseigné"),
                createInfoRow("📏 Taille:", user.getTaille() != null ? user.getTaille() + " m" : "Non renseigné"),
                createInfoRow("📊 IMC:", bmi > 0 ? String.format("%.1f", bmi) + " (" + bmiCategory + ")" : "Non calculé"),
                createInfoRow("🩸 Glycémie:", user.getGlycemie() != null ? user.getGlycemie() + " g/L" : "Non renseigné"),
                createInfoRow("💓 Tension:", user.getTension() != null ? user.getTension() : "Non renseignée")
            );
        } else if ("DOCTOR".equals(user.getRole())) {
            infoBox.getChildren().addAll(
                createInfoRow("🎓 Spécialité:", user.getSpecialite() != null ? user.getSpecialite() : "Non renseignée"),
                createInfoRow("📜 Diplôme:", user.getDiplome() != null ? user.getDiplome() : "Non renseigné")
            );
        }
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox(closeBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        mainContainer.getChildren().addAll(titleLabel, separator, infoBox, buttonBox);
        
        Scene scene = new Scene(mainContainer, 400, "PATIENT".equals(user.getRole()) ? 480 : 380);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    /**
     * Dialogue pour modifier un utilisateur
     */
    public static void showEditUserDialog(User user, UserDAO userDAO, Runnable onSave) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Modifier l'utilisateur");
        
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: " + BACKGROUND + "; -fx-background-radius: 10;");
        mainContainer.setPadding(new Insets(20));
        
        Label titleLabel = new Label("✏️ Modifier l'utilisateur");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_COLOR + ";");
        
        Separator separator = new Separator();
        
        VBox formBox = new VBox(12);
        
        HBox firstNameBox = createStyledField("Prénom", user.getFirstName());
        TextField firstNameField = (TextField) firstNameBox.getChildren().get(1);
        
        HBox lastNameBox = createStyledField("Nom", user.getLastName());
        TextField lastNameField = (TextField) lastNameBox.getChildren().get(1);
        
        HBox emailBox = createStyledField("Email", user.getEmail());
        TextField emailField = (TextField) emailBox.getChildren().get(1);
        
        HBox roleBox = new HBox(10);
        roleBox.setAlignment(Pos.CENTER_LEFT);
        Label roleLabel = new Label("Rôle");
        roleLabel.setPrefWidth(80);
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e66;");
        
        ComboBox<String> roleCombo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList("PATIENT", "DOCTOR", "ADMIN"));
        roleCombo.setValue(user.getRole());
        roleCombo.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 6;");
        
        roleBox.getChildren().addAll(roleLabel, roleCombo);
        HBox.setHgrow(roleCombo, Priority.ALWAYS);
        
        formBox.getChildren().addAll(firstNameBox, lastNameBox, emailBox, roleBox);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveBtn = new Button("💾 Enregistrer");
        Button cancelBtn = new Button("Annuler");
        
        saveBtn.setStyle("-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        
        saveBtn.setOnAction(e -> {
            user.setFirstName(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setRole(roleCombo.getValue());
            
            boolean ok = userDAO.updateUser(user);
            if (ok) {
                dialog.close();
                onSave.run();
                showToast("✅ Utilisateur modifié avec succès!");
            } else {
                errorLabel.setText("Erreur lors de la mise à jour.");
            }
        });
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        mainContainer.getChildren().addAll(titleLabel, separator, formBox, errorLabel, buttonBox);
        
        Scene scene = new Scene(mainContainer, 400, 380);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private static HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelField = new Label(label);
        labelField.setPrefWidth(80);
        labelField.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        
        Label valueField = new Label(value);
        valueField.setStyle("-fx-text-fill: #2c3e66; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        row.getChildren().addAll(labelField, valueField);
        HBox.setHgrow(valueField, Priority.ALWAYS);
        
        return row;
    }
    
    private static HBox createStyledField(String label, String defaultValue) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelField = new Label(label);
        labelField.setPrefWidth(80);
        labelField.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e66;");
        
        TextField field = new TextField(defaultValue);
        field.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 6;");
        
        row.getChildren().addAll(labelField, field);
        HBox.setHgrow(field, Priority.ALWAYS);
        
        return row;
    }
    
    private static void showToast(String message) {
        Stage toastStage = new Stage();
        toastStage.initModality(Modality.NONE);
        toastStage.setOpacity(0);
        
        VBox toast = new VBox();
        toast.setAlignment(Pos.CENTER);
        toast.setPadding(new Insets(10, 20, 10, 20));
        toast.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 8;");
        
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        toast.getChildren().add(messageLabel);
        
        Scene scene = new Scene(toast);
        scene.setFill(null);
        
        toastStage.setScene(scene);
        toastStage.show();
        
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> toastStage.close());
        delay.play();
    }
}