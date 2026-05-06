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
import services.RendezVousService;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import services.MedecinService;
import models.Medecin;
import java.time.LocalDate;

public class RendezVousController implements Initializable {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private Label lblErreur;

    @FXML private TableView<RendezVous>            tableRdv;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String>  colDate;
    @FXML private TableColumn<RendezVous, String>  colHeure;
    @FXML private TableColumn<RendezVous, String>  colMotif;
    @FXML private TableColumn<RendezVous, String>  colPriorite;
    @FXML private TableColumn<RendezVous, String>  colMedecinId;
    @FXML private TableColumn<RendezVous, String>  colPatientNom;
    @FXML private TableColumn<RendezVous, Void>    colActions;

    @FXML private TextField       tfSearch;
    @FXML private ComboBox<String> cbFiltrePriorite;
    @FXML private Label            lblCount, lblTotalRdv, lblUrgentRdv, lblTodayRdv;

    // Custom pagination controls
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;
    @FXML private Label  lblPageInfo;

    // ── State ─────────────────────────────────────────────────────────────────
    private RendezVousService service;
    private MedecinService    medecinService;
    private final Map<Integer, String>       medecinNames = new HashMap<>();
    private final ObservableList<RendezVous> listeRdv     = FXCollections.observableArrayList();
    private FilteredList<RendezVous>  filteredData;
    private SortedList<RendezVous>    sortedData;

    private MainController mainController;

    private static final int ROWS_PER_PAGE = 10;
    private int currentPage = 0;

