package tn.esprit.workshopjdbc.Controllers;

import tn.esprit.workshopjdbc.dao.ParaMedicalDAO;
import tn.esprit.workshopjdbc.dao.PrescriptionDAO;
import tn.esprit.workshopjdbc.dao.UserDAO;
import tn.esprit.workshopjdbc.dao.AppointmentDAO;
import tn.esprit.workshopjdbc.Entities.Appointment;
import tn.esprit.workshopjdbc.Entities.ParaMedical;
import tn.esprit.workshopjdbc.Entities.Prescription;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.WhatsAppReminderService;
import tn.esprit.workshopjdbc.Utils.NotificationManager;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.Utils.ThemeManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorController {

    // Composants FXML
    @FXML private Label userLabel;
    @FXML private Button logoutBtn;
    @FXML private Button themeToggleBtn;

    // Agenda
    @FXML private DatePicker dateFilter;
    @FXML private Button clearFilterBtn;
    @FXML private Button refreshBtn;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, LocalDateTime> colTime;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;

    // Patients
    @FXML private TextField searchPatientField;
    @FXML private TableView<User> patientsTable;
    @FXML private TableColumn<User, String> patColName;
    @FXML private TableColumn<User, String> patColEmail;
    @FXML private TableColumn<User, String> patColLastAppointment;
    @FXML private TableColumn<User, Void> patColActions;

    // Prescriptions
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

    // Paramètres médicaux
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
    @FXML private StackPane forumContainer;

    private UserDAO userDAO;
    private AppointmentDAO appointmentDAO;
    private PrescriptionDAO prescriptionDAO;
    private ParaMedicalDAO paraMedicalDAO;
    private WhatsAppReminderService whatsAppReminderService;
    private User currentUser;
    private ObservableList<User> allPatients;
    private ObservableList<Appointment> doctorAppointments;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        appointmentDAO = new AppointmentDAO();
        prescriptionDAO = new PrescriptionDAO();
        paraMedicalDAO = new ParaMedicalDAO();
        whatsAppReminderService = new WhatsAppReminderService();

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Dr. " + currentUser.getFullName());
        }

        setupAppointmentTable();
        setupPatientsTable();
        setupPrescriptionTable();
        setupParametreTable();
        loadDoctorAppointments();
        loadAllPatients();

        // Actions
        if (searchPatientField != null) {
            searchPatientField.textProperty().addListener((obs, old, val) -> filterPatients());
        }
        if (dateFilter != null) {
            dateFilter.valueProperty().addListener((obs, old, val) -> loadDoctorAppointments());
        }
        if (clearFilterBtn != null) {
            clearFilterBtn.setOnAction(e -> {
                dateFilter.setValue(null);
                loadDoctorAppointments();
            });
        }
        if (refreshBtn != null) {
            refreshBtn.setOnAction(e -> loadDoctorAppointments());
        }

        loadPatientPrescBtn.setOnAction(e -> loadPrescriptionsForPatient());
        refreshPrescListBtn.setOnAction(e -> loadPrescriptionsForPatient());
        ajouterPrescriptionBtn.setOnAction(e -> ajouterPrescription());
        modifierPrescriptionBtn.setOnAction(e -> modifierPrescription());
        supprimerPrescriptionBtn.setOnAction(e -> supprimerPrescription());
        viderPrescChampsBtn.setOnAction(e -> viderChampsPrescription());

        loadPatientParamBtn.setOnAction(e -> loadParametresForPatient());
        refreshParamListBtn.setOnAction(e -> loadParametresForPatient());

        logoutBtn.setOnAction(e -> logout());

        // Converters pour combos
        patientPrescCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
            @Override public User fromString(String s) { return null; }
        });
        patientParamCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
            @Override public User fromString(String s) { return null; }
        });
        loadForumModule();
    }

    private void setupAppointmentTable() {
        if (appointmentsTable == null) return;

        colTime.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button confirmBtn = new Button("Confirmer");
            private final Button doneBtn = new Button("Terminer");
            private final Button cancelBtn = new Button("Annuler");
            private final Button whatsAppBtn = new Button("WhatsApp");
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, confirmBtn, doneBtn, cancelBtn, whatsAppBtn);
            {
                confirmBtn.setOnAction(e -> updateAppointmentStatus("CONFIRMED"));
                doneBtn.setOnAction(e -> updateAppointmentStatus("COMPLETED"));
                cancelBtn.setOnAction(e -> updateAppointmentStatus("CANCELLED"));
                whatsAppBtn.setOnAction(e -> sendWhatsAppReminder());
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
            private void updateAppointmentStatus(String status) {
                Appointment app = getTableView().getItems().get(getIndex());
                if (appointmentDAO.updateAppointmentStatus(app.getId(), status)) {
                    loadDoctorAppointments();
                }
            }

            private void sendWhatsAppReminder() {
                Appointment app = getTableView().getItems().get(getIndex());
                try {
                    User patient = userDAO.findById(app.getPatientId());
                    WhatsAppReminderService.ReminderResult result = whatsAppReminderService.sendAppointmentReminder(app, patient, currentUser);
                    NotificationManager.Type type = result.sent()
                            ? NotificationManager.Type.SUCCESS
                            : result.dryRun() ? NotificationManager.Type.INFO : NotificationManager.Type.ERROR;
                    NotificationManager.showToast(logoutBtn != null ? logoutBtn.getScene() : null,
                            "Rappel WhatsApp", result.message(), type);
                } catch (Exception ex) {
                    NotificationManager.showToast(logoutBtn != null ? logoutBtn.getScene() : null,
                            "Rappel WhatsApp", "Impossible de charger le patient: " + ex.getMessage(), NotificationManager.Type.ERROR);
                }
            }
        });
    }

    private void setupPatientsTable() {
        if (patientsTable == null) return;

        patColName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        patColEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        patColLastAppointment.setCellValueFactory(data -> new SimpleStringProperty(getLastAppointmentLabel(data.getValue().getId())));
        patColActions.setCellFactory(column -> new TableCell<>() {
            private final Button selectBtn = new Button("Selectionner");
            {
                selectBtn.setOnAction(e -> {
                    User patient = getTableView().getItems().get(getIndex());
                    patientPrescCombo.setValue(patient);
                    patientParamCombo.setValue(patient);
                    loadPrescriptionsForPatient();
                    loadParametresForPatient();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : selectBtn);
            }
        });
    }

    private void loadForumModule() {
        if (forumContainer == null) return;

        try {
            Parent forumView = FXMLLoader.load(getClass().getResource("/fxml/forum/ForumView.fxml"));
            forumContainer.getChildren().setAll(forumView);
        } catch (Exception e) {
            System.err.println("Could not load forum module: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupPrescriptionTable() {
        colPrescDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colMedicaments.setCellValueFactory(new PropertyValueFactory<>("medicationList"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colInstructions.setCellValueFactory(new PropertyValueFactory<>("instructions"));
        colPrescDate.setCellFactory(column -> new TableCell<Prescription, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
    }

    private void setupParametreTable() {
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
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        colImc.setCellFactory(column -> new TableCell<ParaMedical, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
    }

    private void loadAllPatients() {
        try {
            List<User> patients = userDAO.findByRole("PATIENT");
            allPatients = FXCollections.observableArrayList(patients);
            patientPrescCombo.setItems(allPatients);
            patientParamCombo.setItems(allPatients);
            if (patientsTable != null) {
                patientsTable.setItems(allPatients);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterPatients() {
        if (allPatients == null) return;
        String keyword = searchPatientField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            patientsTable.setItems(allPatients);
            patientPrescCombo.setItems(allPatients);
            patientParamCombo.setItems(allPatients);
            return;
        }

        List<User> filtered = allPatients.stream()
                .filter(p -> contains(p.getFirstName(), keyword)
                        || contains(p.getLastName(), keyword)
                        || contains(p.getEmail(), keyword)
                        || contains(p.getMaladie(), keyword))
                .collect(Collectors.toList());
        ObservableList<User> filteredList = FXCollections.observableArrayList(filtered);
        patientsTable.setItems(filteredList);
        patientPrescCombo.setItems(filteredList);
        patientParamCombo.setItems(filteredList);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void loadDoctorAppointments() {
        if (currentUser == null || appointmentsTable == null) return;
        List<Appointment> appointments = appointmentDAO.getAppointmentsByDoctor(currentUser.getId());
        if (dateFilter != null && dateFilter.getValue() != null) {
            appointments = appointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().toLocalDate().equals(dateFilter.getValue()))
                    .collect(Collectors.toList());
        }
        doctorAppointments = FXCollections.observableArrayList(appointments);
        appointmentsTable.setItems(doctorAppointments);
    }

    private String getLastAppointmentLabel(int patientId) {
        if (doctorAppointments == null) return "-";
        return doctorAppointments.stream()
                .filter(a -> a.getPatientId() == patientId)
                .filter(a -> a.getDate() != null)
                .map(Appointment::getDate)
                .max(LocalDateTime::compareTo)
                .map(d -> d.format(formatter))
                .orElse("-");
    }

    private void loadPrescriptionsForPatient() {
        User patient = patientPrescCombo.getValue();
        if (patient == null) {
            showAlert("Info", "Selectionnez un patient", Alert.AlertType.INFORMATION);
            return;
        }
        List<Prescription> prescriptions = prescriptionDAO.getByPatientId(patient.getId());
        prescriptionTable.setItems(FXCollections.observableArrayList(prescriptions));
    }

    private void loadParametresForPatient() {
        User patient = patientParamCombo.getValue();
        if (patient == null) {
            showAlert("Info", "Selectionnez un patient", Alert.AlertType.INFORMATION);
            return;
        }
        List<ParaMedical> parametres = paraMedicalDAO.getByUserId(patient.getId());
        parametreTable.setItems(FXCollections.observableArrayList(parametres));
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
            showAlert("Succes", "Prescription ajoutee", Alert.AlertType.INFORMATION);
            loadPrescriptionsForPatient();
            viderChampsPrescription();
        } else {
            showAlert("Erreur", "Echec de l'ajout", Alert.AlertType.ERROR);
        }
    }

    private void modifierPrescription() {
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Selectionnez une prescription", Alert.AlertType.WARNING);
            return;
        }
        selected.setMedicationList(medicamentsArea.getText());
        selected.setInstructions(instructionsArea.getText());
        selected.setDuration(dureeField.getText());
        if (prescriptionDAO.modifier(selected)) {
            showAlert("Succes", "Prescription modifiee", Alert.AlertType.INFORMATION);
            loadPrescriptionsForPatient();
        } else {
            showAlert("Erreur", "Modification impossible", Alert.AlertType.ERROR);
        }
    }

    private void supprimerPrescription() {
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette prescription ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES && prescriptionDAO.supprimer(selected.getId())) {
                showAlert("Succes", "Prescription supprimee", Alert.AlertType.INFORMATION);
                loadPrescriptionsForPatient();
            }
        });
    }

    private void viderChampsPrescription() {
        medicamentsArea.clear();
        dureeField.clear();
        instructionsArea.clear();
    }

    private void logout() {
        SessionManager.getInstance().logout();
        LoginController loginController = new LoginController();
        Scene scene = loginController.getScene();
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("VitaHealthFX - Connexion");
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void toggleTheme() {
        Scene scene = logoutBtn != null ? logoutBtn.getScene() : null;
        ThemeManager.toggle(scene);
        if (themeToggleBtn != null) themeToggleBtn.setText(ThemeManager.toggleText());
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        NotificationManager.showAlert(title, msg, type);
    }
}
