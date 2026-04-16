package com.vitahealth.controller;

import com.vitahealth.dao.AppointmentDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.dao.ParaMedicalDAO;
import com.vitahealth.dao.PrescriptionDAO;
import com.vitahealth.entity.Appointment;
import com.vitahealth.entity.User;
import com.vitahealth.entity.ParaMedical;
import com.vitahealth.entity.Prescription;
import com.vitahealth.util.SessionManager;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorController {

    // ========== COMPOSANTS ==========
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

    @FXML private ComboBox<User> patientPrescCombo;
    @FXML private Button loadPatientPrescBtn;
    @FXML private Button refreshPrescListBtn;
    @FXML private TextArea medicamentsArea;
    @FXML private TextField dureeField;
    @FXML private TextArea instructionsArea;
    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, LocalDateTime> colPrescDate;
    @FXML private TableColumn<Prescription, String> colMedicaments;
    @FXML private TableColumn<Prescription, String> colDuree;
    @FXML private TableColumn<Prescription, String> colInstructions;
    @FXML private Button ajouterPrescriptionBtn;
    @FXML private Button modifierPrescriptionBtn;
    @FXML private Button supprimerPrescriptionBtn;
    @FXML private Button viderPrescChampsBtn;

    @FXML private ComboBox<User> patientParamCombo;
    @FXML private Button loadPatientParamBtn;
    @FXML private Button refreshParamListBtn;
    @FXML private TableView<ParaMedical> parametreTable;
    @FXML private TableColumn<ParaMedical, LocalDateTime> colParamDate;
    @FXML private TableColumn<ParaMedical, Double> colPoids;
    @FXML private TableColumn<ParaMedical, Double> colTaille;
    @FXML private TableColumn<ParaMedical, Double> colGlycemie;
    @FXML private TableColumn<ParaMedical, String> colTension;
    @FXML private TableColumn<ParaMedical, Double> colImc;
    @FXML private TableColumn<ParaMedical, String> colInterpretation;

    private AppointmentDAO appointmentDAO;
    private UserDAO userDAO;
    private ParaMedicalDAO paraMedicalDAO;
    private PrescriptionDAO prescriptionDAO;

    private User currentUser;
    private ObservableList<Appointment> appointmentsList;
    private ObservableList<User> patientsList;
    private ObservableList<Prescription> prescriptionsList;
    private ObservableList<ParaMedical> parametresList;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        appointmentDAO = new AppointmentDAO();
        userDAO = new UserDAO();
        paraMedicalDAO = new ParaMedicalDAO();
        prescriptionDAO = new PrescriptionDAO();

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("👨‍⚕️ Dr. " + currentUser.getFullName());
        }

        setupAppointmentsTable();
        setupPatientsTable();
        setupPrescriptions();
        setupParametres();

        loadAppointments();
        loadPatients();
        loadPatientsForCombos();

        dateFilter.setOnAction(e -> filterAppointmentsByDate());
        clearFilterBtn.setOnAction(e -> clearFilter());
        refreshBtn.setOnAction(e -> refreshAll());
        searchPatientField.textProperty().addListener((obs, old, val) -> searchPatients());

        applyButtonStyles();

        loadPatientPrescBtn.setOnAction(e -> loadPrescriptionsForPatient());
        refreshPrescListBtn.setOnAction(e -> loadPrescriptionsForPatient());
        ajouterPrescriptionBtn.setOnAction(e -> ajouterPrescription());
        modifierPrescriptionBtn.setOnAction(e -> modifierPrescription());
        supprimerPrescriptionBtn.setOnAction(e -> supprimerPrescription());
        viderPrescChampsBtn.setOnAction(e -> viderChampsPrescription());

        loadPatientParamBtn.setOnAction(e -> loadParametresForPatient());
        refreshParamListBtn.setOnAction(e -> loadParametresForPatient());

        logoutBtn.setOnAction(e -> logout());
    }

    private void applyButtonStyles() {
        refreshBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        clearFilterBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        loadPatientPrescBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshPrescListBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        loadPatientParamBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshParamListBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        ajouterPrescriptionBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        modifierPrescriptionBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        supprimerPrescriptionBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        viderPrescChampsBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");

        addHoverAnimation(refreshBtn);
        addHoverAnimation(clearFilterBtn);
        addHoverAnimation(loadPatientPrescBtn);
        addHoverAnimation(ajouterPrescriptionBtn);
        addHoverAnimation(modifierPrescriptionBtn);
        addHoverAnimation(supprimerPrescriptionBtn);
    }

    private void addHoverAnimation(Button btn) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void setupAppointmentsTable() {
        colTime.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(timeFormatter));
            }
        });
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Appointment, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
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
                confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");
                addHoverAnimation(confirmBtn);
                addHoverAnimation(cancelBtn);
                confirmBtn.setOnAction(e -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    updateAppointmentStatus(app, "CONFIRMED");
                });
                cancelBtn.setOnAction(e -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    updateAppointmentStatus(app, "CANCELLED");
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Appointment app = getTableView().getItems().get(getIndex());
                    setGraphic("SCHEDULED".equals(app.getStatus()) ? buttons : null);
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
                historyBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");
                addHoverAnimation(historyBtn);
                historyBtn.setOnAction(e -> {
                    User patient = getTableView().getItems().get(getIndex());
                    showPatientHistory(patient);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : historyBtn);
            }
        });
    }

    private void setupPrescriptions() {
        colPrescDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colMedicaments.setCellValueFactory(new PropertyValueFactory<>("medicationList"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colInstructions.setCellValueFactory(new PropertyValueFactory<>("instructions"));
        colPrescDate.setCellFactory(column -> new TableCell<Prescription, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
    }

    private void setupParametres() {
        colParamDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        colGlycemie.setCellValueFactory(new PropertyValueFactory<>("glycemie"));
        colTension.setCellValueFactory(new PropertyValueFactory<>("tensionSystolique"));
        colImc.setCellValueFactory(cellData -> {
            Double imc = cellData.getValue().getImc();
            return new SimpleObjectProperty<>(imc != null ? imc : 0.0);
        });
        colInterpretation.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getImcInterpretation()));
        colParamDate.setCellFactory(column -> new TableCell<ParaMedical, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
        colImc.setCellFactory(column -> new TableCell<ParaMedical, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
    }

    private void loadAppointments() {
        appointmentsList = FXCollections.observableArrayList(appointmentDAO.getAppointmentsByDoctor(currentUser.getId()));
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

    private void loadPatientsForCombos() {
        try {
            List<User> allPatients = userDAO.findByRole("PATIENT");
            ObservableList<User> patientOptions = FXCollections.observableArrayList(allPatients);
            patientPrescCombo.setItems(patientOptions);
            patientParamCombo.setItems(patientOptions);
            patientPrescCombo.setConverter(new javafx.util.StringConverter<User>() {
                @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
                @Override public User fromString(String s) { return null; }
            });
            patientParamCombo.setConverter(new javafx.util.StringConverter<User>() {
                @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
                @Override public User fromString(String s) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPrescriptionsForPatient() {
        User patient = patientPrescCombo.getValue();
        if (patient == null) {
            showAlert("Info", "Sélectionnez un patient", Alert.AlertType.INFORMATION);
            return;
        }
        List<Prescription> prescriptions = prescriptionDAO.getByPatientId(patient.getId());
        prescriptionsList = FXCollections.observableArrayList(prescriptions);
        prescriptionTable.setItems(prescriptionsList);
    }

    private void loadParametresForPatient() {
        User patient = patientParamCombo.getValue();
        if (patient == null) {
            showAlert("Info", "Sélectionnez un patient", Alert.AlertType.INFORMATION);
            return;
        }
        List<ParaMedical> parametres = paraMedicalDAO.getByUserId(patient.getId());
        parametresList = FXCollections.observableArrayList(parametres);
        parametreTable.setItems(parametresList);
    }

    private void ajouterPrescription() {
        User patient = patientPrescCombo.getValue();
        if (patient == null) {
            showAlert("Erreur", "Choisissez un patient", Alert.AlertType.ERROR);
            return;
        }
        Prescription p = new Prescription();
        p.setMedicationList(medicamentsArea.getText());
        p.setInstructions(instructionsArea.getText());
        p.setDuration(dureeField.getText());
        if (prescriptionDAO.ajouter(patient.getId(), currentUser.getId(), p)) {
            showAlert("✅ Succès", "Prescription ajoutée avec succès !", Alert.AlertType.INFORMATION);
            loadPrescriptionsForPatient();
            viderChampsPrescription();
        } else {
            showAlert("❌ Erreur", "Échec de l'ajout", Alert.AlertType.ERROR);
        }
    }

    private void modifierPrescription() {
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez une prescription", Alert.AlertType.WARNING);
            return;
        }
        selected.setMedicationList(medicamentsArea.getText());
        selected.setInstructions(instructionsArea.getText());
        selected.setDuration(dureeField.getText());
        if (prescriptionDAO.modifier(selected)) {
            showAlert("✅ Succès", "Prescription modifiée avec succès !", Alert.AlertType.INFORMATION);
            loadPrescriptionsForPatient();
        } else {
            showAlert("❌ Erreur", "Modification impossible", Alert.AlertType.ERROR);
        }
    }

    private void supprimerPrescription() {
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "⚠️ Supprimer cette prescription ?\nCette action est irréversible !",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES && prescriptionDAO.supprimer(selected.getId())) {
                showAlert("✅ Succès", "Prescription supprimée avec succès !", Alert.AlertType.INFORMATION);
                loadPrescriptionsForPatient();
            }
        });
    }

    private void viderChampsPrescription() {
        medicamentsArea.clear();
        dureeField.clear();
        instructionsArea.clear();
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

    private void updateAppointmentStatus(Appointment appointment, String status) {
        if (appointmentDAO.updateAppointmentStatus(appointment.getId(), status)) {
            showAlert("✅ Succès", "Rendez-vous " + (status.equals("CONFIRMED") ? "confirmé" : "annulé"), Alert.AlertType.INFORMATION);
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
        alert.setTitle("📋 Historique du patient");
        alert.setHeaderText("Historique des rendez-vous de " + patient.getFullName());

        TextArea textArea = new TextArea(history.toString());
        textArea.setEditable(false);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(500);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
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

    private void refreshAll() {
        loadAppointments();
        loadPatients();
        dateFilter.setValue(null);
        searchPatientField.clear();
        if (patientPrescCombo.getValue() != null) loadPrescriptionsForPatient();
        if (patientParamCombo.getValue() != null) loadParametresForPatient();
    }

    private void logout() {
        SessionManager.getInstance().logout();
        LoginController loginController = new LoginController();
        Scene scene = loginController.getScene();
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("VitaHealthFX - Connexion");
        stage.show();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}