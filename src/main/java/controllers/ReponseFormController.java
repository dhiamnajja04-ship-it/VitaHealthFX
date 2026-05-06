package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.RendezVous;
import models.ReponseRendezVous;
import services.RendezVousService;
import services.ReponseRendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReponseFormController implements Initializable {

    @FXML private ComboBox<RendezVous> cbRepRdv;
    @FXML private TextArea taRepMessage;
    @FXML private ComboBox<String> cbRepType;
    @FXML private Label lblRepDate, lblRepErreur;
    @FXML private Button btnAjouter, btnModifier;
    @FXML private DatePicker dpNouvelleDate;
    @FXML private TextField tfNouvelleHeure;
    @FXML private Label lblNouvelleDateText, lblNouvelleHeureText;

    private ReponseRendezVousService service;
    private RendezVousService rdvService;
    private ReponseRendezVous repSelectionnee = null;
    private MainController mainController;

    private final ObservableList<String> listTypes = FXCollections.observableArrayList();
    private final ObservableList<RendezVous> listRdv = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service = new ReponseRendezVousService();
            rdvService = new RendezVousService();
            
            cbRepType.setItems(listTypes);
            cbRepRdv.setItems(listRdv);

            listTypes.setAll("accepte", "refuse", "reporte");
            cbRepType.setValue("accepte");
            
            cbRepType.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean isReport = "reporte".equals(newVal) || "refuse".equals(newVal);
                if (dpNouvelleDate != null) {
                    dpNouvelleDate.setVisible(isReport);
                    dpNouvelleDate.setManaged(isReport);
                }
                if (tfNouvelleHeure != null) {
                    tfNouvelleHeure.setVisible(isReport);
                    tfNouvelleHeure.setManaged(isReport);
                }
                if (lblNouvelleDateText != null) {
                    lblNouvelleDateText.setVisible(isReport);
                    lblNouvelleDateText.setManaged(isReport);
                }
                if (lblNouvelleHeureText != null) {
                    lblNouvelleHeureText.setVisible(isReport);
                    lblNouvelleHeureText.setManaged(isReport);
                }
            });
            
            chargerRendezVousEnAttente();
        } catch (SQLException e) {
            System.err.println("Erreur init ReponseForm: " + e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setReponse(ReponseRendezVous r) {
        if (r != null) {
            repSelectionnee = r;
            taRepMessage.setText(r.getMessage());
            cbRepType.setValue(r.getTypeReponse());
            lblRepDate.setText(r.getDateReponse().toString());

            // On ajoute le RDV actuel s'il n'est plus "en_attente" pour qu'il soit visible
            try {
                RendezVous rv = rdvService.getById(r.getRendezVousId());
                if (rv != null && !listRdv.contains(rv)) {
                    listRdv.add(rv);
                }
                cbRepRdv.setValue(rv);
            } catch (SQLException ignored) {}
            
            btnAjouter.setVisible(false);
            btnAjouter.setManaged(false);
        } else {
            btnModifier.setVisible(false);
            btnModifier.setManaged(false);
        }
    }

    public void setRendezVous(RendezVous rv) {
        if (rv != null) {
            if (!listRdv.contains(rv)) {
                listRdv.add(rv);
            }
            cbRepRdv.setValue(rv);
            cbRepRdv.setDisable(true); // Verrouiller le choix du RDV si on vient de l'Espace Médecin
        }
    }

    @FXML
    private void handleRepAjouter() {
        try {
            service.ajouter(construireReponse());
            if (mainController != null) mainController.rafraichirListes();
            utils.NotificationUtils.showSuccess("Succès", "La réponse a été envoyée avec succès et l'email a été expédié !");
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
            utils.NotificationUtils.showError("Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void handleRepModifier() {
        if (repSelectionnee == null) return;
        try {
            ReponseRendezVous r = construireReponse();
            r.setId(repSelectionnee.getId());
            service.modifier(r);
            if (mainController != null) mainController.rafraichirListes();
            utils.NotificationUtils.showSuccess("Modifié", "La réponse a été mise à jour.");
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
            utils.NotificationUtils.showError("Erreur", "Modification échouée : " + e.getMessage());
        }
    }

    @FXML
    private void handleRepVider() {
        cbRepRdv.setValue(null); taRepMessage.clear();
        cbRepType.setValue("accepte"); lblRepDate.setText("-");
        if (dpNouvelleDate != null) dpNouvelleDate.setValue(null);
        if (tfNouvelleHeure != null) tfNouvelleHeure.clear();
        lblRepErreur.setText("");
    }

    @FXML
    private void handleFermer() {
        if (taRepMessage.getScene() != null && taRepMessage.getScene().getWindow() != null) {
            ((Stage) taRepMessage.getScene().getWindow()).close();
        }
    }

    private void chargerRendezVousEnAttente() {
        try {
            listRdv.setAll(rdvService.getByStatut("en_attente"));
        } catch (SQLException ignored) {}
    }

    private ReponseRendezVous construireReponse() {
        ReponseRendezVous r = new ReponseRendezVous();
        RendezVous rv = cbRepRdv.getValue();
        if (rv == null) throw new IllegalArgumentException("Le rendez-vous est requis.");
        
        r.setRendezVousId(rv.getId());
        r.setMessage(taRepMessage.getText().trim());
        r.setTypeReponse(cbRepType.getValue());
        
        String type = r.getTypeReponse();
        String message = r.getMessage();
        if (("reporte".equals(type) || "refuse".equals(type)) && dpNouvelleDate != null && dpNouvelleDate.getValue() != null && tfNouvelleHeure != null && !tfNouvelleHeure.getText().trim().isEmpty()) {
            try {
                java.time.LocalTime.parse(tfNouvelleHeure.getText().trim());
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("Nouvelle heure invalide (HH:mm).");
            }
            message += "\n[Proposition d'une autre date : le " + dpNouvelleDate.getValue() + " à " + tfNouvelleHeure.getText().trim() + "]";
            r.setMessage(message);
        }
        
        if (r.getMessage().isEmpty()) throw new IllegalArgumentException("Le message est requis.");
        
        return r;
    }

    public void setViewMode() {
        cbRepRdv.setDisable(true);
        taRepMessage.setDisable(true);
        cbRepType.setDisable(true);
        if (dpNouvelleDate != null) dpNouvelleDate.setDisable(true);
        if (tfNouvelleHeure != null) tfNouvelleHeure.setDisable(true);
        btnAjouter.setVisible(false);
        btnAjouter.setManaged(false);
        btnModifier.setVisible(false);
        btnModifier.setManaged(false);
    }

    private void afficherErreur(String msg) {
        lblRepErreur.setStyle("-fx-text-fill: red;");
        lblRepErreur.setText("⚠ " + msg);
    }
}
