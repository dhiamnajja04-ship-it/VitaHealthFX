package tn.esprit.workshopjdbc.Controllers;

import javafx.geometry.VPos;
import javafx.scene.layout.FlowPane;
import tn.esprit.workshopjdbc.dao.AppointmentDAO;
import tn.esprit.workshopjdbc.dao.HealthProfileDAO;
import tn.esprit.workshopjdbc.dao.ParaMedicalDAO;
import tn.esprit.workshopjdbc.dao.PrescriptionDAO;
import tn.esprit.workshopjdbc.dao.UserDAO;
import tn.esprit.workshopjdbc.dao.SeuilDAO;
import tn.esprit.workshopjdbc.Entities.Appointment;
import tn.esprit.workshopjdbc.Entities.HealthProfile;
import tn.esprit.workshopjdbc.Entities.ParaMedical;
import tn.esprit.workshopjdbc.Entities.Prescription;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.NotificationManager;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.Utils.ThemeManager;
import tn.esprit.workshopjdbc.Services.PDFGenerator;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PatientController {

    @FXML private Label userLabel;
    @FXML private Button logoutBtn;
    @FXML private Button themeToggleBtn;

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, LocalDateTime> colDate;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;
    @FXML private Button newAppointmentBtn;
    @FXML private Button refreshAppointmentsBtn;
    @FXML private TextField appointmentSearchField;
    @FXML private ComboBox<String> appointmentStatusFilter;
    @FXML private Pagination appointmentPagination;

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

    @FXML private TextField poidsField;
    @FXML private TextField tailleField;
    @FXML private TextField glycemieField;
    @FXML private TextField tensionField;
    @FXML private Label imcCalculLabel;
    @FXML private Label interpretationLabel;
    @FXML private Button ajouterParametreBtn;
    @FXML private Button modifierParametreBtn;
    @FXML private Button supprimerParametreBtn;
    @FXML private Button rafraichirParametresBtn;
    @FXML private TableView<ParaMedical> parametreTable;
    @FXML private TableColumn<ParaMedical, LocalDateTime> colParamDate;
    @FXML private TableColumn<ParaMedical, Double> colParamPoids;
    @FXML private TableColumn<ParaMedical, Double> colParamTaille;
    @FXML private TableColumn<ParaMedical, Double> colParamGlycemie;
    @FXML private TableColumn<ParaMedical, String> colParamTension;
    @FXML private TableColumn<ParaMedical, Double> colParamImc;
    @FXML private TableColumn<ParaMedical, String> colAlerte;
    @FXML private Label niveauActiviteLabel;

    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, LocalDateTime> colPrescDate;
    @FXML private TableColumn<Prescription, String> colMedicaments;
    @FXML private TableColumn<Prescription, String> colDuree;
    @FXML private TableColumn<Prescription, String> colInstructions;
    @FXML private TableColumn<Prescription, Void> colActionsPresc;
    @FXML private Button refreshPrescriptionsBtn;

    @FXML private MenuItem changerVersMedecin;
    @FXML private MenuItem quitter;
    @FXML private MenuItem aPropos;
    @FXML private FlowPane workshopGrid;
    @FXML private TextField searchWorkshopField;
    @FXML private StackPane forumContainer;
    @FXML private Button showTrendsBtn;

    private AppointmentDAO appointmentDAO;
    private HealthProfileDAO healthProfileDAO;
    private UserDAO userDAO;
    private ParaMedicalDAO paraMedicalDAO;
    private PrescriptionDAO prescriptionDAO;
    private SeuilDAO seuilDAO;

    private User currentUser;
    private ObservableList<Appointment> appointmentsList;
    private List<Appointment> currentAppointmentPageSource = List.of();
    private ObservableList<ParaMedical> parametresList;
    private ObservableList<Prescription> prescriptionsList;
    private Map<String, Map<String, Double>> seuils;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int PATIENT_ROWS_PER_PAGE = 8;

    @FXML
    public void initialize() {
        appointmentDAO = new AppointmentDAO();
        healthProfileDAO = new HealthProfileDAO();
        userDAO = new UserDAO();
        paraMedicalDAO = new ParaMedicalDAO();
        prescriptionDAO = new PrescriptionDAO();
        seuilDAO = new SeuilDAO();

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) userLabel.setText("👤 " + currentUser.getFullName());
        else userLabel.setText("👤 Patient");

        seuils = seuilDAO.getSeuils();

        setupAppointmentsTable();
        setupHealthProfile();
        setupParametresTable();
        setupPrescriptionsTable();

        loadAppointments();
        loadHealthProfile();
        loadParametres();
        loadPrescriptions();

        newAppointmentBtn.setOnAction(e -> openNewAppointmentDialog());
        refreshAppointmentsBtn.setOnAction(e -> {
            if (appointmentSearchField != null) appointmentSearchField.clear();
            if (appointmentStatusFilter != null) appointmentStatusFilter.setValue("Tous");
            loadAppointments();
        });
        saveHealthBtn.setOnAction(e -> saveHealthProfile());
        refreshHealthBtn.setOnAction(e -> loadHealthProfile());
        if (refreshPrescriptionsBtn != null) refreshPrescriptionsBtn.setOnAction(e -> loadPrescriptions());
        if (appointmentSearchField != null) appointmentSearchField.textProperty().addListener((obs, old, val) -> applyAppointmentFilters());
        if (appointmentStatusFilter != null) appointmentStatusFilter.valueProperty().addListener((obs, old, val) -> applyAppointmentFilters());
        if (appointmentPagination != null) appointmentPagination.currentPageIndexProperty().addListener((obs, old, val) -> updateAppointmentPage());

        ajouterParametreBtn.setOnAction(e -> ajouterOuModifierParametre());
        supprimerParametreBtn.setOnAction(e -> supprimerParametre());
        rafraichirParametresBtn.setOnAction(e -> loadParametres());
        if (showTrendsBtn != null) showTrendsBtn.setOnAction(e -> showTrendsForPatient(currentUser.getId()));

        logoutBtn.setOnAction(e -> logout());
        if (changerVersMedecin != null) changerVersMedecin.setOnAction(e -> changerVersMedecin());
        if (quitter != null) quitter.setOnAction(e -> System.exit(0));
        if (aPropos != null) aPropos.setOnAction(e -> showAlert("À propos", "VitaHealthFX - Version 2.0\nEspace Patient\nPDF multilingue, graphiques, alertes", Alert.AlertType.INFORMATION));

        loadExternalEventSystem();
        loadForumModule();
        updateNiveauActivite();
    }

    // ================== RENDEZ-VOUS ==================
    private void setupAppointmentsTable() {
        if (appointmentStatusFilter != null) {
            appointmentStatusFilter.setItems(FXCollections.observableArrayList("Tous", "SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED"));
            appointmentStatusFilter.setValue("Tous");
        }
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
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
            private final Button cancelBtn = new Button("Annuler");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 15; -fx-cursor: hand;");
                cancelBtn.setOnAction(e -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    cancelAppointment(app);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : cancelBtn);
            }
        });
    }

    private void loadAppointments() {
        appointmentsList = FXCollections.observableArrayList(appointmentDAO.getAppointmentsByPatient(currentUser.getId()));
        applyAppointmentFilters();
    }

    private void applyAppointmentFilters() {
        if (appointmentsList == null) { currentAppointmentPageSource = List.of(); updateAppointmentPagination(); return; }
        String keyword = appointmentSearchField == null ? "" : appointmentSearchField.getText().trim().toLowerCase();
        String status = appointmentStatusFilter == null ? "Tous" : appointmentStatusFilter.getValue();
        List<Appointment> filtered = appointmentsList.stream()
                .filter(a -> keyword.isEmpty() || contains(a.getDoctorName(), keyword) || contains(a.getReason(), keyword) || contains(a.getStatus(), keyword))
                .filter(a -> status == null || "Tous".equals(status) || status.equalsIgnoreCase(a.getStatus()))
                .toList();
        currentAppointmentPageSource = filtered;
        updateAppointmentPagination();
    }

    private void updateAppointmentPagination() {
        if (appointmentPagination == null) {
            if (appointmentsTable != null) appointmentsTable.setItems(FXCollections.observableArrayList(currentAppointmentPageSource));
            return;
        }
        int pageCount = Math.max(1, (int) Math.ceil(currentAppointmentPageSource.size() / (double) PATIENT_ROWS_PER_PAGE));
        appointmentPagination.setPageCount(pageCount);
        if (appointmentPagination.getCurrentPageIndex() >= pageCount) appointmentPagination.setCurrentPageIndex(0);
        updateAppointmentPage();
    }

    private void updateAppointmentPage() {
        if (appointmentsTable == null) return;
        int page = appointmentPagination == null ? 0 : appointmentPagination.getCurrentPageIndex();
        int from = Math.min(page * PATIENT_ROWS_PER_PAGE, currentAppointmentPageSource.size());
        int to = Math.min(from + PATIENT_ROWS_PER_PAGE, currentAppointmentPageSource.size());
        appointmentsTable.setItems(FXCollections.observableArrayList(currentAppointmentPageSource.subList(from, to)));
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void openNewAppointmentDialog() {
        List<User> doctors = appointmentDAO.getAllDoctors();
        if (doctors.isEmpty()) { showAlert("Info", "Aucun médecin disponible", Alert.AlertType.INFORMATION); return; }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau rendez-vous");
        dialog.setHeaderText("Prendre un rendez-vous");
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        ComboBox<User> doctorCombo = new ComboBox<>(FXCollections.observableArrayList(doctors));
        doctorCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
            @Override public User fromString(String s) { return null; }
        });
        DatePicker datePicker = new DatePicker();
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        ComboBox<String> hourCombo = new ComboBox<>(FXCollections.observableArrayList("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));
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
                Appointment app = new Appointment();
                app.setPatientId(currentUser.getId());
                app.setDoctorId(doctorCombo.getValue().getId());
                LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(hourCombo.getValue() + ":00"));
                app.setDate(dateTime);
                app.setReason(reasonArea.getText());
                app.setStatus("SCHEDULED");
                if (appointmentDAO.createAppointment(app)) {
                    showAlert("✅ Succès", "Rendez-vous pris avec succès !", Alert.AlertType.INFORMATION);
                    loadAppointments();
                } else showAlert("❌ Erreur", "Impossible de prendre le rendez-vous", Alert.AlertType.ERROR);
            }
        });
    }

    private void cancelAppointment(Appointment app) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "⚠️ Annuler ce rendez-vous ?\nCette action est irréversible !", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation d'annulation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES && appointmentDAO.cancelAppointment(app.getId())) {
                showAlert("✅ Succès", "Rendez-vous annulé avec succès !", Alert.AlertType.INFORMATION);
                loadAppointments();
            }
        });
    }

    // ================== PROFIL SANTÉ ==================
    private void setupHealthProfile() {
        bloodTypeCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        heightField.textProperty().addListener((obs, old, val) -> calculateBMI());
        weightField.textProperty().addListener((obs, old, val) -> calculateBMI());
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
            } else bmiLabel.setText("---");
        } catch (NumberFormatException e) { bmiLabel.setText("---"); }
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
                showAlert("✅ Succès", "Profil santé sauvegardé avec succès !", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("❌ Erreur", "Valeurs numériques invalides", Alert.AlertType.ERROR);
        }
    }

    // ================== PARAMÈTRES MÉDICAUX + ALERTES + NIVEAU ACTIVITÉ ==================
    private void setupParametresTable() {
        // ✅ Activer l'édition
        parametreTable.setEditable(true);

        colParamDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colParamDate.setCellFactory(column -> new TableCell<ParaMedical, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        // ✅ Colonne Poids — éditable
        colParamPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colParamPoids.setCellFactory(col -> {
            TableCell<ParaMedical, Double> cell = new TableCell<>() {
                private final TextField tf = new TextField();
                {
                    tf.setStyle("-fx-font-size: 12px;");
                    tf.focusedProperty().addListener((obs, wasFocused, isNow) -> {
                        if (!isNow && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            commitEdit(parseDouble(tf.getText(), getItem()));
                    });
                    tf.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ENTER)  commitEdit(parseDouble(tf.getText(), getItem()));
                        if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
                    });
                }
                @Override public void startEdit() {
                    super.startEdit();
                    tf.setText(getItem() != null ? String.valueOf(getItem()) : "");
                    setGraphic(tf); setText(null);
                    tf.requestFocus(); tf.selectAll();
                }
                @Override public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem() != null ? String.valueOf(getItem()) : ""); setGraphic(null);
                }
                @Override public void commitEdit(Double newVal) {
                    super.commitEdit(newVal);
                    ParaMedical pm = getTableView().getItems().get(getIndex());
                    pm.setPoids(newVal);
                    saveAndRefresh(pm);
                    setGraphic(null);
                }
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else if (isEditing()) { setGraphic(tf); setText(null); }
                    else { setText(String.valueOf(item)); setGraphic(null); }
                }
            };
            cell.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !cell.isEmpty()) cell.startEdit(); });
            return cell;
        });
        colParamPoids.setOnEditCommit(e -> {});

        // ✅ Colonne Taille — éditable
        colParamTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        colParamTaille.setCellFactory(col -> {
            TableCell<ParaMedical, Double> cell = new TableCell<>() {
                private final TextField tf = new TextField();
                {
                    tf.setStyle("-fx-font-size: 12px;");
                    tf.focusedProperty().addListener((obs, wasFocused, isNow) -> {
                        if (!isNow && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            commitEdit(parseDouble(tf.getText(), getItem()));
                    });
                    tf.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ENTER)  commitEdit(parseDouble(tf.getText(), getItem()));
                        if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
                    });
                }
                @Override public void startEdit() {
                    super.startEdit();
                    tf.setText(getItem() != null ? String.valueOf(getItem()) : "");
                    setGraphic(tf); setText(null);
                    tf.requestFocus(); tf.selectAll();
                }
                @Override public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem() != null ? String.valueOf(getItem()) : ""); setGraphic(null);
                }
                @Override public void commitEdit(Double newVal) {
                    super.commitEdit(newVal);
                    ParaMedical pm = getTableView().getItems().get(getIndex());
                    pm.setTaille(newVal);
                    saveAndRefresh(pm);
                    setGraphic(null);
                }
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else if (isEditing()) { setGraphic(tf); setText(null); }
                    else { setText(String.valueOf(item)); setGraphic(null); }
                }
            };
            cell.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !cell.isEmpty()) cell.startEdit(); });
            return cell;
        });
        colParamTaille.setOnEditCommit(e -> {});

        // ✅ Colonne Glycémie — éditable
        colParamGlycemie.setCellValueFactory(new PropertyValueFactory<>("glycemie"));
        colParamGlycemie.setCellFactory(col -> {
            TableCell<ParaMedical, Double> cell = new TableCell<>() {
                private final TextField tf = new TextField();
                {
                    tf.setStyle("-fx-font-size: 12px;");
                    tf.focusedProperty().addListener((obs, wasFocused, isNow) -> {
                        if (!isNow && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            commitEdit(parseDouble(tf.getText(), getItem()));
                    });
                    tf.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ENTER)  commitEdit(parseDouble(tf.getText(), getItem()));
                        if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
                    });
                }
                @Override public void startEdit() {
                    super.startEdit();
                    tf.setText(getItem() != null ? String.valueOf(getItem()) : "");
                    setGraphic(tf); setText(null);
                    tf.requestFocus(); tf.selectAll();
                }
                @Override public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem() != null ? String.valueOf(getItem()) : ""); setGraphic(null);
                }
                @Override public void commitEdit(Double newVal) {
                    super.commitEdit(newVal);
                    ParaMedical pm = getTableView().getItems().get(getIndex());
                    pm.setGlycemie(newVal);
                    saveAndRefresh(pm);
                    setGraphic(null);
                }
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else if (isEditing()) { setGraphic(tf); setText(null); }
                    else { setText(String.valueOf(item)); setGraphic(null); }
                }
            };
            cell.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !cell.isEmpty()) cell.startEdit(); });
            return cell;
        });
        colParamGlycemie.setOnEditCommit(e -> {});

        // ✅ Colonne Tension — éditable (String)
        colParamTension.setCellValueFactory(new PropertyValueFactory<>("tensionSystolique"));
        colParamTension.setCellFactory(col -> {
            TableCell<ParaMedical, String> cell = new TableCell<>() {
                private final TextField tf = new TextField();
                {
                    tf.setStyle("-fx-font-size: 12px;");
                    tf.focusedProperty().addListener((obs, wasFocused, isNow) -> {
                        if (!isNow && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            commitEdit(tf.getText());
                    });
                    tf.setOnKeyPressed(e -> {
                        if (e.getCode() == javafx.scene.input.KeyCode.ENTER)  commitEdit(tf.getText());
                        if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
                    });
                }
                @Override public void startEdit() {
                    super.startEdit();
                    tf.setText(getItem() != null ? getItem() : "");
                    setGraphic(tf); setText(null);
                    tf.requestFocus(); tf.selectAll();
                }
                @Override public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem()); setGraphic(null);
                }
                @Override public void commitEdit(String newVal) {
                    super.commitEdit(newVal);
                    ParaMedical pm = getTableView().getItems().get(getIndex());
                    pm.setTensionSystolique(newVal);
                    saveAndRefresh(pm);
                    setGraphic(null);
                }
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setGraphic(null); }
                    else if (isEditing()) { setGraphic(tf); setText(null); }
                    else { setText(item); setGraphic(null); }
                }
            };
            cell.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !cell.isEmpty()) cell.startEdit(); });
            return cell;
        });
        colParamTension.setOnEditCommit(e -> {});

        // Colonne IMC — calculée, non éditable
        colParamImc.setCellValueFactory(cellData -> {
            Double imc = cellData.getValue().getImc();
            return new javafx.beans.property.SimpleObjectProperty<>(imc != null ? imc : 0.0);
        });
        colParamImc.setCellFactory(column -> new TableCell<ParaMedical, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        // Colonne Alerte — calculée, non éditable
        colAlerte.setCellValueFactory(cellData -> new SimpleStringProperty(verifierAlerte(cellData.getValue())));
        colAlerte.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    if (item.startsWith("⚠️")) setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: green;");
                }
            }
        });

        // Listeners pour calcul IMC en temps réel dans les champs du formulaire
        poidsField.textProperty().addListener((obs, old, val) -> calculerIMCParam());
        tailleField.textProperty().addListener((obs, old, val) -> calculerIMCParam());
    }

    // ✅ Méthode utilitaire : sauvegarde + rafraîchit alerte sans recharger toute la liste
    private void saveAndRefresh(ParaMedical pm) {
        if (paraMedicalDAO.modifier(pm)) {
            // Recalculer l'alerte visuellement
            parametreTable.refresh();
            updateNiveauActivite();
        } else {
            showAlert("Erreur", "Échec de la modification.", Alert.AlertType.ERROR);
        }
    }

    // ✅ Méthode utilitaire : parse un Double avec fallback sur l'ancienne valeur
    private double parseDouble(String text, Double fallback) {
        try { return Double.parseDouble(text.replace(",", ".")); }
        catch (NumberFormatException e) { return fallback != null ? fallback : 0.0; }
    }

    private void loadParametres() {
        parametresList = FXCollections.observableArrayList(paraMedicalDAO.getByUserId(currentUser.getId()));
        parametreTable.setItems(parametresList);
        updateNiveauActivite();
    }

    private void calculerIMCParam() {
        try {
            double poids = Double.parseDouble(poidsField.getText());
            double taille = Double.parseDouble(tailleField.getText());
            if (taille > 0 && poids > 0) {
                double imc = poids / (taille * taille);
                imcCalculLabel.setText(String.format("%.2f", imc));
                if (imc < 18.5) interpretationLabel.setText("Insuffisance pondérale");
                else if (imc < 25) interpretationLabel.setText("Poids normal");
                else if (imc < 30) interpretationLabel.setText("Surpoids");
                else if (imc < 35) interpretationLabel.setText("Obésité modérée");
                else if (imc < 40) interpretationLabel.setText("Obésité sévère");
                else interpretationLabel.setText("Obésité morbide");
            } else { imcCalculLabel.setText("--"); interpretationLabel.setText(""); }
        } catch (NumberFormatException e) { imcCalculLabel.setText("--"); interpretationLabel.setText(""); }
    }

    private void ajouterOuModifierParametre() {
        ParaMedical selected = parametreTable.getSelectionModel().getSelectedItem();
        try {
            double poids = Double.parseDouble(poidsField.getText());
            double taille = Double.parseDouble(tailleField.getText());
            double glycemie = Double.parseDouble(glycemieField.getText());
            String tension = tensionField.getText();

            if (selected != null) {
                selected.setPoids(poids);
                selected.setTaille(taille);
                selected.setGlycemie(glycemie);
                selected.setTensionSystolique(tension);
                if (paraMedicalDAO.modifier(selected)) {
                    showAlert("✅ Succès", "Paramètre modifié", Alert.AlertType.INFORMATION);
                    loadParametres();
                    viderChampsParam();
                    parametreTable.getSelectionModel().clearSelection();
                    verifierAlerteApresAjout(selected);
                } else showAlert("❌ Erreur", "Échec modification", Alert.AlertType.ERROR);
            } else {
                ParaMedical pm = new ParaMedical();
                pm.setPoids(poids);
                pm.setTaille(taille);
                pm.setGlycemie(glycemie);
                pm.setTensionSystolique(tension);
                if (paraMedicalDAO.ajouter(currentUser.getId(), pm)) {
                    showAlert("✅ Succès", "Paramètre ajouté", Alert.AlertType.INFORMATION);
                    loadParametres();
                    viderChampsParam();
                    verifierAlerteApresAjout(pm);
                } else showAlert("❌ Erreur", "Échec ajout", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("❌ Erreur", "Valeurs numériques invalides", Alert.AlertType.ERROR);
        }
    }

    private void verifierAlerteApresAjout(ParaMedical pm) {
        String alerte = verifierAlerte(pm);
        if (!alerte.equals("✓ Normal")) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Valeur critique");
            warning.setHeaderText("Attention : mesures hors normes");
            warning.setContentText(alerte);
            warning.show();
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

    private void supprimerParametre() {
        ParaMedical selected = parametreTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES && paraMedicalDAO.supprimer(selected.getId())) {
                showAlert("✅ Succès", "Paramètre supprimé", Alert.AlertType.INFORMATION);
                loadParametres();
                viderChampsParam();
                parametreTable.getSelectionModel().clearSelection();
            }
        });
    }

    private void viderChampsParam() {
        poidsField.clear();
        tailleField.clear();
        glycemieField.clear();
        tensionField.clear();
        imcCalculLabel.setText("--");
        interpretationLabel.setText("");
    }

    // ================== PRESCRIPTIONS (PDF) ==================
    private void setupPrescriptionsTable() {
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
        colActionsPresc.setCellFactory(col -> new TableCell<>() {
            private final Button pdfBtn = new Button("📄 PDF");
            {
                pdfBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                pdfBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    genererPDFPrescription(p);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : pdfBtn);
            }
        });
    }

    private void loadPrescriptions() {
        prescriptionsList = FXCollections.observableArrayList(prescriptionDAO.getByPatientId(currentUser.getId()));
        prescriptionTable.setItems(prescriptionsList);
    }

    private void genererPDFPrescription(Prescription prescription) {
        User doctor = prescriptionDAO.getDoctorByPrescriptionId(prescription.getId());
        if (doctor == null) {
            showAlert("Erreur", "Impossible de récupérer le médecin", Alert.AlertType.ERROR);
            return;
        }
        ChoiceDialog<String> langDialog = new ChoiceDialog<>("fr", "fr", "en", "ar");
        langDialog.setTitle("Choisir la langue");
        langDialog.setHeaderText("Langue du document PDF");
        langDialog.setContentText("Sélectionnez la langue pour la traduction :");
        langDialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        String selectedLang = langDialog.showAndWait().orElse(null);
        if (selectedLang == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la prescription PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fileChooser.setInitialFileName("prescription_" + prescription.getId() + ".pdf");
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Génération PDF");
        progressAlert.setHeaderText(null);
        progressAlert.setContentText("Traduction en cours...\nVeuillez patienter.");
        progressAlert.show();

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                PDFGenerator.generatePrescriptionPDF(prescription, currentUser, doctor, file.getAbsolutePath(), selectedLang);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            progressAlert.close();
            showAlert("Succès", "PDF généré avec succès !", Alert.AlertType.INFORMATION);
        });
        task.setOnFailed(e -> {
            progressAlert.close();
            Throwable ex = task.getException();
            showAlert("Erreur", "Échec : " + (ex != null ? ex.getMessage() : "Erreur inconnue"), Alert.AlertType.ERROR);
            ex.printStackTrace();
        });
        new Thread(task).start();
    }

    // ================== GRAPHIQUES DE TENDANCES ==================
    private void showTrendsForPatient(int patientId) {
        List<ParaMedical> data = paraMedicalDAO.getByUserId(patientId);
        if (data.isEmpty()) {
            showAlert("Info", "Aucune donnée médicale pour afficher les tendances", Alert.AlertType.INFORMATION);
            return;
        }
        data.sort(Comparator.comparing(ParaMedical::getCreatedAt));
        Stage stage = new Stage();
        stage.setTitle("Tendances des paramètres médicaux");
        TabPane tp = new TabPane();
        tp.getTabs().addAll(
                new Tab("Glycémie", createLineChart(data, "glycemie", "Glycémie (mmol/L)")),
                new Tab("IMC", createLineChart(data, "imc", "IMC (kg/m²)")),
                new Tab("Tension", createLineChart(data, "tension", "Tension systolique"))
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
                case "imc": val = pm.getImc(); break;
                case "tension":
                    try { val = Integer.parseInt(pm.getTensionSystolique()); } catch (Exception e) { val = 0; }
                    break;
            }
            series.getData().add(new XYChart.Data<>(date, val));
        }
        chart.getData().add(series);
        return chart;
    }

    // ================== NIVEAU D'ACTIVITÉ ==================
    private void updateNiveauActivite() {
        String niveau = getNiveauActivite();
        if (niveauActiviteLabel != null) {
            niveauActiviteLabel.setText("🏋️ Niveau d'activité : " + niveau);
            if (niveau.startsWith("Actif")) niveauActiviteLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            else if (niveau.startsWith("Moyen")) niveauActiviteLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold");
            else niveauActiviteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
    }

    private String getNiveauActivite() {
        List<ParaMedical> liste = paraMedicalDAO.getByUserId(currentUser.getId());
        if (liste.isEmpty()) return "Non actif (aucune saisie)";
        LocalDateTime derniere = liste.stream()
                .map(ParaMedical::getCreatedAt)
                .max(LocalDateTime::compareTo).orElse(null);
        if (derniere == null) return "Non actif";
        long jours = ChronoUnit.DAYS.between(derniere, LocalDateTime.now());
        if (jours <= 7) return "Actif (dernière saisie ≤ 7 jours)";
        if (jours <= 30) return "Moyen (dernière saisie entre 8 et 30 jours)";
        return "Non actif (plus de 30 jours sans saisie)";
    }

    // ================== MODULES EXTERNES (Ateliers, Forum) ==================
    private void loadExternalEventSystem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event/UserEventView.fxml"));
            Parent eventView = loader.load();
            workshopGrid.getChildren().clear();
            workshopGrid.getChildren().add(eventView);
            workshopGrid.setRowValignment(VPos.TOP);
        } catch (IOException e) {
            System.err.println("❌ Could not load modular event system: " + e.getMessage());
            e.printStackTrace();
        }
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

    // ================== ACTIONS GÉNÉRALES ==================
    private void changerVersMedecin() {
        if ("DOCTOR".equals(currentUser.getRole()) || "ADMIN".equals(currentUser.getRole())) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/DoctorDashboard.fxml"));
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 800));
                stage.setTitle("VitaHealthFX - Espace Médecin");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de basculer vers le mode médecin", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Accès refusé", "Vous n'avez pas les droits médecin", Alert.AlertType.ERROR);
        }
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