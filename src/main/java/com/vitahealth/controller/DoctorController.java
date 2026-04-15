package com.vitahealth.controller.doctor;

import com.vitahealth.dao.AppointmentDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.model.Appointment;
import com.vitahealth.model.User;
import com.vitahealth.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorController {

    @FXML private Label userLabel;
    @FXML private Button logoutBtn;

    @FXML private DatePicker dateFilter;
    @FXML private Button clearFilterBtn;
    @FXML private Button refreshBtn;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, LocalDateTime> colTime;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;

    @FXML private TextField searchPatientField;
    @FXML private TableView<User> patientsTable;
    @FXML private TableColumn<User, String> patColName;
    @FXML private TableColumn<User, String> patColEmail;
    @FXML private TableColumn<User, String> patColLastAppointment;
    @FXML private TableColumn<User, Void> patColActions;

    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private User currentUser;
    private ObservableList<Appointment> appointmentsList;
    private ObservableList<User> patientsList;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        appointmentDAO = new AppointmentDAO();
        userDAO = new UserDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            userLabel.setText("👨‍⚕️ Dr. " + currentUser.getFullName());
        }

        setupAppointmentsTable();
        setupPatientsTable();

        loadAppointments();
        loadPatients();

        dateFilter.setOnAction(e -> filterAppointmentsByDate());
        clearFilterBtn.setOnAction(e -> clearFilter());
        refreshBtn.setOnAction(e -> refreshAll());
        searchPatientField.textProperty().addListener((obs, old, val) -> searchPatients());
        logoutBtn.setOnAction(e -> logout());
    }

    private void setupAppointmentsTable() {
        colTime.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(timeFormatter));
            }
        });

        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
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
            private final Button confirmBtn = new Button("✓ Confirmer");
            private final Button cancelBtn = new Button("✗ Annuler");
            private final HBox buttons = new HBox(8, confirmBtn, cancelBtn);

            {
                confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;");
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;");
                confirmBtn.setOnAction(e -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    updateAppointmentStatus(app, "CONFIRMED");
                });
                cancelBtn.setOnAction(e -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    updateAppointmentStatus(app, "CANCELLED");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment app = getTableView().getItems().get(getIndex());
                    if ("SCHEDULED".equals(app.getStatus())) {
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void setupPatientsTable() {
        patColName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        patColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        patColLastAppointment.setCellValueFactory(new PropertyValueFactory<>("lastAppointment"));

        patColActions.setCellFactory(param -> new TableCell<>() {
            private final Button historyBtn = new Button("📋 Historique");
            {
                historyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;");
                historyBtn.setOnAction(e -> {
                    User patient = getTableView().getItems().get(getIndex());
                    showPatientHistory(patient);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : historyBtn);
            }
        });
    }

    private void loadAppointments() {
        appointmentsList = FXCollections.observableArrayList(appointmentDAO.getAppointmentsByDoctor(currentUser.getId()));
        appointmentsTable.setItems(appointmentsList);
    }

    private void filterAppointmentsByDate() {
        LocalDate date = dateFilter.getValue();
        if (date != null) {
            List<Appointment> filtered = appointmentsList.stream()
                    .filter(a -> a.getDate() != null && a.getDate().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            appointmentsTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void clearFilter() {
        dateFilter.setValue(null);
        appointmentsTable.setItems(appointmentsList);
    }

    private void loadPatients() {
        List<Appointment> appointments = appointmentDAO.getAppointmentsByDoctor(currentUser.getId());
        List<User> patients = appointments.stream()
                .map(a -> {
                    User u = new User();
                    u.setId(a.getPatientId());
                    u.setEmail("");
                    String[] nameParts = a.getPatientName() != null ? a.getPatientName().split(" ") : new String[]{"", ""};
                    u.setFirstName(nameParts[0]);
                    u.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                    u.setRole("PATIENT");
                    return u;
                })
                .distinct()
                .collect(Collectors.toList());
        patientsList = FXCollections.observableArrayList(patients);
        patientsTable.setItems(patientsList);
    }

    private void searchPatients() {
        String keyword = searchPatientField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            patientsTable.setItems(patientsList);
        } else {
            List<User> filtered = patientsList.stream()
                    .filter(p -> p.getFullName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            patientsTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void updateAppointmentStatus(Appointment appointment, String status) {
        if (appointmentDAO.updateAppointmentStatus(appointment.getId(), status)) {
            showAlert("Succès", "Rendez-vous " + (status.equals("CONFIRMED") ? "confirmé" : "annulé"), Alert.AlertType.INFORMATION);
            refreshAll();
        }
    }

    private void showPatientHistory(User patient) {
        List<Appointment> patientAppointments = appointmentDAO.getAppointmentsByPatient(patient.getId());
        StringBuilder history = new StringBuilder();
        for (Appointment app : patientAppointments) {
            history.append("- ").append(app.getDate().format(dateFormatter))
                    .append(" : ").append(app.getReason())
                    .append(" (").append(app.getStatus()).append(")\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historique du patient");
        alert.setHeaderText("Historique des rendez-vous de " + patient.getFullName());
        alert.setContentText(history.length() > 0 ? history.toString() : "Aucun rendez-vous");
        alert.showAndWait();
    }

    private void refreshAll() {
        loadAppointments();
        loadPatients();
        dateFilter.setValue(null);
        searchPatientField.clear();
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