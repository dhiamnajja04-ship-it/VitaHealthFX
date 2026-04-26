package com.vitahealth.view;

import com.vitahealth.service.PharmacieService;
import com.vitahealth.service.PharmacieService.Pharmacy;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class PharmacieView extends BorderPane {

    private final TableView<Pharmacy> tableView = new TableView<>();
    private final ObservableList<Pharmacy> pharmacyList = FXCollections.observableArrayList();
    private final PharmacieService service = new PharmacieService();
    private final Label statusLabel = new Label("📍 Recherche autour de Tunis...");
    private final Button refreshButton = new Button("🔄 Actualiser");

    public PharmacieView() {
        TableColumn<Pharmacy, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Pharmacy, String> addressCol = new TableColumn<>("Adresse");
        addressCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAddress()));
        addressCol.setPrefWidth(300);

        TableColumn<Pharmacy, Double> distanceCol = new TableColumn<>("Distance (km)");
        distanceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDistance()));
        distanceCol.setCellFactory(col -> new TableCell<Pharmacy, Double>() {
            @Override
            protected void updateItem(Double dist, boolean empty) {
                super.updateItem(dist, empty);
                if (empty || dist == null) setText(null);
                else setText(String.format("%.1f", dist));
            }
        });

        TableColumn<Pharmacy, Void> actionCol = new TableColumn<>("Itinéraire");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗺️ Google Maps");
            { btn.setOnAction(e -> {
                Pharmacy p = getTableView().getItems().get(getIndex());
                service.openGoogleMapsDirections(p.getLat(), p.getLon());
            }); }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btn);
            }
        });

        tableView.getColumns().addAll(nameCol, addressCol, distanceCol, actionCol);
        tableView.setItems(pharmacyList);

        refreshButton.setOnAction(e -> rechercherAutomatique());
        VBox top = new VBox(10, statusLabel, refreshButton);
        top.setPadding(new Insets(10));
        setTop(top);
        setCenter(tableView);

        rechercherAutomatique();
    }

    private void rechercherAutomatique() {
        statusLabel.setText("📍 Recherche en cours...");
        statusLabel.setStyle("-fx-text-fill: #2c3e50;");
        refreshButton.setDisable(true);
        service.autoSearchNearby(new PharmacieService.Callback() {
            @Override
            public void onSuccess(List<Pharmacy> pharmacies, double radiusUsed) {
                javafx.application.Platform.runLater(() -> {
                    pharmacyList.setAll(pharmacies);
                    refreshButton.setDisable(false);
                    if (pharmacies.isEmpty()) {
                        statusLabel.setText("❌ Aucune pharmacie trouvée autour de Tunis (jusqu'à 50 km).");
                        statusLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        statusLabel.setText("✅ " + pharmacies.size() + " pharmacie(s) trouvée(s) - Rayon : " + (int)radiusUsed + " km");
                        statusLabel.setStyle("-fx-text-fill: #27ae60;");
                    }
                });
            }

            @Override
            public void onError(String message) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("⚠️ " + message);
                    statusLabel.setStyle("-fx-text-fill: orange;");
                    refreshButton.setDisable(false);
                });
            }
        });
    }
}