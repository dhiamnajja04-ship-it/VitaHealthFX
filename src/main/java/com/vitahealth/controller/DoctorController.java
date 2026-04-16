package com.vitahealth.controller;

import com.vitahealth.dao.ParaMedicalDAO;
import com.vitahealth.dao.PrescriptionDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.entity.ParaMedical;
import com.vitahealth.entity.Prescription;
import com.vitahealth.entity.User;
import com.vitahealth.util.SessionManager;
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
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorController {

    // Composants FXML
    @FXML private Label userLabel;
    @FXML private Button logoutBtn;

    // Recherche par maladie
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

    private UserDAO userDAO;
    private PrescriptionDAO prescriptionDAO;
    private ParaMedicalDAO paraMedicalDAO;
    private User currentUser;
    private ObservableList<User> allPatients;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        prescriptionDAO = new PrescriptionDAO();
        paraMedicalDAO = new ParaMedicalDAO();

        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Dr. " + currentUser.getFullName());
        }

        setupPrescriptionTable();
        setupParametreTable();
        loadAllPatients();

        // Actions
        searchMaladieBtn.setOnAction(e -> filtrerPatientsParMaladie());
        resetMaladieBtn.setOnAction(e -> resetFiltreMaladie());

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filtrerPatientsParMaladie() {
        String maladie = searchMaladieField.getText().trim().toLowerCase();
        if (maladie.isEmpty()) {
            resetFiltreMaladie();
            return;
        }
        List<User> filtered = allPatients.stream()
                .filter(p -> p.getMaladie() != null && p.getMaladie().toLowerCase().contains(maladie))
                .collect(Collectors.toList());
        ObservableList<User> filteredList = FXCollections.observableArrayList(filtered);
        patientPrescCombo.setItems(filteredList);
        patientParamCombo.setItems(filteredList);
    }

    private void resetFiltreMaladie() {
        patientPrescCombo.setItems(allPatients);
        patientParamCombo.setItems(allPatients);
        searchMaladieField.clear();
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
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(loginView, 450, 600));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}