package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur principal modernized.
 * Gère la navigation par Sidebar et l'injection dynamique des vues dans le contentArea.
 */
public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button btnRdv, btnMedecins, btnReponses, btnEspaceMedecin;

    private Map<String, Node> viewCache = new HashMap<>();
    private Map<String, Object> controllerCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger la vue par défaut (Rendez-vous)
        showRendezVous();
    }

    @FXML
    public void showRendezVous() {
        switchView("/rendezvous-view.fxml", btnRdv);
    }

    @FXML
    public void showMedecins() {
        switchView("/medecin-view.fxml", btnMedecins);
    }

    @FXML
    public void showReponses() {
        switchView("/reponse-view.fxml", btnReponses);
    }

    @FXML
    public void showEspaceMedecin() {
        switchView("/espace-medecin-view.fxml", btnEspaceMedecin);
    }

    /**
     * Change la vue affichée au centre et met à jour le style du bouton actif.
     */
    private void switchView(String fxmlPath, Button activeBtn) {
        try {
            Node view = viewCache.get(fxmlPath);
            if (view == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                view = loader.load();
                viewCache.put(fxmlPath, view);
                
                Object ctrl = loader.getController();
                controllerCache.put(fxmlPath, ctrl);
                setupSubController(ctrl);
            }

            // Conserver le fond décoratif (index 0) et remplacer le contenu
            if (contentArea.getChildren().size() > 1) {
                contentArea.getChildren().remove(1, contentArea.getChildren().size());
            }
            contentArea.getChildren().add(view);
            
            // Animation combinée : Fondu + Glissement
            FadeTransition ft = new FadeTransition(Duration.millis(400), view);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), view);
            tt.setFromX(20);
            tt.setToX(0);
            
            ft.play();
            tt.play();

            updateButtonStyles(activeBtn);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSubController(Object ctrl) {
        if (ctrl instanceof MedecinController c) c.setMainController(this);
        else if (ctrl instanceof RendezVousController c) c.setMainController(this);
        else if (ctrl instanceof ReponseRendezVousController c) c.setMainController(this);
        else if (ctrl instanceof EspaceMedecinController c) c.setMainController(this);
    }

    private void updateButtonStyles(Button activeBtn) {
        btnRdv.getStyleClass().remove("sidebar-btn-active");
        btnMedecins.getStyleClass().remove("sidebar-btn-active");
        btnReponses.getStyleClass().remove("sidebar-btn-active");
        btnEspaceMedecin.getStyleClass().remove("sidebar-btn-active");

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-btn-active");
        }
    }

    /**
     * Rafraîchit les données dans tous les contrôleurs chargés.
     */
    public void rafraichirListes() {
        for (Object ctrl : controllerCache.values()) {
            if (ctrl instanceof MedecinController c) c.chargerDonnees();
            else if (ctrl instanceof RendezVousController c) c.chargerDonnees();
            else if (ctrl instanceof ReponseRendezVousController c) c.chargerDonnees();
            else if (ctrl instanceof EspaceMedecinController c) c.handleRefresh();
        }
    }

    public void openForm(String type, Object data, boolean isViewOnly) {
        try {
            String fxmlFile;
            if (isViewOnly) {
                // Mode Vue : toujours la fiche unifiée
                fxmlFile = "/consultation-fiche.fxml";
            } else {
                fxmlFile = switch (type.toUpperCase()) {
                    case "MEDECIN" -> "/medecin-form.fxml";
                    case "RDV"     -> "/rendezvous-form.fxml";
                    case "REPONSE" -> "/reponse-form.fxml";
                    default -> throw new IllegalArgumentException("Type inconnu : " + type);
                };
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            URL css = getClass().getResource("/css/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("VitalHealth — " + (isViewOnly ? "Consultation" : "Édition"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(scene);

            Object ctrl = loader.getController();

            if (isViewOnly) {
                // Fiche de lecture seule
                if (ctrl instanceof ConsultationFicheController c) {
                    c.setData(data);
                }
            } else {
                // Formulaires d'édition / création
                switch (type.toUpperCase()) {
                    case "MEDECIN" -> {
                        if (ctrl instanceof MedecinFormController c) {
                            c.setMainController(this);
                            c.setMedecin(data instanceof models.Medecin m ? m : null);
                        }
                    }
                    case "RDV" -> {
                        if (ctrl instanceof RdvFormController c) {
                            c.setMainController(this);
                            c.setRendezVous(data instanceof models.RendezVous rv ? rv : null);
                        }
                    }
                    case "REPONSE" -> {
                        if (ctrl instanceof ReponseFormController c) {
                            c.setMainController(this);
                            if (data instanceof models.ReponseRendezVous rep) c.setReponse(rep);
                            else if (data instanceof models.RendezVous rdv)   c.setRendezVous(rdv);
                        }
                    }
                }
            }

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            utils.NotificationUtils.showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }
}
