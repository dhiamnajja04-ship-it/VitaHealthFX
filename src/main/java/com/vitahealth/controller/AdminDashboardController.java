package com.vitahealth.controller;

import com.vitahealth.dao.AppointmentDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.entity.Appointment;
import com.vitahealth.entity.User;
import com.vitahealth.util.SessionManager;
import com.vitahealth.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class AdminDashboardController {

    private Label userLabel;
    private Button logoutBtn;

    private Label totalUsersLabel;
    private Label totalPatientsLabel;
    private Label totalDoctorsLabel;
    private Label totalAppointmentsLabel;
    private Label newUsersLabel;
    private Label monthAppointmentsLabel;
    private Label avgWeightLabel;

    private TextField searchField;
    private TableView<User> userTable;

    private TextField appointmentSearchField;
    private TableView<Appointment> appointmentTable;

    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    private final User currentUser;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminDashboardController(User currentUser) {
        this.currentUser = currentUser;
    }

    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f2f5;");

        root.setTop(buildTopBar());
        root.setCenter(buildTabPane());

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());

        refreshAll();
        return scene;
    }

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

    private TabPane buildTabPane() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab dashTab = new Tab("📊 Tableau de bord", buildDashboardTab());
        Tab usersTab = new Tab("👥 Utilisateurs", buildUsersTab());
        Tab appTab = new Tab("📅 Rendez-vous", buildAppointmentsTab());

        tabs.getTabs().addAll(dashTab, usersTab, appTab);
        return tabs;
    }

    private VBox buildDashboardTab() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));

        totalUsersLabel = statLabel();
        totalPatientsLabel = statLabel();
        totalDoctorsLabel = statLabel();
        totalAppointmentsLabel = statLabel();
        newUsersLabel = statLabel();
        monthAppointmentsLabel = statLabel();
        avgWeightLabel = statLabel();

        HBox row1 = new HBox(20,
                statCard("👥 Utilisateurs", totalUsersLabel, "#667eea"),
                statCard("🤒 Patients", totalPatientsLabel, "#27ae60"),
                statCard("👨‍⚕️ Médecins", totalDoctorsLabel, "#3498db"),
                statCard("📅 Rendez-vous", totalAppointmentsLabel, "#f39c12")
        );
        HBox row2 = new HBox(20,
                statCard("🆕 Derniers inscrits (10)", newUsersLabel, "#9b59b6"),
                statCard("📆 RDV ce mois", monthAppointmentsLabel, "#e67e22"),
                statCard("⚖️ Poids moyen patients (kg)", avgWeightLabel, "#1abc9c")
        );

        for (HBox row : new HBox[]{row1, row2}) {
            row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        }

        vbox.getChildren().addAll(row1, row2);
        return vbox;
    }

    private Label statLabel() {
        Label l = new Label("—");
        l.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        return l;
    }

    private VBox statCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14px; -fx-font-weight: 500;");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox buildUsersTab() {
        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(16));

        searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher par nom ou email...");
        searchField.setMaxWidth(320);
        searchField.textProperty().addListener((obs, old, val) -> searchUsers());

        Button addBtn = new Button("➕ Ajouter");
        Button refreshBtn = new Button("🔄 Actualiser");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");

        addBtn.setOnAction(e -> openAddUserDialog());
        refreshBtn.setOnAction(e -> {
            loadAllUsers();
            loadDashboardStats();
        });

        HBox toolbar = new HBox(10, searchField, addBtn, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        userTable = buildUserTable();

        vbox.getChildren().addAll(toolbar, userTable);
        VBox.setVgrow(userTable, Priority.ALWAYS);
        return vbox;
    }

    private TableView<User> buildUserTable() {
        TableView<User> table = new TableView<>();

        TableColumn<User, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(220);

        TableColumn<User, String> colFirstName = new TableColumn<>("Prénom");
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colFirstName.setPrefWidth(120);

        TableColumn<User, String> colLastName = new TableColumn<>("Nom");
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colLastName.setPrefWidth(120);

        TableColumn<User, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setPrefWidth(100);

        TableColumn<User, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(8, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");
                editBtn.setOnAction(e -> openEditUserDialog(getTableView().getItems().get(getIndex())));
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
        return table;
    }

    private VBox buildAppointmentsTab() {
        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(16));

        appointmentSearchField = new TextField();
        appointmentSearchField.setPromptText("🔍 Rechercher par patient ou médecin...");
        appointmentSearchField.setMaxWidth(320);
        appointmentSearchField.textProperty().addListener((obs, old, val) -> searchAppointments());

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadAllAppointments());

        HBox toolbar = new HBox(10, appointmentSearchField, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        appointmentTable = buildAppointmentTable();

        vbox.getChildren().addAll(toolbar, appointmentTable);
        VBox.setVgrow(appointmentTable, Priority.ALWAYS);
        return vbox;
    }

    private TableView<Appointment> buildAppointmentTable() {
        TableView<Appointment> table = new TableView<>();

        TableColumn<Appointment, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Appointment, String> colPatient = new TableColumn<>("Patient");
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPatient.setPrefWidth(160);

        TableColumn<Appointment, String> colDoctor = new TableColumn<>("Médecin");
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDoctor.setPrefWidth(160);

        TableColumn<Appointment, String> colReason = new TableColumn<>("Motif");
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colReason.setPrefWidth(180);

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

        TableColumn<Appointment, String> colStatus = new TableColumn<>("Statut");
        colStatus.setPrefWidth(110);
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                String color;
                switch (item.toUpperCase()) {
                    case "SCHEDULED": color = "#f39c12"; break;
                    case "CONFIRMED": color = "#27ae60"; break;
                    case "COMPLETED": color = "#3498db"; break;
                    case "CANCELLED": color = "#e74c3c"; break;
                    default: color = "#555";
                }
                setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });

        TableColumn<Appointment, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(100);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("❌ Annuler");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");
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
        return table;
    }

    private void loadDashboardStats() {
        try {
            List<User> allUsers = userDAO.getAllUsers();
            totalUsersLabel.setText(String.valueOf(allUsers.size()));

            long patients = allUsers.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
            long doctors = allUsers.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();

            totalPatientsLabel.setText(String.valueOf(patients));
            totalDoctorsLabel.setText(String.valueOf(doctors));
            avgWeightLabel.setText("—");

            List<Appointment> allAppointments = appointmentDAO.getAllAppointments();
            totalAppointmentsLabel.setText(String.valueOf(allAppointments.size()));

            int cutoff = allUsers.size() > 10 ? allUsers.get(allUsers.size() - 10).getId() : 0;
            long newUsers = allUsers.stream().filter(u -> u.getId() >= cutoff).count();
            newUsersLabel.setText(String.valueOf(newUsers));

            int currentMonth = LocalDateTime.now().getMonthValue();
            long monthApps = allAppointments.stream()
                    .filter(a -> a.getDate() != null && a.getDate().getMonthValue() == currentMonth)
                    .count();
            monthAppointmentsLabel.setText(String.valueOf(monthApps));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllUsers() {
        try {
            usersList.setAll(userDAO.getAllUsers());
            userTable.setItems(usersList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllAppointments() {
        appointmentsList.setAll(appointmentDAO.getAllAppointments());
        appointmentTable.setItems(appointmentsList);
    }

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

    private void openAddUserDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Ajouter un utilisateur");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField firstName = new TextField();
        firstName.setPromptText("Prénom");
        TextField lastName = new TextField();
        lastName.setPromptText("Nom");
        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField pwd = new PasswordField();
        pwd.setPromptText("Mot de passe");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("PATIENT", "DOCTOR", "ADMIN"));
        roleBox.setValue("PATIENT");

        grid.addRow(0, new Label("Prénom:"), firstName);
        grid.addRow(1, new Label("Nom:"), lastName);
        grid.addRow(2, new Label("Email:"), email);
        grid.addRow(3, new Label("Mot de passe:"), pwd);
        grid.addRow(4, new Label("Rôle:"), roleBox);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #e74c3c;");

        Button saveBtn = new Button("Enregistrer");
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
            u.setPassword(pwd.getText());
            u.setRole(roleBox.getValue());

            boolean ok = userDAO.createUser(u);
            if (ok) {
                dialog.close();
                refreshAll();
            } else {
                errorLbl.setText("Cet email existe déjà.");
            }
        });

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        grid.add(errorLbl, 0, 5, 2, 1);
        grid.add(buttons, 0, 6, 2, 1);

        dialog.setScene(new Scene(grid));
        dialog.showAndWait();
    }

    private void openEditUserDialog(User user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modifier l'utilisateur #" + user.getId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField firstName = new TextField(user.getFirstName());
        TextField lastName = new TextField(user.getLastName());
        TextField email = new TextField(user.getEmail());
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("PATIENT", "DOCTOR", "ADMIN"));
        roleBox.setValue(user.getRole());

        grid.addRow(0, new Label("Prénom:"), firstName);
        grid.addRow(1, new Label("Nom:"), lastName);
        grid.addRow(2, new Label("Email:"), email);
        grid.addRow(3, new Label("Rôle:"), roleBox);

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #e74c3c;");

        Button saveBtn = new Button("Enregistrer");
        Button cancelBtn = new Button("Annuler");
        saveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 10;");
        cancelBtn.setOnAction(e -> dialog.close());
        saveBtn.setOnAction(e -> {
            user.setFirstName(firstName.getText().trim());
            user.setLastName(lastName.getText().trim());
            user.setEmail(email.getText().trim());
            user.setRole(roleBox.getValue());

            boolean ok = userDAO.updateUser(user);
            if (ok) {
                dialog.close();
                refreshAll();
            } else {
                errorLbl.setText("Erreur lors de la mise à jour.");
            }
        });

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        grid.add(errorLbl, 0, 4, 2, 1);
        grid.add(buttons, 0, 5, 2, 1);

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
                if (userDAO.deleteUser(user.getId())) {
                    refreshAll();
                } else {
                    showAlert("Erreur", "Suppression impossible.", Alert.AlertType.ERROR);
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
                if (ok) {
                    refreshAll();
                } else {
                    showAlert("Erreur", "Annulation impossible.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void refreshAll() {
        loadDashboardStats();
        loadAllUsers();
        loadAllAppointments();
        if (searchField != null) searchField.clear();
        if (appointmentSearchField != null) appointmentSearchField.clear();
    }

    private void logout() {
        SessionManager.getInstance().logout();
        LoginController loginController = new LoginController();
        Scene scene = loginController.getScene();
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("VitaHealthFX - Connexion");
        stage.show();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}