package com.vitahealth.controller;

import com.vitahealth.dao.AppointmentDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.entity.Role;
import com.vitahealth.entity.User;
import com.vitahealth.entity.Appointment;
import com.vitahealth.util.SessionManager;
import com.vitahealth.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unified admin dashboard controller.
 * Replaces the previous AdminController (FXML) and AdminDashboardController (programmatic) duplicates.
 * This version is programmatic — no FXML file needed for this scene.
 */
public class AdminDashboardController {

    // ── top bar ──────────────────────────────────────────────
    private Label userLabel;
    private Button logoutBtn;

    // ── stat labels ──────────────────────────────────────────
    private Label totalUsersLabel;
    private Label totalPatientsLabel;
    private Label totalDoctorsLabel;
    private Label totalAppointmentsLabel;
    private Label newUsersLabel;
    private Label monthAppointmentsLabel;
    private Label avgWeightLabel;   // renamed: was "avgAgeLabel" but showed avg ID

    // ── user table ───────────────────────────────────────────
    private TextField searchField;
    private TableView<User> userTable;

    // ── appointment table ────────────────────────────────────
    private TextField appointmentSearchField;
    private TableView<Appointment> appointmentTable;

    // ── data ─────────────────────────────────────────────────
    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    private final User currentUser;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────
    // Constructor — receives the logged-in user from LoginController
    // ─────────────────────────────────────────────────────────
    public AdminDashboardController(User currentUser) {
        this.currentUser = currentUser;
    }

    // ─────────────────────────────────────────────────────────
    // Scene builder
    // ─────────────────────────────────────────────────────────
    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");

        root.setTop(buildTopBar());
        root.setCenter(buildTabPane());

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

        // Load data after the scene graph is built
        refreshAll();

