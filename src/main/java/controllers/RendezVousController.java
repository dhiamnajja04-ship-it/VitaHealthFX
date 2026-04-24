package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.RendezVous;
import services.RendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RendezVousController implements Initializable {

    @FXML private Label     lblErreur;

    @FXML private TableView<RendezVous>            tableRdv;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String>  colDate;
    @FXML private TableColumn<RendezVous, String>  colHeure;
    @FXML private TableColumn<RendezVous, String>  colMotif;
    @FXML private TableColumn<RendezVous, String>  colPriorite;
    @FXML private TableColumn<RendezVous, Integer> colMedecinId;
    @FXML private TableColumn<RendezVous, String>  colPatientNom;

    private RendezVousService service;
    private ObservableList<RendezVous> listeRdv = FXCollections.observableArrayList();
    
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service = new RendezVousService();
        } catch (SQLException e) {
            afficherErreur("Connexion échouée : " + e.getMessage());
            return;
        }


        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate().toString()));
        colHeure.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getHeure().toString()));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colMedecinId.setCellValueFactory(new PropertyValueFactory<>("medecinId"));
        colPatientNom.setCellValueFactory(new PropertyValueFactory<>("patientNom"));

        // Colonne Priorité avec Badges Colorés
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colPriorite.setCellFactory(column -> new TableCell<RendezVous, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge");
                    
                    if (item.equalsIgnoreCase("Urgente")) {
                        badge.getStyleClass().add("badge-urgent");
                    } else if (item.equalsIgnoreCase("Normale")) {
                        badge.getStyleClass().add("badge-normal");
                    } else if (item.equalsIgnoreCase("Basse")) {
                        badge.getStyleClass().add("badge-low");
                    }
                    
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        tableRdv.setItems(listeRdv);
        chargerDonnees();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleNouveau() {
        if (mainController != null) {
            mainController.openForm("RDV", null);
        }
    }

    @FXML
    private void handleEditSelected() {
        RendezVous rv = tableRdv.getSelectionModel().getSelectedItem();
        if (rv == null) {
            afficherErreur("Sélectionnez un rendez-vous à modifier.");
            return;
        }
        if (mainController != null) {
            mainController.openForm("RDV", rv);
        }
    }

    @FXML
    private void handleSupprimer() {
        RendezVous selected = tableRdv.getSelectionModel().getSelectedItem();
        if (selected == null) { afficherErreur("Sélectionnez un rendez-vous à supprimer."); return; }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
            
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    service.supprimer(selected.getId());
                    chargerDonnees();
                    afficherSucces("Rendez-vous supprimé.");
                } catch (SQLException e) {
                    afficherErreur("Erreur SQL : " + e.getMessage());
                }
            }
        });
    }


    public void chargerDonnees() {
        try {
            listeRdv.setAll(service.afficher());
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
