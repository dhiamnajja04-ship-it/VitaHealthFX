package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Medecin;
import models.RendezVous;
import models.ReponseRendezVous;
import services.MedecinService;
import services.RendezVousService;
import services.ReponseRendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EspaceMedecinController implements Initializable {

    @FXML private ComboBox<Medecin> cbMedecinSession;
    @FXML private Label lblBadgeNotif, lblTotalRdv, lblAttenteRdv, lblConfirmeRdv;

    @FXML private TableView<RendezVous> tableNouveauxRdv;
    @FXML private TableColumn<RendezVous, String> colDate, colHeure, colPatient, colMotif, colPriorite;
    @FXML private TableColumn<RendezVous, Void> colAction;

    @FXML private TableView<ReponseRendezVous> tableMesReponses;
    @FXML private TableColumn<ReponseRendezVous, String> colRepDate, colRepRdvInfo, colRepType, colRepMessage;

    private MedecinService           medService;
    private RendezVousService        rdvService;
    private ReponseRendezVousService repService;
    
    private final ObservableList<Medecin>           listeMedecins = FXCollections.observableArrayList();
    private final ObservableList<RendezVous>        listeNouveaux = FXCollections.observableArrayList();
    private final ObservableList<ReponseRendezVous> listeReponses = FXCollections.observableArrayList();

    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            medService = new MedecinService();
            rdvService = new RendezVousService();
            repService = new ReponseRendezVousService();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setupTables();
        chargerMedecins();

        cbMedecinSession.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) chargerDonneesMedecin(newV.getId());
            else viderDonnees();
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupTables() {
        // Table Nouveaux RDV
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));
        colHeure.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHeure().toString()));
        colPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientNom() + " " + d.getValue().getPatientPrenom()));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        
        // Priorité avec Badges Colorés
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colPriorite.setCellFactory(column -> new TableCell<RendezVous, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge");
                    if (item.equalsIgnoreCase("Urgente")) badge.getStyleClass().add("badge-urgent");
                    else if (item.equalsIgnoreCase("Normale")) badge.getStyleClass().add("badge-normal");
                    else if (item.equalsIgnoreCase("Basse")) badge.getStyleClass().add("badge-low");
                    setGraphic(badge);
                }
            }
        });

        setupActionColumn();
        tableNouveauxRdv.setItems(listeNouveaux);

        // Table Mes Réponses
        colRepDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateReponse().toString().replace("T", " ")));
        colRepRdvInfo.setCellValueFactory(d -> {
            // Dans un cas réel, on ferait un JOIN. Ici on affiche au moins l'ID du RDV.
            return new SimpleStringProperty("RDV #" + d.getValue().getRendezVousId());
        });
        colRepType.setCellValueFactory(new PropertyValueFactory<>("typeReponse"));
        colRepMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        tableMesReponses.setItems(listeReponses);
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRepondre = new Button("Répondre");
            {
                btnRepondre.getStyleClass().add("btn-info");
                btnRepondre.setStyle("-fx-font-size: 11px;");
                btnRepondre.setOnAction(event -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    handleRepondre(rdv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnRepondre);
            }
        });
    }

    private void chargerMedecins() {
        try {
            listeMedecins.setAll(medService.afficher());
            cbMedecinSession.setItems(listeMedecins);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerDonneesMedecin(int medecinId) {
        try {
            // 1. Charger tous les RDV pour les stats
            List<RendezVous> tousLesRdv = rdvService.getByMedecin(medecinId);
            long total    = tousLesRdv.size();
            long attente  = tousLesRdv.stream().filter(r -> "en_attente".equals(r.getStatut())).count();
            long confirme = tousLesRdv.stream().filter(r -> "confirme".equals(r.getStatut())).count();

            lblTotalRdv.setText(String.valueOf(total));
            lblAttenteRdv.setText(String.valueOf(attente));
            lblConfirmeRdv.setText(String.valueOf(confirme));
            lblBadgeNotif.setText(String.valueOf(attente));

            // 2. Filtrer les nouveaux (en_attente) pour la table
            listeNouveaux.setAll(tousLesRdv.stream().filter(r -> "en_attente".equals(r.getStatut())).toList());

            // 3. Charger les réponses
            listeReponses.setAll(repService.getReponsesByMedecinId(medecinId));

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void viderDonnees() {
        listeNouveaux.clear();
        listeReponses.clear();
        lblTotalRdv.setText("0");
        lblAttenteRdv.setText("0");
        lblConfirmeRdv.setText("0");
        lblBadgeNotif.setText("0");
    }

    @FXML
    public void handleRefresh() {
        Medecin m = cbMedecinSession.getValue();
        if (m != null) chargerDonneesMedecin(m.getId());
    }

    private void handleRepondre(RendezVous rdv) {
        if (mainController != null) {
            mainController.openForm("REPONSE", rdv); 
        }
    }
}
