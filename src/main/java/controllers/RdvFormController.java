package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Medecin;
import models.RendezVous;
import services.MedecinService;
import services.RendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class RdvFormController implements Initializable {

    @FXML private DatePicker dpRdvDate;
    @FXML private TextField tfRdvHeure, tfRdvPatientNom, tfRdvPatientPrenom, tfRdvPatientTel;
    @FXML private TextArea taRdvMotif;
    @FXML private ComboBox<String> cbRdvPriorite;
    @FXML private ComboBox<Medecin> cbRdvMedecin;
    @FXML private Label lblRdvErreur;
    @FXML private Button btnAjouter, btnModifier;

    private RendezVousService service;
    private MedecinService medecinService;
    private RendezVous rdvSelectionne = null;
    private MainController mainController;

    private final ObservableList<String> listPriorites = FXCollections.observableArrayList();
    private final ObservableList<Medecin> listMedecins = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service = new RendezVousService();
            medecinService = new MedecinService();
            
            cbRdvPriorite.setItems(listPriorites);
            cbRdvMedecin.setItems(listMedecins);

            listPriorites.setAll("Normale", "Urgente", "Basse");
            cbRdvPriorite.setValue("Normale");
            
            dpRdvDate.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(java.time.LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.compareTo(java.time.LocalDate.now()) < 0);
                }
            });
            
            chargerMedecins();
        } catch (SQLException e) {
            System.err.println("Erreur init RdvForm: " + e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setRendezVous(RendezVous rv) {
        if (rv != null) {
            rdvSelectionne = rv;
            dpRdvDate.setValue(rv.getDate());
            tfRdvHeure.setText(rv.getHeure().toString());
            taRdvMotif.setText(rv.getMotif());
            cbRdvPriorite.setValue(rv.getPriorite());
            tfRdvPatientNom.setText(rv.getPatientNom());
            tfRdvPatientPrenom.setText(rv.getPatientPrenom());
            tfRdvPatientTel.setText(rv.getPatientTel());
            
            cbRdvMedecin.getItems().stream()
                .filter(m -> m.getId() == rv.getMedecinId())
                .findFirst().ifPresent(m -> cbRdvMedecin.setValue(m));
                
            btnAjouter.setVisible(false);
            btnAjouter.setManaged(false);
        } else {
            btnModifier.setVisible(false);
            btnModifier.setManaged(false);
        }
    }

    @FXML
    private void handleRdvAjouter() {
        try {
            service.ajouter(construireRdv());
            if (mainController != null) mainController.rafraichirListes();
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
        }
    }

    @FXML
    private void handleRdvModifier() {
        if (rdvSelectionne == null) return;
        try {
            RendezVous rv = construireRdv();
            rv.setId(rdvSelectionne.getId());
            // Conserver l'ancien statut lors de la modification depuis ce formulaire
            rv.setStatut(rdvSelectionne.getStatut());
            service.modifier(rv);
            if (mainController != null) mainController.rafraichirListes();
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
        }
    }

    @FXML
    private void handleRdvVider() {
        dpRdvDate.setValue(null); tfRdvHeure.clear(); taRdvMotif.clear();
        cbRdvPriorite.setValue("Normale"); cbRdvMedecin.setValue(null);
        tfRdvPatientNom.clear(); tfRdvPatientPrenom.clear(); tfRdvPatientTel.clear();
        lblRdvErreur.setText("");
    }

    @FXML
    private void handleFermer() {
        if (taRdvMotif.getScene() != null && taRdvMotif.getScene().getWindow() != null) {
            ((Stage) taRdvMotif.getScene().getWindow()).close();
        }
    }

    private void chargerMedecins() {
        try {
            listMedecins.setAll(medecinService.afficher());
        } catch (SQLException ignored) {}
    }

    private RendezVous construireRdv() {
        RendezVous rv = new RendezVous();
        rv.setDate(dpRdvDate.getValue());
        if (rv.getDate() == null) throw new IllegalArgumentException("La date est requise.");
        if (rv.getDate().isBefore(java.time.LocalDate.now())) throw new IllegalArgumentException("La date ne peut pas être dans le passé.");
        
        try { 
            rv.setHeure(LocalTime.parse(tfRdvHeure.getText().trim())); 
        } catch (DateTimeParseException e) { 
            throw new IllegalArgumentException("Heure invalide (HH:mm)."); 
        }
        
        rv.setMotif(taRdvMotif.getText().trim());
        rv.setPriorite(cbRdvPriorite.getValue());
        
        // Statut par défaut si non spécifié (cas nouvel ajout)
        rv.setStatut("en_attente");
        
        Medecin m = cbRdvMedecin.getValue();
        if (m == null) throw new IllegalArgumentException("Médecin requis.");
        rv.setMedecinId(m.getId());
        
        rv.setPatientNom(tfRdvPatientNom.getText().trim());
        rv.setPatientPrenom(tfRdvPatientPrenom.getText().trim());
        String tel = tfRdvPatientTel.getText().trim();
        if (!tel.matches("\\d{8}")) {
            throw new IllegalArgumentException("Le numéro de téléphone doit contenir exactement 8 chiffres.");
        }
        rv.setPatientTel(tel);
        
        if (rv.getPatientNom().isEmpty()) throw new IllegalArgumentException("Le nom du patient est requis.");
        
        return rv;
    }

    private void afficherErreur(String msg) {
        lblRdvErreur.setStyle("-fx-text-fill: red;");
        lblRdvErreur.setText("⚠ " + msg);
    }
}
