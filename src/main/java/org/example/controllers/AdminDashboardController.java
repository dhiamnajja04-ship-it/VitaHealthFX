package org.example.controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.entity.User;
import org.example.service.ServiceVitaHealth;
import org.example.App;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboardController {
    private User currentUser;
    private ServiceVitaHealth service = new ServiceVitaHealth();
    private TableView<User> tableView;
    private ObservableList<User> userList;
    private Label statsLabel;
    private TextField searchField;
    private ComboBox<String> searchTypeCombo;
    private StackPane centerContent;

    public AdminDashboardController(User user) {
        this.currentUser = user;
    }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Centre
        centerContent = new StackPane();
        centerContent.setPadding(new Insets(20));
        centerContent.setStyle("-fx-background-color: #f0f2f5;");
        root.setCenter(centerContent);

        // Afficher le dashboard par défaut
        showDashboard();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        return scene;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e66, #1a4d8c);");

        Text title = new Text("👑 VITAHEALTH - ADMIN");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: white;");

        Text userInfo = new Text("Admin: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        userInfo.setStyle("-fx-fill: #c8d9f0; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪 Déconnexion");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> App.showLoginView());

        header.getChildren().addAll(title, spacer, userInfo, logoutBtn);
        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");

        // Profil résumé
        VBox profileCard = new VBox(8);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-padding: 15;");

        Text avatar = new Text("👑");
        avatar.setStyle("-fx-font-size: 40px;");

        Text name = new Text(currentUser.getFirstName() + " " + currentUser.getLastName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #2c3e66;");

        Text role = new Text("Administrateur");
        role.setStyle("-fx-font-size: 11px; -fx-fill: #6c757d;");

        profileCard.getChildren().addAll(avatar, name, role);
        sidebar.getChildren().add(profileCard);

        Separator separator = new Separator();
        separator.setPadding(new Insets(15, 0, 15, 0));
        sidebar.getChildren().add(separator);

        // Menu items
        String[][] menuItems = {
                {"🏠", "dashboard", "Tableau de bord"},
                {"📊", "users", "Gestion des utilisateurs"},
                {"🔍", "rechercheSQL", "Recherche SQL"},
                {"⚡", "rechercheStream", "Recherche STREAM"},
                {"📈", "stats", "Statistiques"}
        };

        for (String[] item : menuItems) {
            Button btn = new Button(item[0] + "  " + item[2]);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-padding: 12 15; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-font-size: 13px;");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> {
                resetSidebarSelection(sidebar);
                btn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-padding: 12 15; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-font-size: 13px; -fx-background-radius: 10;");
                navigateTo(item[1]);
            });
            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }

    private void resetSidebarSelection(VBox sidebar) {
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                ((Button) node).setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-padding: 12 15; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-font-size: 13px;");
            }
        });
    }

    private void navigateTo(String action) {
        switch (action) {
            case "dashboard": showDashboard(); break;
            case "users": showUserList(); break;
            case "rechercheSQL": showRechercheSQL(); break;
            case "rechercheStream": showRechercheStream(); break;
            case "stats": showStats(); break;
        }
    }

    private void showDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.setAlignment(Pos.CENTER);
        dashboard.setPadding(new Insets(30));

        // Carte de bienvenue
        VBox welcomeCard = new VBox(10);
        welcomeCard.setAlignment(Pos.CENTER);
        welcomeCard.setStyle("-fx-background-color: linear-gradient(to right, #2c3e66, #1a4d8c); -fx-background-radius: 20; -fx-padding: 30;");
        welcomeCard.setMaxWidth(600);

        Text welcome = new Text("👑 Bienvenue " + currentUser.getFirstName() + " !");
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: white;");

        Text info = new Text("Panneau d'administration - Gestion des utilisateurs");
        info.setStyle("-fx-font-size: 14px; -fx-fill: #c8d9f0;");

        welcomeCard.getChildren().addAll(welcome, info);

        // Statistiques
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        try {
            List<User> users = service.getAllUsers();
            long nbPatients = users.stream().filter(u -> u.getRole().equals("patient")).count();
            long nbMedecins = users.stream().filter(u -> u.getRole().equals("medecin")).count();
            long nbAdmins = users.stream().filter(u -> u.getRole().equals("admin")).count();

            VBox patientCard = createStatCard("👥 Patients", String.valueOf(nbPatients));
            VBox medecinCard = createStatCard("👨‍⚕️ Médecins", String.valueOf(nbMedecins));
            VBox adminCard = createStatCard("👑 Admins", String.valueOf(nbAdmins));

            statsBox.getChildren().addAll(patientCard, medecinCard, adminCard);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dashboard.getChildren().addAll(welcomeCard, statsBox);
        centerContent.getChildren().clear();
        centerContent.getChildren().add(dashboard);
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-min-width: 160; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-size: 14px; -fx-fill: #6c757d;");

        Text valueText = new Text(value);
        valueText.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-fill: #2c3e66;");

        card.getChildren().addAll(titleText, valueText);
        return card;
    }

    private void showUserList() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Barre d'outils
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Rafraîchir");
        refreshBtn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadUserList());

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        Button toggleBtn = new Button("🔓 Activer/Désactiver");
        toggleBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #333; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        toggleBtn.setOnAction(e -> toggleUserStatus());

        toolbar.getChildren().addAll(refreshBtn, deleteBtn, toggleBtn);

        // Tableau
        tableView = new TableView<>();
        tableView.setStyle("-fx-background-radius: 10;");

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<User, String> nameCol = new TableColumn<>("Nom complet");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        nameCol.setPrefWidth(200);

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        TableColumn<User, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isVerified() ? "✅ Activé" : "⏳ Désactivé"));
        statusCol.setPrefWidth(100);

        TableColumn<User, String> specialiteCol = new TableColumn<>("Spécialité");
        specialiteCol.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        specialiteCol.setPrefWidth(150);

        tableView.getColumns().addAll(idCol, emailCol, nameCol, roleCol, statusCol, specialiteCol);
        tableView.setPrefHeight(500);

        content.getChildren().addAll(toolbar, tableView);

        loadUserList();

        centerContent.getChildren().clear();
        centerContent.getChildren().add(content);
    }

    private void loadUserList() {
        try {
            List<User> users = service.getAllUsers();
            userList = FXCollections.observableArrayList(users);
            tableView.setItems(userList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
    }

    private void deleteSelectedUser() {
        User selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez sélectionner un utilisateur");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur");
        confirm.setContentText("Supprimer " + selected.getFirstName() + " " + selected.getLastName() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.deleteUser(selected.getId());
                loadUserList();
                showAlert("Succès", "Utilisateur supprimé !");
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    private void toggleUserStatus() {
        User selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez sélectionner un utilisateur");
            return;
        }

        try {
            selected.setVerified(!selected.isVerified());
            service.updateUser(selected);
            loadUserList();
            showAlert("Succès", "Statut modifié !");
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void showRechercheSQL() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Barre de recherche
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll("Par nom", "Par email", "Par rôle");
        searchTypeCombo.setValue("Par nom");
        searchTypeCombo.setPrefWidth(150);

        searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-padding: 8; -fx-background-radius: 10;");

        Button searchBtn = new Button("🔍 Rechercher");
        searchBtn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        searchBtn.setOnAction(e -> rechercherSQL());

        Button resetBtn = new Button("🔄 Réinitialiser");
        resetBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        resetBtn.setOnAction(e -> loadUserList());

        searchBox.getChildren().addAll(searchTypeCombo, searchField, searchBtn, resetBtn);

        // Tableau des résultats
        TableView<User> resultTable = new TableView<>();
        resultTable.setStyle("-fx-background-radius: 10;");

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<User, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        nameCol.setPrefWidth(200);

        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        resultTable.getColumns().addAll(idCol, emailCol, nameCol, roleCol);
        resultTable.setPrefHeight(500);

        content.getChildren().addAll(searchBox, resultTable);

        // Stocker la table pour la recherche
        TableView<User> finalResultTable = resultTable;
        searchBtn.setOnAction(e -> {
            try {
                String keyword = searchField.getText();
                String type = searchTypeCombo.getValue();
                List<User> results = null;

                switch (type) {
                    case "Par nom":
                        results = service.rechercherUtilisateursParNom(keyword);
                        break;
                    case "Par email":
                        results = service.rechercherUtilisateursParEmail(keyword);
                        break;
                    case "Par rôle":
                        results = service.rechercherUtilisateursParRole(keyword);
                        break;
                }

                if (results != null) {
                    finalResultTable.setItems(FXCollections.observableArrayList(results));
                }
            } catch (SQLException ex) {
                showAlert("Erreur", ex.getMessage());
            }
        });

        resetBtn.setOnAction(e -> {
            searchField.clear();
            finalResultTable.setItems(null);
        });

        centerContent.getChildren().clear();
        centerContent.getChildren().add(content);
    }

    private void rechercherSQL() {
        // La recherche est déjà gérée dans le bouton
    }

    private void showRechercheStream() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Barre de recherche
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll("Par nom (Stream)", "Par email (Stream)", "Par rôle (Stream)");
        searchTypeCombo.setValue("Par nom (Stream)");
        searchTypeCombo.setPrefWidth(180);

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-padding: 8; -fx-background-radius: 10;");

        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        Button searchBtn = new Button("🔍 Rechercher (Stream)");
        searchBtn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(15);
        resultArea.setStyle("-fx-font-family: monospace;");

        searchBtn.setOnAction(e -> {
            try {
                String keyword = searchField.getText();
                String type = searchTypeCombo.getValue();
                List<User> results = null;

                switch (type) {
                    case "Par nom (Stream)":
                        results = service.rechercherUtilisateursParNomStream(keyword);
                        break;
                    case "Par email (Stream)":
                        results = service.rechercherUtilisateursParEmailStream(keyword);
                        break;
                    case "Par rôle (Stream)":
                        results = service.rechercherUtilisateursParRoleStream(keyword);
                        break;
                }

                if (results == null || results.isEmpty()) {
                    resultArea.setText("Aucun résultat trouvé pour : " + keyword);
                    resultLabel.setText("0 résultat");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%-5s | %-25s | %-20s | %-10s\n", "ID", "Email", "Nom", "Rôle"));
                    sb.append("------------------------------------------------------------\n");
                    for (User u : results) {
                        sb.append(String.format("%-5d | %-25s | %-20s | %-10s\n",
                                u.getId(), u.getEmail(), u.getFirstName() + " " + u.getLastName(), u.getRole()));
                    }
                    resultArea.setText(sb.toString());
                    resultLabel.setText(results.size() + " résultat(s)");
                }
            } catch (SQLException ex) {
                resultArea.setText("Erreur: " + ex.getMessage());
            }
        });

        content.getChildren().addAll(searchBox, searchTypeCombo, searchField, searchBtn, resultLabel, resultArea);
        centerContent.getChildren().clear();
        centerContent.getChildren().add(content);
    }

    private void showStats() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        try {
            long nbPatients = service.compterUtilisateursParRole("patient");
            long nbMedecins = service.compterUtilisateursParRole("medecin");
            long nbAdmins = service.compterUtilisateursParRole("admin");
            double moyennePoids = service.moyennePoidsPatients();

            VBox statsCard = new VBox(15);
            statsCard.setAlignment(Pos.CENTER);
            statsCard.setMaxWidth(500);
            statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

            Text title = new Text("📈 STATISTIQUES");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: #2c3e66;");

            VBox statsGrid = new VBox(10);
            statsGrid.setAlignment(Pos.CENTER);
            statsGrid.getChildren().addAll(
                    createStatRow("👥 Nombre de patients :", String.valueOf(nbPatients)),
                    createStatRow("👨‍⚕️ Nombre de médecins :", String.valueOf(nbMedecins)),
                    createStatRow("👑 Nombre d'administrateurs :", String.valueOf(nbAdmins)),
                    createStatRow("⚖️ Moyenne des poids :", String.format("%.1f kg", moyennePoids))
            );

            statsCard.getChildren().addAll(title, statsGrid);
            content.getChildren().add(statsCard);
        } catch (SQLException e) {
            content.getChildren().add(new Label("Erreur: " + e.getMessage()));
        }

        centerContent.getChildren().clear();
        centerContent.getChildren().add(content);
    }

    private HBox createStatRow(String label, String value) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8 0;");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e66;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(labelText, spacer, valueText);
        return row;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}