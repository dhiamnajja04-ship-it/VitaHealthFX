package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.RendezVous;
import models.ReponseRendezVous;
import services.ReponseRendezVousService;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;

public class ReponseRendezVousController implements Initializable {

    @FXML private Label                lblErreur;

    @FXML private TableView<ReponseRendezVous>            tableReponse;
    @FXML private TableColumn<ReponseRendezVous, Integer> colId;
    @FXML private TableColumn<ReponseRendezVous, String>  colMessage;
    @FXML private TableColumn<ReponseRendezVous, String>  colType;
    @FXML private TableColumn<ReponseRendezVous, String>  colMedecin;
    @FXML private TableColumn<ReponseRendezVous, String>  colDate;
    @FXML private TableColumn<ReponseRendezVous, Integer> colRdvId;

    @FXML private TextField tfSearch;

    private ReponseRendezVousService service;
    private ObservableList<ReponseRendezVous> listeReponses = FXCollections.observableArrayList();
    private FilteredList<ReponseRendezVous> filteredData;
    
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
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecinNom"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getDateReponse() != null ? cd.getValue().getDateReponse().toString() : ""));
        colRdvId.setCellValueFactory(new PropertyValueFactory<>("rendezVousId"));

        // colActions removed based on "leave only supprimer" request
        
        setupSearch();
        chargerDonnees();
    }
 
    private void setupSearch() {
        filteredData = new FilteredList<>(listeReponses, p -> true);

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase();
            filteredData.setPredicate(r -> {
                if (filter == null || filter.isEmpty()) return true;
                return r.getMessage().toLowerCase().contains(filter) || 
                       r.getTypeReponse().toLowerCase().contains(filter) ||
                       (r.getMedecinNom() != null && r.getMedecinNom().toLowerCase().contains(filter));
            });
        });

        SortedList<ReponseRendezVous> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableReponse.comparatorProperty());
        tableReponse.setItems(sortedData);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'historique CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        fileChooser.setInitialFileName("historique_rdv.csv");

        File file = fileChooser.showSaveDialog(tableReponse.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Header
                writer.println("ID;RDV_ID;Médecin;Type;Date;Message");

                for (ReponseRendezVous r : listeReponses) {
                    writer.printf("%d;%d;%s;%s;%s;%s%n",
                        r.getId(),
                        r.getRendezVousId(),
                        r.getMedecinNom() != null ? r.getMedecinNom() : "Inconnu",
                        r.getTypeReponse(),
                        r.getDateReponse() != null ? r.getDateReponse().toString() : "",
                        r.getMessage().replace(";", ",") // Eviter de casser le CSV
                    );
                }
                afficherSucces("Exportation réussie : " + file.getName());
            } catch (Exception e) {
                afficherErreur("Erreur lors de l'export : " + e.getMessage());
            }
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
