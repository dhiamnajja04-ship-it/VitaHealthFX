package com.vitahealth.controller.patient;

import com.vitahealth.dao.AppointmentDAO;
import com.vitahealth.dao.HealthProfileDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.model.Appointment;
import com.vitahealth.model.HealthProfile;
import com.vitahealth.model.User;
import com.vitahealth.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientController {

    @FXML private Label userLabel;
    @FXML private Button logoutBtn;

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, LocalDateTime> colDate;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;
    @FXML private Button newAppointmentBtn;
    @FXML private Button refreshAppointmentsBtn;

    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private ComboBox<String> bloodTypeCombo;
    @FXML private TextArea allergiesArea;
    @FXML private TextArea diseasesArea;
    @FXML private TextField emergencyContactField;
    @FXML private TextField emergencyPhoneField;
    @FXML private Label bmiLabel;
    @FXML private Button saveHealthBtn;
    @FXML private Button refreshHealthBtn;

    private AppointmentDAO appointmentDAO;
    private HealthProfileDAO healthProfileDAO;
    private UserDAO userDAO;
    private User currentUser;
    private ObservableList<Appointment> appointmentsList;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        appointmentDAO = new AppointmentDAO();
        healthProfileDAO = new HealthProfileDAO();
        userDAO = new UserDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            userLabel.setText("👤 " + currentUser.getFullName());
        }

        bloodTypeCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));

        // Configuration du tableau
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "SCHEDULED": setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); break;
                        case "CONFIRMED": setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); break;
                        case "COMPLETED": setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); break;
                        case "CANCELLED": setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); break;
                        default: setStyle("");
                    }
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("Annuler");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 15;");
                cancelBtn.setOnAction(e -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    cancelAppointment(app);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cancelBtn);
            }
        });

        loadAppointments();
        loadHealthProfile();

        heightField.textProperty().addListener((obs, old, val) -> calculateBMI());
        weightField.textProperty().addListener((obs, old, val) -> calculateBMI());

        newAppointmentBtn.setOnAction(e -> openNewAppointmentDialog());
        refreshAppointmentsBtn.setOnAction(e -> loadAppointments());
        saveHealthBtn.setOnAction(e -> saveHealthProfile());
        refreshHealthBtn.setOnAction(e -> loadHealthProfile());
        logoutBtn.setOnAction(e -> logout());
    }

    private void loadAppointments() {
        appointmentsList = FXCollections.observableArrayList(appointmentDAO.getAppointmentsByPatient(currentUser.getId()));
        appointmentsTable.setItems(appointmentsList);
    }

    private void loadHealthProfile() {
        HealthProfile profile = healthProfileDAO.getHealthProfileByUserId(currentUser.getId());
        if (profile != null) {
            heightField.setText(String.valueOf(profile.getHeight()));
            weightField.setText(String.valueOf(profile.getWeight()));
            bloodTypeCombo.setValue(profile.getBloodType());
            allergiesArea.setText(profile.getAllergies());
            diseasesArea.setText(profile.getChronicDiseases());
            emergencyContactField.setText(profile.getEmergencyContact());
            emergencyPhoneField.setText(profile.getEmergencyPhone());
            calculateBMI();
        }
    }

    private void calculateBMI() {
        try {
            double height = Double.parseDouble(heightField.getText());
            double weight = Double.parseDouble(weightField.getText());
            if (height > 0 && weight > 0) {
                double bmi = weight / ((height / 100) * (height / 100));
                bmiLabel.setText(String.format("%.1f", bmi));
                if (bmi < 18.5) bmiLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                else if (bmi < 25) bmiLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                else if (bmi < 30) bmiLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                else bmiLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        } catch (NumberFormatException e) {
            bmiLabel.setText("---");
        }
    }

    private void saveHealthProfile() {
        try {
            HealthProfile profile = new HealthProfile();
            profile.setUserId(currentUser.getId());
            profile.setHeight(Double.parseDouble(heightField.getText()));
            profile.setWeight(Double.parseDouble(weightField.getText()));
            profile.setBloodType(bloodTypeCombo.getValue());
            profile.setAllergies(allergiesArea.getText());
            profile.setChronicDiseases(diseasesArea.getText());
            profile.setEmergencyContact(emergencyContactField.getText());
            profile.setEmergencyPhone(emergencyPhoneField.getText());

            if (healthProfileDAO.saveOrUpdateHealthProfile(profile)) {
                showAlert("Succès", "Profil santé sauvegardé !", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des nombres valides", Alert.AlertType.ERROR);
        }
    }

    private void openNewAppointmentDialog() {
        List<User> doctors = appointmentDAO.getAllDoctors();
        if (doctors.isEmpty()) {
            showAlert("Info", "Aucun médecin disponible", Alert.AlertType.INFORMATION);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau rendez-vous");
        dialog.setHeaderText("Prendre un rendez-vous");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(350);

        ComboBox<User> doctorCombo = new ComboBox<>();
        doctorCombo.setItems(FXCollections.observableArrayList(doctors));
        doctorCombo.setPromptText("Choisir un médecin");
        doctorCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override public String toString(User user) { return user != null ? user.getFullName() : ""; }
            @Override public User fromString(String string) { return null; }
        });

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        ComboBox<String> hourCombo = new ComboBox<>();
        hourCombo.setItems(FXCollections.observableArrayList("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Motif de la consultation");
        reasonArea.setPrefHeight(80);

        content.getChildren().addAll(
                new Label("Médecin :"), doctorCombo,
                new Label("Date :"), datePicker,
                new Label("Heure :"), hourCombo,
                new Label("Motif :"), reasonArea
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && doctorCombo.getValue() != null && datePicker.getValue() != null && hourCombo.getValue() != null) {
                Appointment appointment = new Appointment();
                appointment.setPatientId(currentUser.getId());
                appointment.setDoctorId(doctorCombo.getValue().getId());
                LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(hourCombo.getValue() + ":00"));
                appointment.setDate(dateTime);
                appointment.setReason(reasonArea.getText());
                appointment.setStatus("SCHEDULED");

                if (appointmentDAO.createAppointment(appointment)) {
                    showAlert("Succès", "Rendez-vous pris !", Alert.AlertType.INFORMATION);
                    loadAppointments();
                } else {
                    showAlert("Erreur", "Impossible de prendre le rendez-vous", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cancelAppointment(Appointment appointment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler le rendez-vous ?");
        confirm.setContentText("Voulez-vous vraiment annuler ce rendez-vous ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && appointmentDAO.cancelAppointment(appointment.getId())) {
                showAlert("Succès", "Rendez-vous annulé", Alert.AlertType.INFORMATION);
                loadAppointments();
            }
        });
    }

    private void logout() {
        SessionManager.getInstance().logout();
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loginView, 450, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}