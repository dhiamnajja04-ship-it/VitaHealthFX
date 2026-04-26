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
import services.WeatherService;
import javafx.application.Platform;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class RdvFormController implements Initializable {

    @FXML private DatePicker dpRdvDate;
    @FXML private TextField tfRdvHeure, tfRdvPatientNom, tfRdvPatientPrenom, tfRdvPatientTel, tfRdvPatientEmail;
    @FXML private TextArea taRdvMotif;
    @FXML private ComboBox<String> cbRdvPriorite;
    @FXML private ComboBox<Medecin> cbRdvMedecin;
    @FXML private Label lblRdvErreur, lblWeather;
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

            // Smart Queue & Weather Listeners
            dpRdvDate.valueProperty().addListener((obs, oldVal, newVal) -> {
                suggérerHeureAutomatique();
                mettreAJourMétéo(newVal);
            });
            cbRdvMedecin.valueProperty().addListener((obs, oldVal, newVal) -> suggérerHeureAutomatique());
            
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
            mettreAJourMétéo(rv.getDate());
            tfRdvHeure.setText(rv.getHeure().toString());
            taRdvMotif.setText(rv.getMotif());
            cbRdvPriorite.setValue(rv.getPriorite());
            tfRdvPatientNom.setText(rv.getPatientNom());
            tfRdvPatientPrenom.setText(rv.getPatientPrenom());
            tfRdvPatientTel.setText(rv.getPatientTel());
            if (tfRdvPatientEmail != null) tfRdvPatientEmail.setText(rv.getPatientEmail());
            
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
        if (tfRdvPatientEmail != null) tfRdvPatientEmail.clear();
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
        if (tfRdvPatientEmail != null) {
            rv.setPatientEmail(tfRdvPatientEmail.getText().trim());
        }

        if (rv.getPatientNom().isEmpty()) throw new IllegalArgumentException("Le nom du patient est requis.");
        
        return rv;
    }

    public void setViewMode() {
        dpRdvDate.setDisable(true);
        tfRdvHeure.setDisable(true);
        taRdvMotif.setDisable(true);
        cbRdvPriorite.setDisable(true);
        cbRdvMedecin.setDisable(true);
        tfRdvPatientNom.setDisable(true);
        tfRdvPatientPrenom.setDisable(true);
        tfRdvPatientTel.setDisable(true);
        if (tfRdvPatientEmail != null) tfRdvPatientEmail.setDisable(true);
        
        btnAjouter.setVisible(false);
        btnAjouter.setManaged(false);
        btnModifier.setVisible(false);
        btnModifier.setManaged(false);
    }

    private void suggérerHeureAutomatique() {
        if (rdvSelectionne != null) return; // Mode édition : on laisse l'heure choisie
        
        Medecin m = cbRdvMedecin.getValue();
        java.time.LocalDate d = dpRdvDate.getValue();
        
        if (m != null && d != null) {
            try {
                LocalTime next = service.genererProchainCreneau(m.getId(), d);
                tfRdvHeure.setText(next.toString());
                lblRdvErreur.setText("");
            } catch (IllegalStateException e) {
                tfRdvHeure.clear();
                afficherErreur(e.getMessage());
            } catch (Exception e) {
                System.err.println("Erreur SmartQueue: " + e.getMessage());
            }
        }
    }

    private void afficherErreur(String msg) {
        lblRdvErreur.setStyle("-fx-text-fill: red;");
        lblRdvErreur.setText("⚠ " + msg);
    }

    private void mettreAJourMétéo(java.time.LocalDate date) {
        if (date == null || lblWeather == null) return;
        
        lblWeather.setText("...☁");
        WeatherService.getWeatherForecast(date).thenAccept(weather -> {
            Platform.runLater(() -> lblWeather.setText("🌤 " + weather));
        });
    }
}
