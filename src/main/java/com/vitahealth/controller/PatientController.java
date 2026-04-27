package com.vitahealth.controller;

import com.vitahealth.dao.AppointmentDAO;
import com.vitahealth.dao.HealthProfileDAO;
import com.vitahealth.dao.ParaMedicalDAO;
import com.vitahealth.dao.PrescriptionDAO;
import com.vitahealth.dao.SeuilDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.entity.Appointment;
import com.vitahealth.entity.HealthProfile;
import com.vitahealth.entity.ParaMedical;
import com.vitahealth.entity.Prescription;
import com.vitahealth.entity.User;
import com.vitahealth.util.PDFGenerator;
import com.vitahealth.util.SessionManager;
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

    // Rendez-vous
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, LocalDateTime> colDate;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;
    @FXML private Button newAppointmentBtn;
    @FXML private Button refreshAppointmentsBtn;

    // Profil santé
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

    // Paramètres médicaux
    @FXML private TextField poidsField;
    @FXML private TextField tailleField;
    @FXML private TextField glycemieField;
    @FXML private TextField tensionField;
    @FXML private Label imcCalculLabel;
    @FXML private Label interpretationLabel;
    @FXML private Button ajouterParametreBtn;
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
    @FXML private Label niveauActiviteLabel; // Ajouté

    // Prescriptions
    @FXML private TableView<Prescription> prescriptionTable;
    @FXML private TableColumn<Prescription, LocalDateTime> colPrescDate;
    @FXML private TableColumn<Prescription, String> colMedicaments;
    @FXML private TableColumn<Prescription, String> colDuree;
    @FXML private TableColumn<Prescription, String> colInstructions;
    @FXML private TableColumn<Prescription, Void> colActionsPresc;

    // Graphiques
    @FXML private Button showTrendsBtn;

    // Menu
    @FXML private MenuItem changerVersMedecin;
    @FXML private MenuItem quitter;
    @FXML private MenuItem aPropos;

    private AppointmentDAO appointmentDAO;
    private HealthProfileDAO healthProfileDAO;
    private UserDAO userDAO;
    private ParaMedicalDAO paraMedicalDAO;
    private PrescriptionDAO prescriptionDAO;
    private SeuilDAO seuilDAO;

    private User currentUser;
    private ObservableList<Appointment> appointmentsList;
    private ObservableList<ParaMedical> parametresList;
    private ObservableList<Prescription> prescriptionsList;

    private Map<String, Map<String, Double>> seuils;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

        setupButtons();

        newAppointmentBtn.setOnAction(e -> openNewAppointmentDialog());
        refreshAppointmentsBtn.setOnAction(e -> loadAppointments());
        saveHealthBtn.setOnAction(e -> saveHealthProfile());
        refreshHealthBtn.setOnAction(e -> loadHealthProfile());

        ajouterParametreBtn.setOnAction(e -> ajouterOuModifierParametre());
        supprimerParametreBtn.setOnAction(e -> supprimerParametre());
        rafraichirParametresBtn.setOnAction(e -> loadParametres());

        parametreTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                poidsField.setText(String.valueOf(newVal.getPoids()));
                tailleField.setText(String.valueOf(newVal.getTaille()));
                glycemieField.setText(String.valueOf(newVal.getGlycemie()));
                tensionField.setText(newVal.getTensionSystolique());
                ajouterParametreBtn.setText("✏️ Mettre à jour");
            } else {
                ajouterParametreBtn.setText("➕ Ajouter");
            }
        });

        showTrendsBtn.setOnAction(e -> showTrendsForPatient(currentUser.getId()));

        logoutBtn.setOnAction(e -> logout());
        if (changerVersMedecin != null) changerVersMedecin.setOnAction(e -> changerVersMedecin());
        if (quitter != null) quitter.setOnAction(e -> System.exit(0));
        if (aPropos != null) aPropos.setOnAction(e -> showAlert("À propos", "VitaHealthFX - Version 2.0\nEspace Patient\nFonctionnalités : PDF, graphiques, alertes", Alert.AlertType.INFORMATION));

        // Mise à jour du niveau d'activité
        updateNiveauActivite();
    }

    // ================== NIVEAU D'ACTIVITÉ ==================
    private void updateNiveauActivite() {
        String niveau = getNiveauActivite();
        if (niveauActiviteLabel != null) {
            niveauActiviteLabel.setText("🏋️ Niveau d'activité : " + niveau);
            if (niveau.startsWith("Actif")) {
                niveauActiviteLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else if (niveau.startsWith("Moyen")) {
                niveauActiviteLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else {
                niveauActiviteLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        }
    }

    private String getNiveauActivite() {
        List<ParaMedical> liste = paraMedicalDAO.getByUserId(currentUser.getId());
        if (liste.isEmpty()) {
            return "Non actif (aucune saisie)";
        }
        LocalDateTime derniereSaisie = liste.stream()
                .map(ParaMedical::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (derniereSaisie == null) return "Non actif";
        long jours = ChronoUnit.DAYS.between(derniereSaisie, LocalDateTime.now());
        if (jours <= 7) {
            return "Actif (dernière saisie dans les 7 jours)";
        } else if (jours <= 30) {
            return "Moyen (dernière saisie entre 7 et 30 jours)";
        } else {
            return "Non actif (plus de 30 jours sans saisie)";
        }
    }

    // ================== FIN NIVEAU D'ACTIVITÉ ==================

    private void setupAppointmentsTable() {
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
                addHoverAnimation(cancelBtn);
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

    private void setupHealthProfile() {
        bloodTypeCombo.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        heightField.textProperty().addListener((obs, old, val) -> calculateBMI());
        weightField.textProperty().addListener((obs, old, val) -> calculateBMI());
    }

    private void setupParametresTable() {
        colParamDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colParamPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colParamTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        colParamGlycemie.setCellValueFactory(new PropertyValueFactory<>("glycemie"));
        colParamTension.setCellValueFactory(new PropertyValueFactory<>("tensionSystolique"));
        colParamImc.setCellValueFactory(cellData -> {
            Double imc = cellData.getValue().getImc();
            return new javafx.beans.property.SimpleObjectProperty<>(imc != null ? imc : 0.0);
        });

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

        colParamDate.setCellFactory(column -> new TableCell<ParaMedical, LocalDateTime>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        colParamImc.setCellFactory(column -> new TableCell<ParaMedical, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });

        poidsField.textProperty().addListener((obs, old, val) -> calculerIMCParam());
        tailleField.textProperty().addListener((obs, old, val) -> calculerIMCParam());
    }

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
            @Override
            protected Void call() throws Exception {
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

    private void setupButtons() {
        newAppointmentBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshAppointmentsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        saveHealthBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshHealthBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        ajouterParametreBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        supprimerParametreBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        rafraichirParametresBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        showTrendsBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");

        addHoverAnimation(newAppointmentBtn);
        addHoverAnimation(refreshAppointmentsBtn);
        addHoverAnimation(saveHealthBtn);
        addHoverAnimation(refreshHealthBtn);
        addHoverAnimation(ajouterParametreBtn);
        addHoverAnimation(supprimerParametreBtn);
        addHoverAnimation(rafraichirParametresBtn);
        addHoverAnimation(showTrendsBtn);
    }

    private void addHoverAnimation(Button btn) {
        if (btn == null) return;
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

    private void loadParametres() {
        parametresList = FXCollections.observableArrayList(paraMedicalDAO.getByUserId(currentUser.getId()));
        parametreTable.setItems(parametresList);
        updateNiveauActivite(); // Met à jour après chargement
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
                    updateNiveauActivite(); // Mise à jour
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
                    updateNiveauActivite(); // Mise à jour
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
        StringBuilder alerte = new StringBuilder();
        double gly = pm.getGlycemie();
        Map<String, Double> glySeuil = seuils.get("glycemie");
        if (glySeuil != null && (gly < glySeuil.get("min") || gly > glySeuil.get("max"))) alerte.append("⚠️ Glycémie anormale ");
        try {
            int tens = Integer.parseInt(pm.getTensionSystolique());
            Map<String, Double> tensSeuil = seuils.get("tension");
            if (tensSeuil != null && (tens < tensSeuil.get("min") || tens > tensSeuil.get("max"))) alerte.append("⚠️ Tension anormale ");
        } catch (NumberFormatException ignored) {}
        double imc = pm.getImc();
        Map<String, Double> imcSeuil = seuils.get("imc");
        if (imcSeuil != null && (imc < imcSeuil.get("min") || imc > imcSeuil.get("max"))) alerte.append("⚠️ IMC anormal ");
        return alerte.length() > 0 ? alerte.toString() : "✓ Normal";
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
                updateNiveauActivite(); // Mise à jour
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

    private void loadPrescriptions() {
        prescriptionsList = FXCollections.observableArrayList(prescriptionDAO.getByPatientId(currentUser.getId()));
        prescriptionTable.setItems(prescriptionsList);
    }

    private void showTrendsForPatient(int patientId) {
        List<ParaMedical> data = paraMedicalDAO.getByUserId(patientId);
        if (data.isEmpty()) {
            showAlert("Info", "Aucune donnée médicale pour afficher les tendances", Alert.AlertType.INFORMATION);
            return;
        }
        Stage stage = new Stage();
        stage.setTitle("Tendances des paramètres médicaux");
        TabPane tabPane = new TabPane();

        data.sort(Comparator.comparing(ParaMedical::getCreatedAt));

        LineChart<String, Number> glycemieChart = createLineChart(data, "glycemie", "Glycémie (mmol/L)");
        Tab glycemieTab = new Tab("Glycémie", glycemieChart);
        glycemieTab.setClosable(false);

        LineChart<String, Number> imcChart = createLineChart(data, "imc", "IMC (kg/m²)");
        Tab imcTab = new Tab("IMC", imcChart);
        imcTab.setClosable(false);

        LineChart<String, Number> tensionChart = createLineChart(data, "tension", "Tension systolique (mmHg)");
        Tab tensionTab = new Tab("Tension", tensionChart);
        tensionTab.setClosable(false);

        tabPane.getTabs().addAll(glycemieTab, imcTab, tensionTab);
        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private LineChart<String, Number> createLineChart(List<ParaMedical> data, String type, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel(yLabel);
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(yLabel);
        chart.setCreateSymbols(true);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valeurs");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM");
        for (ParaMedical pm : data) {
            String dateStr = pm.getCreatedAt().format(dateFormat);
            Number value = 0;
            switch (type) {
                case "glycemie": value = pm.getGlycemie(); break;
                case "imc": value = pm.getImc(); break;
                case "tension":
                    try { value = Integer.parseInt(pm.getTensionSystolique()); } catch (NumberFormatException e) { value = 0; }
                    break;
            }
            series.getData().add(new XYChart.Data<>(dateStr, value));
        }
        chart.getData().add(series);
        return chart;
    }

    // ================== RENDEZ-VOUS ET NAVIGATION ==================
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
                } else {
                    showAlert("❌ Erreur", "Impossible de prendre le rendez-vous", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cancelAppointment(Appointment app) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "⚠️ Annuler ce rendez-vous ?\nCette action est irréversible !",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation d'annulation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES && appointmentDAO.cancelAppointment(app.getId())) {
                showAlert("✅ Succès", "Rendez-vous annulé avec succès !", Alert.AlertType.INFORMATION);
                loadAppointments();
            }
        });
    }

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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("VitaHealthFX - Connexion");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}