package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Medecin;
import services.MedecinService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MedecinController implements Initializable {

    @FXML private Label lblErreur;

    @FXML private TableView<Medecin> tableMedecin;
    @FXML private TableColumn<Medecin, Integer> colId;
    @FXML private TableColumn<Medecin, String>  colNom;
    @FXML private TableColumn<Medecin, String>  colPrenom;
    @FXML private TableColumn<Medecin, String>  colSpecialite;
    @FXML private TableColumn<Medecin, String>  colTelephone;
    @FXML private TableColumn<Medecin, String>  colEmail;

    private MedecinService service;
    private ObservableList<Medecin> listeMedecins = FXCollections.observableArrayList();
    
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service = new MedecinService();
        } catch (SQLException e) {
            afficherErreur("Erreur de connexion : " + e.getMessage());
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colSpecialite.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableMedecin.setItems(listeMedecins);
        chargerDonnees();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleNouveau() {
        if (mainController != null) {
            mainController.openForm("MEDECIN", null);
        }
    }

    @FXML
    private void handleEditSelected() {
        Medecin m = tableMedecin.getSelectionModel().getSelectedItem();
        if (m == null) {
            afficherErreur("Veuillez sélectionner un médecin à modifier.");
            return;
        }
        if (mainController != null) {
            mainController.openForm("MEDECIN", m);
        }
    }

    @FXML
    private void handleSupprimer() {
        Medecin selected = tableMedecin.getSelectionModel().getSelectedItem();
        if (selected == null) { afficherErreur("Sélectionnez un médecin à supprimer."); return; }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le médecin " + selected.getNom() + " ?",
            ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    service.supprimer(selected.getId());
                    chargerDonnees();
                    afficherSucces("Médecin supprimé.");
                } catch (SQLException e) {
                    afficherErreur("Erreur SQL : " + e.getMessage());
                }
            }
        });
    }


    public void chargerDonnees() {
        try {
            listeMedecins.setAll(service.afficher());
            lblErreur.setText("");
        } catch (SQLException e) {
            afficherErreur("Impossible de charger les médecins : " + e.getMessage());
        }
    }

    private void afficherErreur(String msg) {
        lblErreur.setStyle("-fx-text-fill: red;");
        lblErreur.setText("⚠ " + msg);
    }

    private void afficherSucces(String msg) {
        lblErreur.setStyle("-fx-text-fill: green;");
        lblErreur.setText("✔ " + msg);
    }
}
