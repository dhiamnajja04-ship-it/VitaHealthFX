package tn.esprit.workshopjdbc.Controllers;

import tn.esprit.workshopjdbc.Services.MedicamentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class NouvellePreescriptionController {

    @FXML private TextField rechercheMedicamentField;
    @FXML private Button rechercherMedicamentBtn;
    @FXML private ListView<MedicamentService.Medicament> resultatsMedicaments;
    @FXML private TextArea medicamentsArea;
    @FXML private TextField dureeField;
    @FXML private TextArea instructionsArea;
    @FXML private Button validerBtn;
    @FXML private Button annulerBtn;

    private TriConsumer onValider;
    private Runnable onAnnuler;

    @FunctionalInterface
    public interface TriConsumer {
        void accept(String medicaments, String duree, String instructions);
    }

    public void setOnValider(TriConsumer callback) { this.onValider = callback; }
    public void setOnAnnuler(Runnable callback)    { this.onAnnuler = callback; }

    @FXML
    public void initialize() {
        rechercherMedicamentBtn.setOnAction(e -> rechercherMedicament());

        resultatsMedicaments.setOnMouseClicked(event -> {
            MedicamentService.Medicament selected = resultatsMedicaments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String current = medicamentsArea.getText();
                medicamentsArea.setText(current.isEmpty() ? selected.toString() : current + "\n" + selected);
                resultatsMedicaments.getItems().clear();
                rechercheMedicamentField.clear();
            }
        });

        validerBtn.setOnAction(e -> {
            String med = medicamentsArea.getText().trim();
            String dur = dureeField.getText().trim();
            String ins = instructionsArea.getText().trim();
            if (med.isEmpty() || dur.isEmpty() || ins.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Remplissez tous les champs.", ButtonType.OK).showAndWait();
                return;
            }
            if (onValider != null) onValider.accept(med, dur, ins);
        });

        annulerBtn.setOnAction(e -> { if (onAnnuler != null) onAnnuler.run(); });
    }

    private void rechercherMedicament() {
        String query = rechercheMedicamentField.getText().trim();
        if (query.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Saisissez un nom de médicament.", ButtonType.OK).showAndWait();
            return;
        }
        resultatsMedicaments.getItems().clear();
        resultatsMedicaments.setPlaceholder(new Label("Recherche en cours..."));

        MedicamentService service = new MedicamentService();
        service.rechercher(query).thenAccept(medicaments -> Platform.runLater(() -> {
            List<MedicamentService.Medicament> results = medicaments.isEmpty()
                    ? getLocalMedicaments(query) : medicaments;
            if (results.isEmpty()) {
                resultatsMedicaments.setPlaceholder(new Label("Aucun résultat trouvé."));
                return;
            }
            resultatsMedicaments.getItems().setAll(results);
            resultatsMedicaments.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(MedicamentService.Medicament item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            });
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                List<MedicamentService.Medicament> fallback = getLocalMedicaments(query);
                if (fallback.isEmpty()) {
                    resultatsMedicaments.setPlaceholder(new Label("Erreur de connexion API."));
                } else {
                    resultatsMedicaments.getItems().setAll(fallback);
                    resultatsMedicaments.setCellFactory(lv -> new ListCell<>() {
                        @Override protected void updateItem(MedicamentService.Medicament item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : item.toString());
                        }
                    });
                }
            });
            return null;
        });
    }

    private List<MedicamentService.Medicament> getLocalMedicaments(String query) {
        List<MedicamentService.Medicament> list = new ArrayList<>();
        String q = query.toLowerCase();
        String[][] meds = {
                {"DOLIPRANE",      "500 mg"},  {"DOLIPRANE",      "1000 mg"},
                {"EFFERALGAN",     "500 mg"},  {"EFFERALGAN",     "1000 mg"},
                {"PARACETAMOL",    "500 mg"},  {"PARACETAMOL",    "1000 mg"},
                {"ASPIRINE",       "300 mg"},  {"ASPIRINE",       "500 mg"},
                {"IBUPROFENE",     "200 mg"},  {"IBUPROFENE",     "400 mg"},
                {"ADVIL",          "200 mg"},  {"NUROFEN",        "200 mg"},
                {"AMOXICILLINE",   "500 mg"},  {"AMOXICILLINE",   "1 g"},
                {"AUGMENTIN",      "500 mg"},  {"AUGMENTIN",      "1 g"},
                {"AZITHROMYCINE",  "250 mg"},  {"AZITHROMYCINE",  "500 mg"},
                {"DOXYCYCLINE",    "100 mg"},  {"CLARITHROMYCINE","500 mg"}
        };
        for (String[] m : meds) {
            if (m[0].toLowerCase().contains(q)) {
                MedicamentService.Medicament med = new MedicamentService.Medicament();
                med.setNom(m[0]);
                med.setDosage(m[1]);
                list.add(med);
            }
        }
        return list;
    }
}