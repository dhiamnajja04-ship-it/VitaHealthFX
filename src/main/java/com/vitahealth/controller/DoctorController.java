package com.vitahealth.controller;

import com.vitahealth.dao.ParaMedicalDAO;
import com.vitahealth.dao.PrescriptionDAO;
import com.vitahealth.dao.SeuilDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.entity.ParaMedical;
import com.vitahealth.entity.Prescription;
import com.vitahealth.entity.User;
import com.vitahealth.service.MedicamentService;
import com.vitahealth.util.PDFGenerator;
import com.vitahealth.util.SessionManager;
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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DoctorController {

    @FXML private Label userLabel;
    @FXML private Button logoutBtn;

    @FXML private TextField searchMaladieField;
    @FXML private Button searchMaladieBtn;
    @FXML private Button resetMaladieBtn;

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
    @FXML private Button supprimerPrescriptionBtn;
    @FXML private Button viderPrescChampsBtn;
    @FXML private Button genererPdfBtn;

    // Recherche médicaments
    @FXML private TextField rechercheMedicamentField;
    @FXML private Button rechercherMedicamentBtn;
    @FXML private ListView<MedicamentService.Medicament> resultatsMedicaments;

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
    @FXML private TableColumn<ParaMedical, String> colAlerte;
    @FXML private TableColumn<ParaMedical, String> colNiveauActivite;

    @FXML private Button showTrendsBtn;

    private UserDAO userDAO;
    private PrescriptionDAO prescriptionDAO;
    private ParaMedicalDAO paraMedicalDAO;
    private SeuilDAO seuilDAO;
    private User currentUser;
    private ObservableList<User> allPatients;
    private Map<String, Map<String, Double>> seuils;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private int currentPatientId = -1;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        prescriptionDAO = new PrescriptionDAO();
        paraMedicalDAO = new ParaMedicalDAO();
        seuilDAO = new SeuilDAO();

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) userLabel.setText("Dr. " + currentUser.getFullName());

        seuils = seuilDAO.getSeuils();

        setupPrescriptionTable();
        setupParametreTable();
        loadAllPatients();

        // Actions
        searchMaladieBtn.setOnAction(e -> filtrerPatientsParMaladie());
        resetMaladieBtn.setOnAction(e -> resetFiltreMaladie());

        loadPatientPrescBtn.setOnAction(e -> loadPrescriptionsForPatient());
        refreshPrescListBtn.setOnAction(e -> loadPrescriptionsForPatient());

        ajouterPrescriptionBtn.setOnAction(e -> ajouterOuModifierPrescription());
        supprimerPrescriptionBtn.setOnAction(e -> supprimerPrescription());
        viderPrescChampsBtn.setOnAction(e -> viderChampsPrescription());
        genererPdfBtn.setOnAction(e -> genererPDF());

        loadPatientParamBtn.setOnAction(e -> loadParametresForPatient());
        refreshParamListBtn.setOnAction(e -> loadParametresForPatient());
        showTrendsBtn.setOnAction(e -> showTrendsForSelectedPatient());

        logoutBtn.setOnAction(e -> logout());

        // API Médicaments
        rechercherMedicamentBtn.setOnAction(e -> rechercherMedicament());
        resultatsMedicaments.setOnMouseClicked(event -> {
            MedicamentService.Medicament selected = resultatsMedicaments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String current = medicamentsArea.getText();
                String newMed = selected.toString();
                if (current.isEmpty()) medicamentsArea.setText(newMed);
                else medicamentsArea.setText(current + "\n" + newMed);
                resultatsMedicaments.setVisible(false);
                resultatsMedicaments.setManaged(false);
                resultatsMedicaments.getItems().clear();
                rechercheMedicamentField.clear();
            }
        });

        // Listener sélection prescription
        prescriptionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                medicamentsArea.setText(newVal.getMedicationList());
                dureeField.setText(newVal.getDuration());
                instructionsArea.setText(newVal.getInstructions());
                ajouterPrescriptionBtn.setText("✏️ Mettre à jour");
            } else {
                ajouterPrescriptionBtn.setText("➕ Ajouter");
            }
        });

        patientPrescCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
            @Override public User fromString(String s) { return null; }
        });
        patientParamCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
            @Override public User fromString(String s) { return null; }
        });
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
        colInterpretation.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImcInterpretation()));

        colAlerte.setCellValueFactory(cellData -> new SimpleStringProperty(verifierAlerte(cellData.getValue())));
        colAlerte.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                if (!empty && item != null && item.startsWith("⚠️")) {
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: green;");
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
                    if (item.contains("Actif")) setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
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

    private void loadAllPatients() {
        try {
            List<User> patients = userDAO.findByRole("PATIENT");
            allPatients = FXCollections.observableArrayList(patients);
            patientPrescCombo.setItems(allPatients);
            patientParamCombo.setItems(allPatients);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void filtrerPatientsParMaladie() {
        String maladie = searchMaladieField.getText().trim().toLowerCase();
        if (maladie.isEmpty()) { resetFiltreMaladie(); return; }
        List<User> filtered = allPatients.stream()
                .filter(p -> p.getMaladie() != null && p.getMaladie().toLowerCase().contains(maladie))
                .collect(Collectors.toList());
        patientPrescCombo.setItems(FXCollections.observableArrayList(filtered));
        patientParamCombo.setItems(FXCollections.observableArrayList(filtered));
    }

    private void resetFiltreMaladie() {
        patientPrescCombo.setItems(allPatients);
        patientParamCombo.setItems(allPatients);
        searchMaladieField.clear();
    }

    private void loadPrescriptionsForPatient() {
        User patient = patientPrescCombo.getValue();
        if (patient == null) { showAlert("Info", "Sélectionnez un patient", Alert.AlertType.INFORMATION); return; }
        prescriptionTable.setItems(FXCollections.observableArrayList(prescriptionDAO.getByPatientId(patient.getId())));
    }

    private void loadParametresForPatient() {
        User patient = patientParamCombo.getValue();
        if (patient == null) {
            parametreTable.setItems(FXCollections.observableArrayList());
            currentPatientId = -1;
            parametreTable.refresh();
            return;
        }
        currentPatientId = patient.getId();
        parametreTable.setItems(FXCollections.observableArrayList(paraMedicalDAO.getByUserId(patient.getId())));
        parametreTable.refresh();
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
        if (jours <= 7) return "Actif (dernière saisie ≤ 7 jours)";
        if (jours <= 30) return "Moyen (dernière saisie entre 8 et 30 jours)";
        return "Non actif (plus de 30 jours sans saisie)";
    }

    private void rechercherMedicament() {
        String query = rechercheMedicamentField.getText().trim();
        if (query.isEmpty()) {
            showAlert("Info", "Saisissez un nom de médicament", Alert.AlertType.INFORMATION);
            return;
        }
        resultatsMedicaments.setVisible(false);
        resultatsMedicaments.setManaged(false);
        MedicamentService service = new MedicamentService();
        service.rechercher(query).thenAccept(medicaments -> {
            Platform.runLater(() -> {
                if (medicaments.isEmpty()) {
                    showAlert("Info", "Aucun médicament trouvé", Alert.AlertType.INFORMATION);
                    return;
                }
                resultatsMedicaments.getItems().setAll(medicaments);
                resultatsMedicaments.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(MedicamentService.Medicament item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.toString());
                    }
                });
                resultatsMedicaments.setVisible(true);
                resultatsMedicaments.setManaged(true);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showAlert("Erreur", "Problème de connexion à l'API", Alert.AlertType.ERROR));
            return null;
        });
    }

    private void ajouterOuModifierPrescription() {
        User patient = patientPrescCombo.getValue();
        if (patient == null) { showAlert("Erreur", "Choisissez un patient", Alert.AlertType.ERROR); return; }
        String med = medicamentsArea.getText();
        String dur = dureeField.getText();
        String ins = instructionsArea.getText();
        if (med.isEmpty() || dur.isEmpty() || ins.isEmpty()) {
            showAlert("Info", "Remplissez tous les champs", Alert.AlertType.WARNING);
            return;
        }
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setMedicationList(med);
            selected.setDuration(dur);
            selected.setInstructions(ins);
            if (prescriptionDAO.modifier(selected)) {
                showAlert("Succès", "Prescription modifiée", Alert.AlertType.INFORMATION);
                loadPrescriptionsForPatient();
                viderChampsPrescription();
                prescriptionTable.getSelectionModel().clearSelection();
            } else showAlert("Erreur", "Échec modification", Alert.AlertType.ERROR);
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
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Info", "Sélectionnez une prescription", Alert.AlertType.WARNING); return; }
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
        Prescription selected = prescriptionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Info", "Sélectionnez une prescription", Alert.AlertType.WARNING); return; }
        User patient = patientPrescCombo.getValue();
        if (patient == null) { showAlert("Erreur", "Patient non sélectionné", Alert.AlertType.ERROR); return; }

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

    private void viderChampsPrescription() {
        medicamentsArea.clear();
        dureeField.clear();
        instructionsArea.clear();
        rechercheMedicamentField.clear();
        resultatsMedicaments.setVisible(false);
        resultatsMedicaments.setManaged(false);
        resultatsMedicaments.getItems().clear();
    }

    private void showTrendsForSelectedPatient() {
        User patient = patientParamCombo.getValue();
        if (patient == null) { showAlert("Info", "Sélectionnez un patient", Alert.AlertType.INFORMATION); return; }
        List<ParaMedical> data = paraMedicalDAO.getByUserId(patient.getId());
        if (data.isEmpty()) { showAlert("Info", "Aucune donnée médicale", Alert.AlertType.INFORMATION); return; }
        data.sort(Comparator.comparing(ParaMedical::getCreatedAt));
        Stage stage = new Stage();
        stage.setTitle("Tendances - " + patient.getFullName());
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

    private void logout() {
        SessionManager.getInstance().logout();
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(login, 450, 600));
            stage.setTitle("Connexion");
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