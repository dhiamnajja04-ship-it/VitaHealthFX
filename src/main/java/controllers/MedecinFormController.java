package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Medecin;
import services.MedecinService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MedecinFormController implements Initializable {

    @FXML private TextField tfMedNom, tfMedPrenom, tfMedSpecialite, tfMedTelephone, tfMedEmail;
    @FXML private Label lblMedErreur;
    @FXML private Button btnAjouter, btnModifier;

    private MedecinService service;
    private Medecin medSelectionne = null;
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service = new MedecinService();
        } catch (SQLException e) {
            System.err.println("Erreur init MedecinService: " + e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setMedecin(Medecin m) {
        if (m != null) {
            medSelectionne = m;
            tfMedNom.setText(m.getNom());
            tfMedPrenom.setText(m.getPrenom());
            tfMedSpecialite.setText(m.getSpecialite());
            tfMedTelephone.setText(m.getTelephone());
            tfMedEmail.setText(m.getEmail());
            btnAjouter.setVisible(false);
            btnAjouter.setManaged(false);
        } else {
            btnModifier.setVisible(false);
            btnModifier.setManaged(false);
        }
    }

    @FXML
    private void handleMedAjouter() {
        try {
            service.ajouter(construireMedecin());
            if (mainController != null) mainController.rafraichirListes();
            utils.NotificationUtils.showSuccess("Médecin ajouté", "Le médecin a été enregistré avec succès.");
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
            utils.NotificationUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleMedModifier() {
        if (medSelectionne == null) return;
        try {
            Medecin m = construireMedecin();
            m.setId(medSelectionne.getId());
            service.modifier(m);
            if (mainController != null) mainController.rafraichirListes();
            utils.NotificationUtils.showSuccess("Médecin modifié", "Les informations ont été mises à jour.");
            handleFermer();
        } catch (Exception e) {
            afficherErreur(e.getMessage());
            utils.NotificationUtils.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleMedVider() {
        tfMedNom.clear(); tfMedPrenom.clear(); tfMedSpecialite.clear();
        tfMedTelephone.clear(); tfMedEmail.clear();
        lblMedErreur.setText("");
    }

    @FXML
    private void handleFermer() {
        ((Stage) btnAjouter.getScene().getWindow()).close();
    }

    private Medecin construireMedecin() {
        Medecin m = new Medecin();
        m.setNom(tfMedNom.getText().trim());
        m.setPrenom(tfMedPrenom.getText().trim());
        m.setSpecialite(tfMedSpecialite.getText().trim());
        m.setTelephone(tfMedTelephone.getText().trim());
        m.setEmail(tfMedEmail.getText().trim());
        
        if (m.getNom().isEmpty() || m.getPrenom().isEmpty()) {
            throw new IllegalArgumentException("Le nom et le prénom sont requis.");
        }
        return m;
    }

    public void setViewMode() {
        tfMedNom.setDisable(true);
        tfMedPrenom.setDisable(true);
        tfMedSpecialite.setDisable(true);
        tfMedTelephone.setDisable(true);
        tfMedEmail.setDisable(true);
        btnAjouter.setVisible(false);
        btnAjouter.setManaged(false);
        btnModifier.setVisible(false);
        btnModifier.setManaged(false);
    }

    private void afficherErreur(String msg) {
        lblMedErreur.setStyle("-fx-text-fill: red;");
        lblMedErreur.setText("⚠ " + msg);
    }
}
