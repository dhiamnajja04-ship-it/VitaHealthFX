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
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
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
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
        }
    }

    @FXML
    private void handleRepVider() {
        cbRepRdv.setValue(null); taRepMessage.clear();
        cbRepType.setValue("accepte"); lblRepDate.setText("-");
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
        
        if (r.getMessage().isEmpty()) throw new IllegalArgumentException("Le message est requis.");
        
        return r;
    }

    public void setViewMode() {
        cbRepRdv.setDisable(true);
        taRepMessage.setDisable(true);
        cbRepType.setDisable(true);
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