        return scene;
    }

    // ─────────────────────────────────────────────────────────
    // Top bar
    // ─────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setStyle("-fx-background-color: #2c3e66; -fx-padding: 15 20;");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(20);

        Text logo = new Text("🌟 VITAHEALTH  —  Admin");
        logo.setStyle("-fx-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        userLabel = new Label("👑 " + (currentUser != null ? currentUser.getFullName() : ""));
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> logout());

        bar.getChildren().addAll(logo, spacer, userLabel, logoutBtn);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    // Tab pane: Dashboard | Users | Appointments
    // ─────────────────────────────────────────────────────────
    private TabPane buildTabPane() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab dashTab = new Tab("📊 Tableau de bord", buildDashboardTab());
        Tab usersTab = new Tab("👥 Utilisateurs", buildUsersTab());
        Tab appTab   = new Tab("📅 Rendez-vous", buildAppointmentsTab());

        tabs.getTabs().addAll(dashTab, usersTab, appTab);
        return tabs;
    }

    // ─── Dashboard tab ───────────────────────────────────────
    private VBox buildDashboardTab() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));

        totalUsersLabel        = statLabel();
        totalPatientsLabel     = statLabel();
        totalDoctorsLabel      = statLabel();
        totalAppointmentsLabel = statLabel();
        newUsersLabel          = statLabel();
        monthAppointmentsLabel = statLabel();
        avgWeightLabel         = statLabel();

        HBox row1 = new HBox(20,
                statCard("👥 Utilisateurs",   totalUsersLabel),
                statCard("🤒 Patients",        totalPatientsLabel),
                statCard("👨‍⚕️ Médecins",        totalDoctorsLabel),
                statCard("📅 Rendez-vous",      totalAppointmentsLabel)
        );
        HBox row2 = new HBox(20,
                statCard("🆕 Derniers inscrits (10)", newUsersLabel),
                statCard("📆 RDV ce mois",     monthAppointmentsLabel),
                statCard("⚖️ Poids moyen patients (kg)", avgWeightLabel)
        );

        for (HBox row : new HBox[]{row1, row2}) {
            row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        }

        vbox.getChildren().addAll(row1, row2);
        return vbox;
    }

    private Label statLabel() {
        Label l = new Label("—");
        l.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e66;");
        return l;
    }

    private VBox statCard(String title, Label valueLabel) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        card.getChildren().addAll(titleLabel, valueLabel);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    // ─── Users tab ───────────────────────────────────────────
    private VBox buildUsersTab() {
        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(16));

        searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher par nom ou email...");
        searchField.setMaxWidth(320);
        searchField.textProperty().addListener((obs, old, val) -> searchUsers());

        Button addBtn     = new Button("➕ Ajouter");
        Button refreshBtn = new Button("🔄 Actualiser");
        addBtn.setOnAction(e -> openAddUserDialog());
        refreshBtn.setOnAction(e -> { loadAllUsers(); loadDashboardStats(); });

        HBox toolbar = new HBox(10, searchField, addBtn, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        userTable = buildUserTable();

        vbox.getChildren().addAll(toolbar, userTable);
        VBox.setVgrow(userTable, Priority.ALWAYS);
        return vbox;
    }

    @SuppressWarnings("unchecked")
    private TableView<User> buildUserTable() {
        TableView<User> table = new TableView<>();

        TableColumn<User, Integer> colId        = col("ID",      "id",        60);
        TableColumn<User, String>  colEmail     = col("Email",   "email",     220);
        TableColumn<User, String>  colFirstName = col("Prénom",  "firstName", 120);
        TableColumn<User, String>  colLastName  = col("Nom",     "lastName",  120);
        TableColumn<User, String>  colRole      = col("Rôle",    "role",      100);

        TableColumn<User, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn   = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box         = new HBox(8, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 10;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 10;");
                editBtn.setOnAction(e   -> openEditUserDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(colId, colEmail, colFirstName, colLastName, colRole, colActions);
        table.setItems(usersList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    // ─── Appointments tab ────────────────────────────────────
    private VBox buildAppointmentsTab() {
        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(16));

        appointmentSearchField = new TextField();
        appointmentSearchField.setPromptText("🔍 Rechercher par patient ou médecin...");
        appointmentSearchField.setMaxWidth(320);
        appointmentSearchField.textProperty().addListener((obs, old, val) -> searchAppointments());

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setOnAction(e -> loadAllAppointments());

        HBox toolbar = new HBox(10, appointmentSearchField, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        appointmentTable = buildAppointmentTable();

        vbox.getChildren().addAll(toolbar, appointmentTable);
        VBox.setVgrow(appointmentTable, Priority.ALWAYS);
        return vbox;
    }

    @SuppressWarnings("unchecked")
    private TableView<Appointment> buildAppointmentTable() {
        TableView<Appointment> table = new TableView<>();

        TableColumn<Appointment, Integer> colId      = col("ID",       "id",          60);
        TableColumn<Appointment, String>  colPatient = col("Patient",  "patientName", 160);
        TableColumn<Appointment, String>  colDoctor  = col("Médecin",  "doctorName",  160);
        TableColumn<Appointment, String>  colReason  = col("Motif",    "reason",      180);

        // Date column with formatted display
        TableColumn<Appointment, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setPrefWidth(140);
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.format(DATE_FMT));
            }
        });

        // Status column with color coding
        TableColumn<Appointment, String> colStatus = new TableColumn<>("Statut");
        colStatus.setPrefWidth(110);
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor(item) + ";");
            }
        });

        TableColumn<Appointment, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(100);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("❌ Annuler");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 10;");
                cancelBtn.setOnAction(e -> cancelAppointment(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cancelBtn);
            }
        });

        table.getColumns().addAll(colId, colPatient, colDoctor, colDate, colReason, colStatus, colActions);
        table.setItems(appointmentsList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private String statusColor(String status) {
        switch (status.toUpperCase()) {
            case "SCHEDULED":  return "#f39c12";
            case "CONFIRMED":  return "#27ae60";
            case "COMPLETED":  return "#3498db";
            case "CANCELLED":  return "#e74c3c";
            default:           return "#555";
        }
    }

    // ─────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────
    private void loadDashboardStats() {
        try {
            long total    = userDAO.compterParRole(Role.PATIENT) + userDAO.compterParRole(Role.MEDECIN)
                    + userDAO.compterParRole(Role.ADMIN);
            long patients = userDAO.compterParRole(Role.PATIENT);
            long medecins = userDAO.compterParRole(Role.MEDECIN);
            double avgWeight = userDAO.moyennePoidsPatients();

            totalUsersLabel.setText(String.valueOf(total));
            totalPatientsLabel.setText(String.valueOf(patients));
            totalDoctorsLabel.setText(String.valueOf(medecins));
            avgWeightLabel.setText(String.format("%.1f", avgWeight));

            List<Appointment> allAppointments = appointmentDAO.getAllAppointments();
            totalAppointmentsLabel.setText(String.valueOf(allAppointments.size()));

            // "Derniers inscrits" = last 10 by ID — requires full list
            List<User> allUsers = userDAO.findAll();
            int cutoff = allUsers.size() > 10
                    ? allUsers.stream().mapToInt(User::getId).sorted().toArray()[allUsers.size() - 10]
                    : 0;
            long newUsers = allUsers.stream().filter(u -> u.getId() >= cutoff).count();
            newUsersLabel.setText(String.valueOf(newUsers));

            int currentMonth = LocalDateTime.now().getMonthValue();
            long monthApps = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().getMonthValue() == currentMonth)
                    .count();
            monthAppointmentsLabel.setText(String.valueOf(monthApps));

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les statistiques : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAllUsers() {
        try {
            usersList.setAll(userDAO.findAll());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAllAppointments() {
        try {
            appointmentsList.setAll(appointmentDAO.getAllAppointments());
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les rendez-vous : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Search
    // ─────────────────────────────────────────────────────────
    private void searchUsers() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            userTable.setItems(usersList);
        } else {
            List<User> filtered = usersList.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(kw)
                            || u.getFirstName().toLowerCase().contains(kw)
                            || u.getLastName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
            userTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void searchAppointments() {
        String kw = appointmentSearchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            appointmentTable.setItems(appointmentsList);
        } else {
            List<Appointment> filtered = appointmentsList.stream()
                    .filter(a -> (a.getPatientName() != null && a.getPatientName().toLowerCase().contains(kw))
                            || (a.getDoctorName() != null && a.getDoctorName().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
            appointmentTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    // ─────────────────────────────────────────────────────────
    // CRUD actions — real dialogs
    // ─────────────────────────────────────────────────────────
    private void openAddUserDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Ajouter un utilisateur");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField firstName = new TextField(); firstName.setPromptText("Prénom");
        TextField lastName  = new TextField(); lastName.setPromptText("Nom");
        TextField email     = new TextField(); email.setPromptText("Email");
        PasswordField pwd   = new PasswordField(); pwd.setPromptText("Mot de passe");
        ComboBox<String> roleBox = new ComboBox<>(
                FXCollections.observableArrayList(Role.PATIENT, Role.MEDECIN, Role.ADMIN));
        roleBox.setValue(Role.PATIENT);

        grid.addRow(0, new Label("Prénom:"), firstName);
        grid.addRow(1, new Label("Nom:"),    lastName);
        grid.addRow(2, new Label("Email:"),  email);
        grid.addRow(3, new Label("Mot de passe:"), pwd);
        grid.addRow(4, new Label("Rôle:"),   roleBox);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #e74c3c;");

        Button saveBtn   = new Button("Enregistrer");
        Button cancelBtn = new Button("Annuler");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 10;");
        cancelBtn.setOnAction(e -> dialog.close());
        saveBtn.setOnAction(e -> {
            if (firstName.getText().isBlank() || lastName.getText().isBlank()
                    || email.getText().isBlank() || pwd.getText().isBlank()) {
                errorLbl.setText("Tous les champs sont obligatoires.");
                return;
            }
            User u = new User();
            u.setFirstName(firstName.getText().trim());
            u.setLastName(lastName.getText().trim());
            u.setEmail(email.getText().trim());
            u.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw(pwd.getText(), org.mindrot.jbcrypt.BCrypt.gensalt()));
            u.setRole(roleBox.getValue());
            u.setVerified(false);
            try {
                boolean ok = userDAO.ajouter(u);
                if (ok) {
                    dialog.close();
                    refreshAll();
                } else {
                    errorLbl.setText("Cet email existe déjà.");
                }
            } catch (SQLException ex) {
                errorLbl.setText("Erreur : " + ex.getMessage());
            }
        });

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        grid.add(errorLbl, 0, 5, 2, 1);
        grid.add(buttons,  0, 6, 2, 1);

        dialog.setScene(new Scene(grid));
        dialog.showAndWait();
    }

    private void openEditUserDialog(User user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifier l'utilisateur #" + user.getId());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField firstName = new TextField(user.getFirstName());
        TextField lastName  = new TextField(user.getLastName());
        TextField email     = new TextField(user.getEmail());
        ComboBox<String> roleBox = new ComboBox<>(
                FXCollections.observableArrayList(Role.PATIENT, Role.MEDECIN, Role.ADMIN));
        roleBox.setValue(user.getRole());
        CheckBox verified   = new CheckBox("Compte vérifié");
        verified.setSelected(user.isVerified());

        grid.addRow(0, new Label("Prénom:"), firstName);
        grid.addRow(1, new Label("Nom:"),    lastName);
        grid.addRow(2, new Label("Email:"),  email);
        grid.addRow(3, new Label("Rôle:"),   roleBox);
        grid.addRow(4, verified);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #e74c3c;");

        Button saveBtn   = new Button("Enregistrer");
        Button cancelBtn = new Button("Annuler");
        saveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 10;");
        cancelBtn.setOnAction(e -> dialog.close());
        saveBtn.setOnAction(e -> {
            user.setFirstName(firstName.getText().trim());
            user.setLastName(lastName.getText().trim());
            user.setEmail(email.getText().trim());
            user.setRole(roleBox.getValue());
            user.setVerified(verified.isSelected());
            try {
                userDAO.update(user);
                dialog.close();
                refreshAll();
            } catch (SQLException ex) {
                errorLbl.setText("Erreur : " + ex.getMessage());
            }
        });

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        grid.add(errorLbl, 0, 5, 2, 1);
        grid.add(buttons,  0, 6, 2, 1);

        dialog.setScene(new Scene(grid));
        dialog.showAndWait();
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer " + user.getFullName() + " ?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Supprimer l'utilisateur");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDAO.delete(user.getId());
                    refreshAll();
                } catch (SQLException e) {
                    showAlert("Erreur", "Suppression impossible : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cancelAppointment(Appointment appointment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment annuler ce rendez-vous ?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Annuler le rendez-vous");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = appointmentDAO.cancelAppointment(appointment.getId());
                if (ok) refreshAll();
                else showAlert("Erreur", "Annulation impossible.", Alert.AlertType.ERROR);
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────
    private void refreshAll() {
        loadDashboardStats();
        loadAllUsers();
        loadAllAppointments();
        if (searchField != null) searchField.clear();
        if (appointmentSearchField != null) appointmentSearchField.clear();
    }

    private void logout() {
        SessionManager.getInstance().logout();
        App.changeScene(new LoginController().getScene());
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Generic helper to create a PropertyValueFactory column. */
    private <S, T> TableColumn<S, T> col(String title, String property, double width) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }
}