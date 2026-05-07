package tn.esprit.workshopjdbc.Controllers;
import tn.esprit.workshopjdbc.Services.EmailService;
import tn.esprit.workshopjdbc.dao.ParaMedicalDAO;
import tn.esprit.workshopjdbc.dao.PrescriptionDAO;
import tn.esprit.workshopjdbc.dao.UserDAO;
import tn.esprit.workshopjdbc.dao.AppointmentDAO;
import tn.esprit.workshopjdbc.dao.SeuilDAO;
import tn.esprit.workshopjdbc.Entities.Appointment;
import tn.esprit.workshopjdbc.Entities.ParaMedical;
import tn.esprit.workshopjdbc.Entities.Prescription;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.WhatsAppReminderService;
import tn.esprit.workshopjdbc.Utils.NotificationManager;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.Utils.ThemeManager;
import tn.esprit.workshopjdbc.Services.PDFGenerator;
import tn.esprit.workshopjdbc.Services.MedicamentService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DoctorController {

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
    @FXML private Button refreshPrescListBtn;
    @FXML private TextArea medicamentsArea;
    @FXML private TextField dureeField;
    @FXML private TextArea instructionsArea;
    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, LocalDateTime> colPrescDate;
    @FXML private TableColumn<Prescription, String> colMedicaments;
    @FXML private TableColumn<Prescription, String> colDuree;
    @FXML private TableColumn<Prescription, String> colInstructions;
    @FXML private Button ouvrirFormPrescriptionBtn;
    @FXML private Button modifierPrescriptionBtn;   // peut être null si absent du FXML
    @FXML private Button supprimerPrescriptionBtn;
    @FXML private Button viderPrescChampsBtn;
    @FXML private Button genererPdfBtn;
    @FXML private TextField rechercheMedicamentField;
    @FXML private Button rechercherMedicamentBtn;
    @FXML private ListView<MedicamentService.Medicament> resultatsMedicaments;

    // Paramètres médicaux
    @FXML private ComboBox<User> patientParamCombo;
    @FXML private Button refreshParamListBtn;
    @FXML private Button showTrendsBtn;
    @FXML private TableView<ParaMedical> parametreTable;
    @FXML private TableColumn<ParaMedical, LocalDateTime> colParamDate;
    @FXML private TableColumn<ParaMedical, Double> colPoids;
    @FXML private TableColumn<ParaMedical, Double> colTaille;
    @FXML private TableColumn<ParaMedical, Double> colGlycemie;
    @FXML private TableColumn<ParaMedical, String> colTension;
    @FXML private TableColumn<ParaMedical, Double> colImc;
    @FXML private TableColumn<ParaMedical, String> colInterpretation;
    @FXML private TableColumn<ParaMedical, String> colAlerte;
    @FXML private TableColumn<ParaMedical, String> colNiveauActivite;

    @FXML private StackPane forumContainer;

    private UserDAO userDAO;
    private AppointmentDAO appointmentDAO;
    private PrescriptionDAO prescriptionDAO;
    private ParaMedicalDAO paraMedicalDAO;
    private WhatsAppReminderService whatsAppReminderService;
    private SeuilDAO seuilDAO;
    private User currentUser;
    private ObservableList<User> allPatients;
    private ObservableList<Appointment> doctorAppointments;
    private Map<String, Map<String, Double>> seuils;
    private int currentPatientId = -1;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        appointmentDAO = new AppointmentDAO();
        prescriptionDAO = new PrescriptionDAO();
        paraMedicalDAO = new ParaMedicalDAO();
        whatsAppReminderService = new WhatsAppReminderService();
        seuilDAO = new SeuilDAO();

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) userLabel.setText("Dr. " + currentUser.getFullName());

        seuils = seuilDAO.getSeuils();

        setupAppointmentTable();
        setupPatientsTable();
        setupPrescriptionTable();
        setupParametreTable();
        loadDoctorAppointments();
        loadAllPatients();

        // Agenda
        if (dateFilter != null) dateFilter.valueProperty().addListener((obs, old, val) -> loadDoctorAppointments());
        if (clearFilterBtn != null) clearFilterBtn.setOnAction(e -> { dateFilter.setValue(null); loadDoctorAppointments(); });
        if (refreshBtn != null) refreshBtn.setOnAction(e -> loadDoctorAppointments());
        if (searchPatientField != null) searchPatientField.textProperty().addListener((obs, old, val) -> filterPatients());

        // Prescriptions : chargement automatique à la sélection du patient
        if (patientPrescCombo != null) {
            patientPrescCombo.valueProperty().addListener((obs, old, newPatient) -> {
                System.out.println("PatientCombo changed: " + (newPatient != null ? newPatient.getFullName() : "null"));
                if (newPatient != null) loadPrescriptionsForPatient();
            });
        }
        if (refreshPrescListBtn != null) refreshPrescListBtn.setOnAction(e -> loadPrescriptionsForPatient());

        // ✅ CORRECTION 1 : tous les boutons prescription protégés par null-check
        if (ouvrirFormPrescriptionBtn != null) ouvrirFormPrescriptionBtn.setOnAction(e -> ouvrirDialogPrescription());
        if (modifierPrescriptionBtn != null) modifierPrescriptionBtn.setOnAction(e -> ajouterOuModifierPrescription());
        if (supprimerPrescriptionBtn != null) supprimerPrescriptionBtn.setOnAction(e -> supprimerPrescription());
        if (viderPrescChampsBtn != null)     viderPrescChampsBtn.setOnAction(e -> viderChampsPrescription());
        if (genererPdfBtn != null)           genererPdfBtn.setOnAction(e -> genererPDF());
        if (rechercherMedicamentBtn != null) rechercherMedicamentBtn.setOnAction(e -> rechercherMedicament());

        if (resultatsMedicaments != null) {
            resultatsMedicaments.setOnMouseClicked(event -> {
                MedicamentService.Medicament selected = resultatsMedicaments.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String current = medicamentsArea.getText();
                    String newMed = selected.toString();
                    if (current.isEmpty()) medicamentsArea.setText(newMed);
                    else medicamentsArea.setText(current + "\n" + newMed);
                    resultatsMedicaments.getItems().clear();
                    rechercheMedicamentField.clear();
                }
            });
        }

        // Paramètres médicaux – chargement automatique
        if (patientParamCombo != null) {
            patientParamCombo.valueProperty().addListener((obs, old, newPatient) -> {
                if (newPatient != null) loadParametresForPatient();
            });
        }
        if (refreshParamListBtn != null) refreshParamListBtn.setOnAction(e -> loadParametresForPatient());
        if (showTrendsBtn != null)       showTrendsBtn.setOnAction(e -> showTrendsForSelectedPatient());

        if (logoutBtn != null)      logoutBtn.setOnAction(e -> logout());
        if (themeToggleBtn != null) themeToggleBtn.setOnAction(e -> toggleTheme());

        if (patientPrescCombo != null) {
            patientPrescCombo.setConverter(new javafx.util.StringConverter<User>() {
                @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
                @Override public User fromString(String s) { return null; }
            });
        }
        if (patientParamCombo != null) {
            patientParamCombo.setConverter(new javafx.util.StringConverter<User>() {
                @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
                @Override public User fromString(String s) { return null; }
            });
        }

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
                confirmBtn.setOnAction(e -> updateStatus("CONFIRMED"));
                doneBtn.setOnAction(e -> updateStatus("COMPLETED"));
                cancelBtn.setOnAction(e -> updateStatus("CANCELLED"));
                whatsAppBtn.setOnAction(e -> sendWhatsAppReminder());
            }
            private void updateStatus(String status) {
                Appointment app = getTableView().getItems().get(getIndex());
                if (appointmentDAO.updateAppointmentStatus(app.getId(), status)) loadDoctorAppointments();
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
            @Override protected void updateItem(Void item, boolean empty) { setGraphic(empty ? null : box); }
        });
    }
    private void ouvrirDialogPrescription() {
        User patient = patientPrescCombo != null ? patientPrescCombo.getValue() : null;
        if (patient == null) {
            showAlert("Attention", "Veuillez d'abord sélectionner un patient.", Alert.AlertType.WARNING);
            return;
        }
        try {
            java.net.URL fxmlUrl = getClass().getResource("/fxml/AjoutPrescriptionView.fxml");
            if (fxmlUrl == null) {
                showAlert("Erreur", "Fichier FXML introuvable.", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent form = loader.load();
            NouvellePreescriptionController formCtrl = loader.getController();

            // ✅ Utiliser un Stage au lieu d'un Dialog — fermeture fiable
            Stage formStage = new Stage();
            formStage.setTitle("Nouvelle Prescription — " + patient.getFullName());
            formStage.setScene(new Scene(form, 700, 750));
            formStage.initOwner(ouvrirFormPrescriptionBtn.getScene().getWindow());
            formStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            formStage.setResizable(false);

            // ✅ Retour = simplement fermer la fenêtre
            formCtrl.setOnAnnuler(formStage::close);

            formCtrl.setOnValider((med, dur, ins) -> {
                Prescription p = new Prescription();
                p.setMedicationList(med);
                p.setDuration(dur);
                p.setInstructions(ins);
                if (prescriptionDAO.ajouter(patient.getId(), currentUser.getId(), p)) {
                    showAlert("Succès", "Prescription ajoutée avec succès.", Alert.AlertType.INFORMATION);
                    loadPrescriptionsForPatient();
                } else {
                    showAlert("Erreur", "Échec de l'ajout de la prescription.", Alert.AlertType.ERROR);
                }
                // ✅ Ferme le stage après validation aussi
                formStage.close();
            });

            formStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupPatientsTable() {
        if (patientsTable == null) return;
        patColName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        patColEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        patColLastAppointment.setCellValueFactory(data -> new SimpleStringProperty(getLastAppointmentLabel(data.getValue().getId())));
        patColActions.setCellFactory(column -> new TableCell<>() {
            private final Button selectBtn = new Button("Sélectionner");
            {
                selectBtn.setOnAction(e -> {
                    User patient = getTableView().getItems().get(getIndex());
                    if (patientPrescCombo != null) patientPrescCombo.setValue(patient);
                    if (patientParamCombo != null) patientParamCombo.setValue(patient);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) { setGraphic(empty ? null : selectBtn); }
        });
    }

    // ✅ CORRECTION 2 : loadDoctorAppointments protégée contre l'exception SQL
    private void loadDoctorAppointments() {
        if (currentUser == null || appointmentsTable == null) return;
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByDoctor(currentUser.getId());
            if (dateFilter != null && dateFilter.getValue() != null) {
                appointments = appointments.stream()
                        .filter(a -> a.getDate() != null && a.getDate().toLocalDate().equals(dateFilter.getValue()))
                        .collect(Collectors.toList());
            }
            doctorAppointments = FXCollections.observableArrayList(appointments);
            appointmentsTable.setItems(doctorAppointments);
        } catch (Exception e) {
            System.err.println("Erreur chargement rendez-vous : " + e.getMessage());
            // ⚠️ Vérifiez que la table s'appelle bien 'appointments' dans votre BDD
            // SHOW TABLES FROM vitahealth;
            doctorAppointments = FXCollections.observableArrayList();
            appointmentsTable.setItems(doctorAppointments);
        }
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

    private void loadAllPatients() {
        try {
            List<User> patients = userDAO.findByRole("PATIENT");
            allPatients = FXCollections.observableArrayList(patients);
            if (patientPrescCombo != null) patientPrescCombo.setItems(allPatients);
            if (patientParamCombo != null) patientParamCombo.setItems(allPatients);
            if (patientsTable != null)     patientsTable.setItems(allPatients);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void filterPatients() {
        if (allPatients == null) return;
        String keyword = searchPatientField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            patientsTable.setItems(allPatients);
            if (patientPrescCombo != null) patientPrescCombo.setItems(allPatients);
            if (patientParamCombo != null) patientParamCombo.setItems(allPatients);
            return;
        }
        List<User> filtered = allPatients.stream()
                .filter(p -> contains(p.getFirstName(), keyword) || contains(p.getLastName(), keyword)
                        || contains(p.getEmail(), keyword) || contains(p.getMaladie(), keyword))
                .collect(Collectors.toList());
        ObservableList<User> filteredList = FXCollections.observableArrayList(filtered);
        patientsTable.setItems(filteredList);
        if (patientPrescCombo != null) patientPrescCombo.setItems(filteredList);
        if (patientParamCombo != null) patientParamCombo.setItems(filteredList);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void setupPrescriptionTable() {
        if (prescriptionTable == null) return;

        // ✅ Activer l'édition sur le tableau
        prescriptionTable.setEditable(true);

        colPrescDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colPrescDate.setCellFactory(column -> new TableCell<Prescription, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        // ✅ Colonne Médicaments — éditable au clic
        colMedicaments.setCellValueFactory(new PropertyValueFactory<>("medicationList"));
        colMedicaments.setCellFactory(col -> {
            TableCell<Prescription, String> cell = new TableCell<>() {
                private final TextArea textArea = new TextArea();
                private final Label label = new Label();
                {
                    textArea.setWrapText(true);
                    textArea.setPrefRowCount(3);
                    textArea.setStyle("-fx-font-size: 12px;");

                    // Valider en appuyant sur Ctrl+Enter ou en perdant le focus
                    textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            commitEdit(textArea.getText());
                        }
                    });
                    textArea.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
                    });
                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    textArea.setText(getItem());
                    setGraphic(textArea);
                    setText(null);
                    textArea.requestFocus();
                    textArea.selectAll();
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }

                @Override
                public void commitEdit(String newVal) {
                    super.commitEdit(newVal);
                    Prescription p = getTableView().getItems().get(getIndex());
                    p.setMedicationList(newVal);
                    if (prescriptionDAO.modifier(p)) {
                        setText(newVal);
                    } else {
                        showAlert("Erreur", "Échec de la modification.", Alert.AlertType.ERROR);
                    }
                    setGraphic(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else if (isEditing()) { setGraphic(textArea); setText(null); }
                    else { setText(item); setGraphic(null); }
                }
            };
            // ✅ Double-clic pour éditer
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) cell.startEdit();
            });
            return cell;
        });
        colMedicaments.setOnEditCommit(e -> {});

        // ✅ Colonne Durée — éditable au clic
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colDuree.setCellFactory(col -> {
            TableCell<Prescription, String> cell = new TableCell<>() {
                private final TextField textField = new TextField();
                {
                    textField.setStyle("-fx-font-size: 12px;");
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            commitEdit(textField.getText());
                        }
                    });
                    textField.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ENTER)  commitEdit(textField.getText());
                        if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
                    });
                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    textField.setText(getItem());
                    setGraphic(textField);
                    setText(null);
                    textField.requestFocus();
                    textField.selectAll();
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }

                @Override
                public void commitEdit(String newVal) {
                    super.commitEdit(newVal);
                    Prescription p = getTableView().getItems().get(getIndex());
                    p.setDuration(newVal);
                    if (prescriptionDAO.modifier(p)) {
                        setText(newVal);
                    } else {
                        showAlert("Erreur", "Échec de la modification.", Alert.AlertType.ERROR);
                    }
                    setGraphic(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else if (isEditing()) { setGraphic(textField); setText(null); }
                    else { setText(item); setGraphic(null); }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) cell.startEdit();
            });
            return cell;
        });
        colDuree.setOnEditCommit(e -> {});

        // ✅ Colonne Instructions — éditable au clic
        colInstructions.setCellValueFactory(new PropertyValueFactory<>("instructions"));
        colInstructions.setCellFactory(col -> {
            TableCell<Prescription, String> cell = new TableCell<>() {
                private final TextArea textArea = new TextArea();
                {
                    textArea.setWrapText(true);
                    textArea.setPrefRowCount(3);
                    textArea.setStyle("-fx-font-size: 12px;");
                    textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            commitEdit(textArea.getText());
                        }
                    });
                    textArea.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
                    });
                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    textArea.setText(getItem());
                    setGraphic(textArea);
                    setText(null);
                    textArea.requestFocus();
                    textArea.selectAll();
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                }

                @Override
                public void commitEdit(String newVal) {
                    super.commitEdit(newVal);
                    Prescription p = getTableView().getItems().get(getIndex());
                    p.setInstructions(newVal);
                    if (prescriptionDAO.modifier(p)) {
                        setText(newVal);
                    } else {
                        showAlert("Erreur", "Échec de la modification.", Alert.AlertType.ERROR);
                    }
                    setGraphic(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else if (isEditing()) { setGraphic(textArea); setText(null); }
                    else { setText(item); setGraphic(null); }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) cell.startEdit();
            });
            return cell;
        });
        colInstructions.setOnEditCommit(e -> {});

        System.out.println("Prescription table setup complete");
    }

    private void loadPrescriptionsForPatient() {
        if (patientPrescCombo == null) return;
        User patient = patientPrescCombo.getValue();
        System.out.println("loadPrescriptionsForPatient - patient: " + (patient != null ? patient.getFullName() : "null"));
        if (patient == null) {
            if (prescriptionTable != null) prescriptionTable.setItems(FXCollections.observableArrayList());
            return;
        }
        List<Prescription> list = prescriptionDAO.getByPatientId(patient.getId());
        System.out.println("Found " + list.size() + " prescriptions for patient " + patient.getFullName());
        if (prescriptionTable != null) prescriptionTable.setItems(FXCollections.observableArrayList(list));
    }

    private void ajouterOuModifierPrescription() {
        if (patientPrescCombo == null) return;
        User patient = patientPrescCombo.getValue();
        if (patient == null) {
            showAlert("Erreur", "Choisissez un patient", Alert.AlertType.ERROR);
            return;
        }
        String med = medicamentsArea != null ? medicamentsArea.getText() : "";
        String dur = dureeField != null ? dureeField.getText() : "";
        String ins = instructionsArea != null ? instructionsArea.getText() : "";
        if (med.isEmpty() || dur.isEmpty() || ins.isEmpty()) {
            showAlert("Info", "Remplissez tous les champs", Alert.AlertType.WARNING);
            return;
        }
        Prescription selected = prescriptionTable != null ? prescriptionTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            selected.setMedicationList(med);
            selected.setDuration(dur);
            selected.setInstructions(ins);
            if (prescriptionDAO.ajouter(patient.getId(), currentUser.getId(), selected)) {

                EmailService.envoyerNotificationPrescription(
                        patient,
                        currentUser,
                        selected,
                        false
                );

                showAlert("Succès", "Prescription ajoutée", Alert.AlertType.INFORMATION);

                loadPrescriptionsForPatient();

                viderChampsPrescription();
            }else showAlert("Erreur", "Échec modification", Alert.AlertType.ERROR);
        } else {
            Prescription p = new Prescription();
            p.setMedicationList(med);
            p.setInstructions(ins);
            p.setDuration(dur);
            if (prescriptionDAO.ajouter(patient.getId(), currentUser.getId(), p)) {
                showAlert("Succès", "Prescription ajoutée", Alert.AlertType.INFORMATION);
                loadPrescriptionsForPatient();
                viderChampsPrescription();
            } else showAlert("Erreur", "Échec ajout", Alert.AlertType.ERROR);
        }
    }

    private void supprimerPrescription() {
        if (prescriptionTable == null) return;
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez une prescription", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette prescription ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES && prescriptionDAO.supprimer(selected.getId())) {
                showAlert("Succès", "Prescription supprimée", Alert.AlertType.INFORMATION);
                loadPrescriptionsForPatient();
                viderChampsPrescription();
                prescriptionTable.getSelectionModel().clearSelection();
            }
        });
    }

    private void genererPDF() {
        if (prescriptionTable == null) return;
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez une prescription", Alert.AlertType.WARNING);
            return;
        }
        User patient = patientPrescCombo != null ? patientPrescCombo.getValue() : null;
        if (patient == null) {
            showAlert("Erreur", "Patient non sélectionné", Alert.AlertType.ERROR);
            return;
        }
        ChoiceDialog<String> langDialog = new ChoiceDialog<>("fr", "fr", "en", "ar");
        langDialog.setTitle("Langue du PDF");
        langDialog.setHeaderText("Choisissez la langue pour la traduction");
        langDialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        String lang = langDialog.showAndWait().orElse(null);
        if (lang == null) return;

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("prescription_" + selected.getId() + "_" + patient.getFullName().replace(" ", "_") + ".pdf");
        File file = fc.showSaveDialog(null);
        if (file == null) return;

        Alert progress = new Alert(Alert.AlertType.INFORMATION, "Génération en cours...", ButtonType.CANCEL);
        progress.setTitle("PDF");
        progress.setHeaderText(null);
        progress.show();

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                PDFGenerator.generatePrescriptionPDF(selected, patient, currentUser, file.getAbsolutePath(), lang);
                return null;
            }
        };
        task.setOnSucceeded(e -> { progress.close(); showAlert("Succès", "PDF généré", Alert.AlertType.INFORMATION); });
        task.setOnFailed(e -> { progress.close(); showAlert("Erreur", task.getException().getMessage(), Alert.AlertType.ERROR); });
        new Thread(task).start();
    }

    private void rechercherMedicament() {
        if (rechercheMedicamentField == null) return;
        String query = rechercheMedicamentField.getText().trim();
        if (query.isEmpty()) {
            showAlert("Info", "Saisissez un nom de médicament", Alert.AlertType.INFORMATION);
            return;
        }
        if (resultatsMedicaments != null) {
            resultatsMedicaments.getItems().clear();
            resultatsMedicaments.setPlaceholder(new Label("Recherche en cours..."));
        }

        MedicamentService service = new MedicamentService();
        service.rechercher(query).thenAccept(medicaments -> {
            Platform.runLater(() -> {
                if (resultatsMedicaments == null) return;
                if (medicaments.isEmpty()) {
                    System.out.println("API BDPM n'a retourné aucun résultat. Utilisation du fallback local.");
                    List<MedicamentService.Medicament> fallback = getLocalMedicaments(query);
                    if (fallback.isEmpty()) {
                        showAlert("Info", "Aucun médicament trouvé (API indisponible)", Alert.AlertType.INFORMATION);
                        resultatsMedicaments.setPlaceholder(new Label("Aucun résultat"));
                        resultatsMedicaments.getItems().clear();
                        return;
                    }
                    resultatsMedicaments.getItems().setAll(fallback);
                } else {
                    resultatsMedicaments.getItems().setAll(medicaments);
                }
                resultatsMedicaments.setCellFactory(lv -> new ListCell<MedicamentService.Medicament>() {
                    @Override protected void updateItem(MedicamentService.Medicament item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.toString());
                    }
                });
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                if (resultatsMedicaments == null) return;
                System.err.println("Erreur API: " + ex.getMessage());
                List<MedicamentService.Medicament> fallback = getLocalMedicaments(query);
                if (fallback.isEmpty()) {
                    showAlert("Erreur", "Impossible de contacter l'API médicaments", Alert.AlertType.ERROR);
                    resultatsMedicaments.setPlaceholder(new Label("Erreur de connexion"));
                } else {
                    resultatsMedicaments.getItems().setAll(fallback);
                    resultatsMedicaments.setCellFactory(lv -> new ListCell<MedicamentService.Medicament>() {
                        @Override protected void updateItem(MedicamentService.Medicament item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : item.toString());
                        }
                    });
                }
            });
            return null;
        });
    }

    private List<MedicamentService.Medicament> getLocalMedicaments(String query) {
        List<MedicamentService.Medicament> list = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        String[][] meds = {
                {"DOLIPRANE", "500 mg"}, {"DOLIPRANE", "1000 mg"},
                {"EFFERALGAN", "500 mg"}, {"EFFERALGAN", "1000 mg"},
                {"PARACETAMOL", "500 mg"}, {"PARACETAMOL", "1000 mg"},
                {"ASPIRINE", "300 mg"}, {"ASPIRINE", "500 mg"},
                {"IBUPROFENE", "200 mg"}, {"IBUPROFENE", "400 mg"},
                {"ADVIL", "200 mg"}, {"NUROFEN", "200 mg"},
                {"AMOXICILLINE", "500 mg"}, {"AMOXICILLINE", "1 g"},
                {"AUGMENTIN", "500 mg"}, {"AUGMENTIN", "1 g"},
                {"AZITHROMYCINE", "250 mg"}, {"AZITHROMYCINE", "500 mg"},
                {"DOXYCYCLINE", "100 mg"}, {"CLARITHROMYCINE", "500 mg"}
        };
        for (String[] m : meds) {
            if (m[0].toLowerCase().contains(lowerQuery)) {
                MedicamentService.Medicament med = new MedicamentService.Medicament();
                med.setNom(m[0]);
                med.setDosage(m[1]);
                list.add(med);
            }
        }
        return list;
    }

    private void viderChampsPrescription() {
        if (medicamentsArea != null)       medicamentsArea.clear();
        if (dureeField != null)            dureeField.clear();
        if (instructionsArea != null)      instructionsArea.clear();
        if (rechercheMedicamentField != null) rechercheMedicamentField.clear();
        if (resultatsMedicaments != null) {
            resultatsMedicaments.getItems().clear();
            resultatsMedicaments.setPlaceholder(new Label(""));
        }
        if (prescriptionTable != null) prescriptionTable.getSelectionModel().clearSelection();
    }

    private void setupParametreTable() {
        if (parametreTable == null) return;
        colParamDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        colGlycemie.setCellValueFactory(new PropertyValueFactory<>("glycemie"));
        colTension.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTensionSystolique()));
        colImc.setCellValueFactory(cellData -> {
            Double imc = cellData.getValue().getImc();
            return new SimpleObjectProperty<>(imc != null ? imc : 0.0);
        });
        colInterpretation.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImcInterpretation()));
        colAlerte.setCellValueFactory(cellData -> new SimpleStringProperty(verifierAlerte(cellData.getValue())));
        colAlerte.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item);
                    if (item.startsWith("⚠️")) setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: green;");
                }
            }
        });
        colNiveauActivite.setCellValueFactory(cellData -> new SimpleStringProperty(getNiveauActivitePourPatient()));
        colNiveauActivite.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item);
                    if (item.contains("Actif"))      setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    else if (item.contains("Moyen")) setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    else if (item.contains("Non actif")) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else setStyle("");
                }
            }
        });
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

    private void loadParametresForPatient() {
        if (patientParamCombo == null) return;
        User patient = patientParamCombo.getValue();
        if (patient == null) {
            if (parametreTable != null) parametreTable.setItems(FXCollections.observableArrayList());
            currentPatientId = -1;
            return;
        }
        currentPatientId = patient.getId();
        if (parametreTable != null) {
            parametreTable.setItems(FXCollections.observableArrayList(paraMedicalDAO.getByUserId(patient.getId())));
            parametreTable.refresh();
        }
    }

    private String verifierAlerte(ParaMedical pm) {
        StringBuilder sb = new StringBuilder();
        double gly = pm.getGlycemie();
        Map<String, Double> glySeuil = seuils.get("glycemie");
        if (glySeuil != null && (gly < glySeuil.get("min") || gly > glySeuil.get("max"))) sb.append("⚠️ Glycémie anormale ");
        try {
            int tens = Integer.parseInt(pm.getTensionSystolique());
            Map<String, Double> tensSeuil = seuils.get("tension");
            if (tensSeuil != null && (tens < tensSeuil.get("min") || tens > tensSeuil.get("max"))) sb.append("⚠️ Tension anormale ");
        } catch (NumberFormatException ignored) {}
        double imc = pm.getImc();
        Map<String, Double> imcSeuil = seuils.get("imc");
        if (imcSeuil != null && (imc < imcSeuil.get("min") || imc > imcSeuil.get("max"))) sb.append("⚠️ IMC anormal ");
        return sb.length() > 0 ? sb.toString() : "✓ Normal";
    }

    private String getNiveauActivitePourPatient() {
        if (currentPatientId == -1) return "";
        List<ParaMedical> liste = paraMedicalDAO.getByUserId(currentPatientId);
        if (liste.isEmpty()) return "Non actif (aucune saisie)";
        LocalDateTime derniere = liste.stream()
                .map(ParaMedical::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (derniere == null) return "Non actif";
        long jours = ChronoUnit.DAYS.between(derniere, LocalDateTime.now());
        if (jours <= 7)  return "Actif (dernière saisie ≤ 7 jours)";
        if (jours <= 30) return "Moyen (dernière saisie entre 8 et 30 jours)";
        return "Non actif (plus de 30 jours sans saisie)";
    }

    private void showTrendsForSelectedPatient() {
        if (patientParamCombo == null) return;
        User patient = patientParamCombo.getValue();
        if (patient == null) {
            showAlert("Info", "Sélectionnez un patient", Alert.AlertType.INFORMATION);
            return;
        }
        List<ParaMedical> data = paraMedicalDAO.getByUserId(patient.getId());
        if (data.isEmpty()) {
            showAlert("Info", "Aucune donnée médicale", Alert.AlertType.INFORMATION);
            return;
        }
        data.sort(Comparator.comparing(ParaMedical::getCreatedAt));
        Stage stage = new Stage();
        stage.setTitle("Tendances - " + patient.getFullName());
        TabPane tp = new TabPane();
        tp.getTabs().addAll(
                new Tab("Glycémie", createLineChart(data, "glycemie", "Glycémie (mmol/L)")),
                new Tab("IMC",      createLineChart(data, "imc",      "IMC (kg/m²)")),
                new Tab("Tension",  createLineChart(data, "tension",  "Tension systolique"))
        );
        stage.setScene(new Scene(tp, 800, 600));
        stage.show();
    }

    private LineChart<String, Number> createLineChart(List<ParaMedical> data, String type, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel(yLabel);
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valeurs");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
        for (ParaMedical pm : data) {
            String date = pm.getCreatedAt().format(df);
            Number val = 0;
            switch (type) {
                case "glycemie": val = pm.getGlycemie(); break;
                case "imc":      val = pm.getImc(); break;
                case "tension":
                    try { val = Integer.parseInt(pm.getTensionSystolique()); } catch (Exception e) { val = 0; }
                    break;
            }
            series.getData().add(new XYChart.Data<>(date, val));
        }
        chart.getData().add(series);
        return chart;
    }

    private void loadForumModule() {
        if (forumContainer == null) return;
        try {
            Parent forumView = FXMLLoader.load(getClass().getResource("/fxml/forum/ForumView.fxml"));
            forumContainer.getChildren().setAll(forumView);
        } catch (IOException e) {
            System.err.println("Could not load forum module: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logout() {
        SessionManager.getInstance().logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(login, 450, 600));
            stage.setTitle("Connexion");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
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
