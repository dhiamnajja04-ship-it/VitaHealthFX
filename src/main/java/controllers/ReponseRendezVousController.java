package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.RendezVous;
import models.ReponseRendezVous;
import services.ReponseRendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReponseRendezVousController implements Initializable {

    @FXML private Label                lblErreur;

    @FXML private TableView<ReponseRendezVous>            tableReponse;
    @FXML private TableColumn<ReponseRendezVous, Integer> colId;
    @FXML private TableColumn<ReponseRendezVous, String>  colMessage;
    @FXML private TableColumn<ReponseRendezVous, String>  colType;
    @FXML private TableColumn<ReponseRendezVous, String>  colDate;
    @FXML private TableColumn<ReponseRendezVous, Integer> colRdvId;

    private ReponseRendezVousService service;
    private ObservableList<ReponseRendezVous> listeReponses = FXCollections.observableArrayList();
    
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service    = new ReponseRendezVousService();
        } catch (SQLException e) {
            afficherErreur("Connexion échouée : " + e.getMessage());
            return;
        }



        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeReponse"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getDateReponse() != null ? cd.getValue().getDateReponse().toString() : ""));
        colRdvId.setCellValueFactory(new PropertyValueFactory<>("rendezVousId"));

        tableReponse.setItems(listeReponses);
        chargerDonnees();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleNouveau() {
        if (mainController != null) {
            mainController.openForm("REPONSE", null);
        }
    }

    @FXML
    private void handleEditSelected() {
        ReponseRendezVous r = tableReponse.getSelectionModel().getSelectedItem();
        if (r == null) {
            afficherErreur("Sélectionnez une réponse à modifier.");
            return;
        }
        if (mainController != null) {
            mainController.openForm("REPONSE", r);
        }
    }

    @FXML
    private void handleSupprimer() {
        ReponseRendezVous selected = tableReponse.getSelectionModel().getSelectedItem();
        if (selected == null) { afficherErreur("Sélectionnez une réponse à supprimer."); return; }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer cette réponse ?", ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    service.supprimer(selected.getId());
                    chargerDonnees();
                    afficherSucces("Réponse supprimée.");
                } catch (SQLException e) {
                    afficherErreur("Erreur SQL : " + e.getMessage());
                }
            }
        });
    }

    public void chargerDonnees() {
        try {
            listeReponses.setAll(service.afficher());
            lblErreur.setText("");
        } catch (SQLException e) {
            afficherErreur("Chargement échoué : " + e.getMessage());
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