    // ── Initialize ────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            service        = new RendezVousService();
            medecinService = new MedecinService();
        } catch (SQLException e) {
            afficherErreur("Connexion échouée : " + e.getMessage());
            return;
        }
        setupColumns();
        setupFiltrage();
        chargerDonnees();
    }

    // ── Columns ───────────────────────────────────────────────────────────────
    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getDate()  != null ? cd.getValue().getDate().toString()  : ""));
        colHeure.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getHeure() != null ? cd.getValue().getHeure().toString() : ""));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colMedecinId.setCellValueFactory(cd -> new SimpleStringProperty(
            medecinNames.getOrDefault(cd.getValue().getMedecinId(), "Inconnu")));
        colPatientNom.setCellValueFactory(cd -> new SimpleStringProperty(
            nvl(cd.getValue().getPatientPrenom()) + " " + nvl(cd.getValue().getPatientNom())));

        // Badge priorité
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colPriorite.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item.toUpperCase());
                badge.getStyleClass().add("badge");
                if      (item.equalsIgnoreCase("Urgente")) badge.getStyleClass().add("badge-urgent");
                else if (item.equalsIgnoreCase("Normale")) badge.getStyleClass().add("badge-normal");
                else                                       badge.getStyleClass().add("badge-low");
                setGraphic(badge);
            }
        });

        // Bouton Voir — btn créé UNE FOIS par cellule, toujours affiché sur ligne non-vide
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("👁 Voir");
            {
                btn.getStyleClass().add("btn-info");
                btn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        RendezVous rv = getTableView().getItems().get(idx);
                        if (mainController != null) mainController.openForm("RDV", rv, true);
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);                          // toujours vider le texte
                setGraphic(empty ? null : btn);         // afficher/cacher le bouton
            }
        });
    }

    // ── Filtrage & Custom Pagination ──────────────────────────────────────────
    private void setupFiltrage() {
        cbFiltrePriorite.setItems(FXCollections.observableArrayList("Tous", "Normale", "Urgente", "Basse"));
        cbFiltrePriorite.setValue("Tous");

        filteredData = new FilteredList<>(listeRdv, p -> true);
        sortedData   = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableRdv.comparatorProperty());

        tfSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbFiltrePriorite.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void applyFilters() {
        String search   = tfSearch.getText() != null ? tfSearch.getText().toLowerCase() : "";
        String priority = cbFiltrePriorite.getValue();

        filteredData.setPredicate(rv -> {
            boolean ms = search.isEmpty()
                || nvl(rv.getPatientNom()).toLowerCase().contains(search)
                || nvl(rv.getPatientPrenom()).toLowerCase().contains(search)
                || nvl(rv.getMotif()).toLowerCase().contains(search);
            boolean mp = priority == null || "Tous".equals(priority)
                || priority.equals(rv.getPriorite());
            return ms && mp;
        });

        currentPage = 0;
        refreshPage();
        lblCount.setText(filteredData.size() + " résultat(s)");
    }

    private void refreshPage() {
        int total     = sortedData.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / ROWS_PER_PAGE));

        // clamp
        if (currentPage >= pageCount) currentPage = pageCount - 1;
        if (currentPage < 0)          currentPage = 0;

        int from = currentPage * ROWS_PER_PAGE;
        int to   = Math.min(from + ROWS_PER_PAGE, total);

        ObservableList<RendezVous> page = FXCollections.observableArrayList();
        if (from < total) page.addAll(sortedData.subList(from, to));
        tableRdv.setItems(page);
        tableRdv.refresh();   // force le re-rendu de toutes les cellules visibles

        // Update nav bar
        lblPageInfo.setText("Page " + (currentPage + 1) + " / " + pageCount);
        btnPrevPage.setDisable(currentPage == 0);
        btnNextPage.setDisable(currentPage >= pageCount - 1);
    }

    // ── FXML Handlers ─────────────────────────────────────────────────────────
    @FXML private void handlePrevPage() { currentPage--; refreshPage(); }
    @FXML private void handleNextPage() { currentPage++; refreshPage(); }

    @FXML private void handleNouveau() {
        if (mainController != null) mainController.openForm("RDV", null, false);
    }

    @FXML private void handleEditSelected() {
        RendezVous rv = tableRdv.getSelectionModel().getSelectedItem();
        if (rv == null) {
            afficherErreur("Sélectionnez un rendez-vous à modifier.");
            utils.NotificationUtils.showWarning("Sélection requise", "Veuillez sélectionner un rendez-vous à modifier.");
            return;
        }
        if (mainController != null) mainController.openForm("RDV", rv, false);
    }

    @FXML private void handleSupprimer() {
        RendezVous sel = tableRdv.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherErreur("Sélectionnez un rendez-vous à supprimer.");
            utils.NotificationUtils.showWarning("Sélection requise", "Veuillez sélectionner un rendez-vous à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    service.supprimer(sel.getId());
                    chargerDonnees();
                    afficherSucces("Rendez-vous supprimé.");
                    utils.NotificationUtils.showSuccess("Supprimé", "Le rendez-vous du " + sel.getDate() + " a été supprimé.");
                } catch (SQLException e) {
                    afficherErreur("Erreur SQL : " + e.getMessage());
                    utils.NotificationUtils.showError("Erreur", "Suppression échouée : " + e.getMessage());
                }
            }
        });
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    public void chargerDonnees() {
        try {
            List<Medecin> medecins = medecinService.afficher();
            medecinNames.clear();
            medecins.forEach(m -> medecinNames.put(m.getId(), m.getPrenom() + " " + m.getNom()));
            listeRdv.setAll(service.afficher());
            applyFilters();
            calculerStats();
            lblErreur.setText("");
        } catch (SQLException e) {
            afficherErreur("Chargement échoué : " + e.getMessage());
        }
    }

    public void setMainController(MainController mc) { this.mainController = mc; }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void calculerStats() {
        if (lblTotalRdv == null) return;
        long total  = listeRdv.size();
        long urgent = listeRdv.stream().filter(r -> "Urgente".equalsIgnoreCase(r.getPriorite())).count();
        long today  = listeRdv.stream().filter(r -> r.getDate() != null && r.getDate().equals(LocalDate.now())).count();
        lblTotalRdv.setText(String.valueOf(total));
        lblUrgentRdv.setText(String.valueOf(urgent));
        lblTodayRdv.setText(String.valueOf(today));
    }

    private void afficherErreur(String msg) {
        lblErreur.setStyle("-fx-text-fill: red;");
        lblErreur.setText("⚠ " + msg);
    }

    private void afficherSucces(String msg) {
        lblErreur.setStyle("-fx-text-fill: green;");
        lblErreur.setText("✔ " + msg);
    }

    private static String nvl(String s) { return s != null ? s : ""; }
}
