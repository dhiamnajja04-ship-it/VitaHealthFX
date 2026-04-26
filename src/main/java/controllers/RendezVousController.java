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
import javafx.scene.layout.HBox;
import models.RendezVous;
import services.RendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import services.MedecinService;
import models.Medecin;
import java.time.LocalDate;

public class RendezVousController implements Initializable {

    @FXML private Label     lblErreur;

    @FXML private TableView<RendezVous> tableRdv;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String>  colDate;
    @FXML private TableColumn<RendezVous, String>  colHeure;
    @FXML private TableColumn<RendezVous, String>  colMotif;
    @FXML private TableColumn<RendezVous, String>  colPriorite;
    @FXML private TableColumn<RendezVous, String> colMedecinId;
    @FXML private TableColumn<RendezVous, String>  colPatientNom;
    @FXML private TableColumn<RendezVous, Void>    colActions;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFiltrePriorite;
    @FXML private Pagination pagination;
    @FXML private Label lblCount, lblTotalRdv, lblUrgentRdv, lblTodayRdv;

    private RendezVousService service;
    private MedecinService medecinService;
    private Map<Integer, String> medecinNames = new HashMap<>();
    private ObservableList<RendezVous> listeRdv = FXCollections.observableArrayList();
    private FilteredList<RendezVous> filteredData;
    
    private MainController mainController;
    private static final int ROWS_PER_PAGE = 10;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service = new RendezVousService();
            medecinService = new MedecinService();
        } catch (SQLException e) {
            afficherErreur("Connexion échouée : " + e.getMessage());
            return;
        }


        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate() != null ? cd.getValue().getDate().toString() : ""));
        colHeure.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getHeure() != null ? cd.getValue().getHeure().toString() : ""));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colMedecinId.setCellValueFactory(cd -> {
            int medId = cd.getValue().getMedecinId();
            return new SimpleStringProperty(medecinNames.getOrDefault(medId, "Inconnu"));
        });
        colPatientNom.setCellValueFactory(cd -> new SimpleStringProperty(
            (cd.getValue().getPatientPrenom() != null ? cd.getValue().getPatientPrenom() : "") + " " + 
            (cd.getValue().getPatientNom() != null ? cd.getValue().getPatientNom() : "")
        ));

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

        // Colonne Actions (Bouton View)
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnView = new Button("👁 Voir");
            {
                btnView.getStyleClass().add("btn-info");
                btnView.setOnAction(event -> {
                    RendezVous rv = getTableView().getItems().get(getIndex());
                    if (mainController != null) mainController.openForm("RDV", rv, true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnView);
            }
        });

        setupFiltrage();
        chargerDonnees();
    }

    private void setupFiltrage() {
        cbFiltrePriorite.setItems(FXCollections.observableArrayList("Tous", "Normale", "Urgente", "Basse"));
        cbFiltrePriorite.setValue("Tous");

        filteredData = new FilteredList<>(listeRdv, p -> true);

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        cbFiltrePriorite.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());

        SortedList<RendezVous> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableRdv.comparatorProperty());
        
        // Initialiser Pagination
        pagination.setPageFactory(this::createPage);
        updatePagination();
    }

    private void updateFilters() {
        String searchText = tfSearch.getText() != null ? tfSearch.getText().toLowerCase() : "";
        String priorityFilter = cbFiltrePriorite.getValue();

        filteredData.setPredicate(rv -> {
            boolean matchesSearch = searchText.isEmpty() || 
                rv.getPatientNom().toLowerCase().contains(searchText) ||
                rv.getPatientPrenom().toLowerCase().contains(searchText) ||
                rv.getMotif().toLowerCase().contains(searchText);
            
            boolean matchesPriority = priorityFilter == null || priorityFilter.equals("Tous") || 
                rv.getPriorite().equals(priorityFilter);

            return matchesSearch && matchesPriority;
        });
        
        updatePagination();
        lblCount.setText(filteredData.size() + " résultat(s)");
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredData.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        tableRdv.setItems(createPage(0).getItems()); // Fallback for direct update
    }

    private TableView<RendezVous> createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredData.size());
        
        ObservableList<RendezVous> pageItems = FXCollections.observableArrayList();
        if (fromIndex < filteredData.size()) {
            pageItems.addAll(filteredData.subList(fromIndex, toIndex));
        }
        
        tableRdv.setItems(pageItems);
        return tableRdv;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleNouveau() {
        if (mainController != null) {
            mainController.openForm("RDV", null, false);
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
            mainController.openForm("RDV", rv, false);
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
            List<Medecin> medecins = medecinService.afficher();
            medecinNames.clear();
            for (Medecin m : medecins) {
                medecinNames.put(m.getId(), m.getPrenom() + " " + m.getNom());
            }
            listeRdv.setAll(service.afficher());
            updateFilters();
            calculerStats();
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

    private void calculerStats() {
        if (lblTotalRdv == null) return;
        
        long total = listeRdv.size();
        long urgent = listeRdv.stream().filter(r -> "Urgente".equalsIgnoreCase(r.getPriorite())).count();
        long today = listeRdv.stream().filter(r -> r.getDate() != null && r.getDate().equals(LocalDate.now())).count();
        
        lblTotalRdv.setText(String.valueOf(total));
        lblUrgentRdv.setText(String.valueOf(urgent));
        lblTodayRdv.setText(String.valueOf(today));
    }
}
