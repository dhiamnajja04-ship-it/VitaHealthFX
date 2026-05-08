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

    // ✅ Labels d'erreur (à ajouter dans le FXML)
    @FXML private Label erreurMedicaments;
    @FXML private Label erreurDuree;
    @FXML private Label erreurInstructions;

    private static final int INSTRUCTIONS_MIN_LENGTH = 10;

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
        cacherErreurs();

        rechercherMedicamentBtn.setOnAction(e -> rechercherMedicament());

        resultatsMedicaments.setOnMouseClicked(event -> {
            MedicamentService.Medicament selected = resultatsMedicaments.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String current = medicamentsArea.getText();
                medicamentsArea.setText(current.isEmpty() ? selected.toString() : current + "\n" + selected);
                resultatsMedicaments.getItems().clear();
                rechercheMedicamentField.clear();
                validerChampMedicaments();
            }
        });

        // ✅ Validation en temps réel
        medicamentsArea.textProperty().addListener((obs, old, val) -> validerChampMedicaments());
        dureeField.textProperty().addListener((obs, old, val) -> validerChampDuree());
        instructionsArea.textProperty().addListener((obs, old, val) -> validerChampInstructions());

        validerBtn.setOnAction(e -> soumettre());
        annulerBtn.setOnAction(e -> { if (onAnnuler != null) onAnnuler.run(); });
    }

    // ================== VALIDATIONS ==================

    private boolean validerChampMedicaments() {
        String val = medicamentsArea.getText().trim();

        // ✅ Contrôle 1 : champ obligatoire non vide
        if (val.isEmpty()) {
            afficherErreur(erreurMedicaments, medicamentsArea, "⚠ Ce champ est obligatoire.");
            return false;
        }
        // ✅ Contrôle 3 : doit être une chaîne de caractères valide (pas que des chiffres)
        if (val.matches("^[0-9]+$")) {
            afficherErreur(erreurMedicaments, medicamentsArea, "⚠ Saisissez un nom de médicament valide.");
            return false;
        }
        cacherErreur(erreurMedicaments, medicamentsArea);
        return true;
    }

    private boolean validerChampDuree() {
        String val = dureeField.getText().trim();

        // ✅ Contrôle 1 : champ obligatoire non vide
        if (val.isEmpty()) {
            afficherErreur(erreurDuree, dureeField, "⚠ Ce champ est obligatoire.");
            return false;
        }
        // ✅ Contrôle 3 : doit contenir au moins un caractère alphabétique (ex: "7 jours" et pas juste "7")
        if (!val.matches(".*[a-zA-ZÀ-ÿ].*")) {
            afficherErreur(erreurDuree, dureeField, "⚠ Précisez l'unité. Ex: 7 jours, 2 semaines, 1 mois");
            return false;
        }
        cacherErreur(erreurDuree, dureeField);
        return true;
    }

    private boolean validerChampInstructions() {
        String val = instructionsArea.getText().trim();

        // ✅ Contrôle 1 : champ obligatoire non vide
        if (val.isEmpty()) {
            afficherErreur(erreurInstructions, instructionsArea, "⚠ Ce champ est obligatoire.");
            return false;
        }
        // ✅ Contrôle 2 : longueur minimale
        if (val.length() < INSTRUCTIONS_MIN_LENGTH) {
            afficherErreur(erreurInstructions, instructionsArea,
                    "⚠ Trop court (min " + INSTRUCTIONS_MIN_LENGTH + " caractères, actuellement " + val.length() + ").");
            return false;
        }
        // ✅ Contrôle 3 : doit contenir des lettres (pas que des chiffres/symboles)
        if (!val.matches(".*[a-zA-ZÀ-ÿ].*")) {
            afficherErreur(erreurInstructions, instructionsArea, "⚠ Saisissez des instructions valides.");
            return false;
        }
        cacherErreur(erreurInstructions, instructionsArea);
        return true;
    }

    private void soumettre() {
        boolean medOk = validerChampMedicaments();
        boolean durOk = validerChampDuree();
        boolean insOk = validerChampInstructions();

        if (!medOk || !durOk || !insOk) {
            // ✅ Animation shake sur le bouton valider
            animerErreur(validerBtn);
            return;
        }

        if (onValider != null) {
            onValider.accept(
                    medicamentsArea.getText().trim(),
                    dureeField.getText().trim(),
                    instructionsArea.getText().trim()
            );
        }
    }

    // ================== HELPERS VISUELS ==================

    private void afficherErreur(Label label, javafx.scene.Node champ, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
        champ.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 6;");
    }

    private void cacherErreur(Label label, javafx.scene.Node champ) {
        if (label != null) {
            label.setVisible(false);
            label.setManaged(false);
        }
        champ.setStyle("-fx-border-color: #dce1e7; -fx-border-width: 1; -fx-border-radius: 6;");
    }

    private void cacherErreurs() {
        if (erreurMedicaments != null) { erreurMedicaments.setVisible(false); erreurMedicaments.setManaged(false); }
        if (erreurDuree != null)       { erreurDuree.setVisible(false);       erreurDuree.setManaged(false); }
        if (erreurInstructions != null){ erreurInstructions.setVisible(false); erreurInstructions.setManaged(false); }
    }

    // ✅ Animation shake si formulaire invalide
    private void animerErreur(javafx.scene.Node node) {
        javafx.animation.TranslateTransition tt =
                new javafx.animation.TranslateTransition(javafx.util.Duration.millis(60), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    // ================== RECHERCHE MÉDICAMENTS ==================

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