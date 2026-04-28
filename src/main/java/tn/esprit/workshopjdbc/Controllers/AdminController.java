package tn.esprit.workshopjdbc.Controllers;

import tn.esprit.workshopjdbc.dao.AppointmentDAO;
import tn.esprit.workshopjdbc.dao.UserDAO;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Entities.Appointment;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminController {

    // ========== FXML COMPOSANTS ==========
    @FXML private Label userLabel;
    @FXML private Button logoutBtn;

    @FXML private Label totalUsersLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label newUsersLabel;
    @FXML private Label monthAppointmentsLabel;

    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button addUserBtn;
    @FXML private Button refreshBtn;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private Label avgAgeLabel;

    @FXML private TextField appointmentSearchField;
    @FXML private Button searchAppointmentBtn;
    @FXML private Button refreshAppointmentBtn;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, Integer> appColId;
    @FXML private TableColumn<Appointment, String> appColPatient;
    @FXML private TableColumn<Appointment, String> appColDoctor;
    @FXML private TableColumn<Appointment, LocalDateTime> appColDate;
    @FXML private TableColumn<Appointment, String> appColReason;
    @FXML private TableColumn<Appointment, String> appColStatus;
    @FXML private TableColumn<Appointment, Void> appColActions;

    private UserDAO userDAO;
    private AppointmentDAO appointmentDAO;
    private ObservableList<User> usersList;
    private ObservableList<Appointment> appointmentsList;
    private User currentUser;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        appointmentDAO = new AppointmentDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            userLabel.setText("👑 " + currentUser.getFullName());
        }

        setupUserTable();
        setupAppointmentTable();
        loadDashboardStats();
        loadAllUsers();
        loadAllAppointments();

        searchBtn.setOnAction(e -> searchUsers());
        addUserBtn.setOnAction(e -> openAddUserDialog());
        refreshBtn.setOnAction(e -> refreshAll());
        searchAppointmentBtn.setOnAction(e -> searchAppointments());
        refreshAppointmentBtn.setOnAction(e -> loadAllAppointments());
        logoutBtn.setOnAction(e -> logout());

        applyButtonStyles();
    }

    // ========== CONFIGURATION DES STYLES ==========
    private void applyButtonStyles() {
        searchBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        addUserBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        searchAppointmentBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        refreshAppointmentBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");

        addHoverAnimation(searchBtn);
        addHoverAnimation(addUserBtn);
        addHoverAnimation(refreshBtn);
        addHoverAnimation(searchAppointmentBtn);
        addHoverAnimation(refreshAppointmentBtn);
    }

    private void addHoverAnimation(Button btn) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void setupUserTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colId.setSortType(TableColumn.SortType.ASCENDING);
        colEmail.setSortable(true);
        colFirstName.setSortable(true);
        colLastName.setSortable(true);
        colRole.setSortable(true);

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            private final HBox buttons = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12; -fx-cursor: hand;");

                editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #5dade2; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;"));
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;"));

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    openEditUserDialog(user);
                });
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(buttons);
            }
        });
    }

    private void setupAppointmentTable() {
        appColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        appColPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        appColDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        appColReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        appColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        appColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        appColDate.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(dateFormatter));
            }
        });

        appColStatus.setCellFactory(column -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toUpperCase()) {
                        case "SCHEDULED": setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); break;
                        case "CONFIRMED": setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); break;
                        case "COMPLETED": setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); break;
                        case "CANCELLED": setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); break;
                        default: setStyle("");
                    }
                }
            }
        });

        appColActions.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("❌ Annuler");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10;"));
                cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10;"));
                cancelBtn.setOnAction(e -> {
                    Appointment app = getTableView().getItems().get(getIndex());
                    cancelAppointment(app);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(cancelBtn);
            }
        });
    }

    private void loadDashboardStats() {
        try {
            List<User> allUsers = userDAO.findAll();
            totalUsersLabel.setText(String.valueOf(allUsers.size()));

            long patientsCount = allUsers.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
            long doctorsCount = allUsers.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();

            totalPatientsLabel.setText(String.valueOf(patientsCount));
            totalDoctorsLabel.setText(String.valueOf(doctorsCount));

            List<Appointment> allAppointments = appointmentDAO.getAllAppointments();
            totalAppointmentsLabel.setText(String.valueOf(allAppointments.size()));

            long newUsers = allUsers.stream().filter(u -> u.getId() > (allUsers.size() - 10)).count();
            newUsersLabel.setText(String.valueOf(newUsers));

            long monthApps = allAppointments.stream().filter(a -> {
                if (a.getDate() == null) return false;
                return a.getDate().getMonthValue() == LocalDateTime.now().getMonthValue();
            }).count();
            monthAppointmentsLabel.setText(String.valueOf(monthApps));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les statistiques", Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void loadAllUsers() {
        try {
            usersList = FXCollections.observableArrayList(userDAO.findAll());
            userTable.setItems(usersList);
            double avgId = usersList.stream().mapToInt(User::getId).average().orElse(0);
            avgAgeLabel.setText(String.format("%.1f", avgId));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les utilisateurs", Alert.AlertType.ERROR);
        }
    }

    private void loadAllAppointments() {
        appointmentsList = FXCollections.observableArrayList(appointmentDAO.getAllAppointments());
        appointmentTable.setItems(appointmentsList);
    }

    private void searchUsers() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            loadAllUsers();
        } else {
            List<User> filtered = usersList.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(keyword) ||
                            u.getFirstName().toLowerCase().contains(keyword) ||
                            u.getLastName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            userTable.setItems(FXCollections.observableArrayList(filtered));
            avgAgeLabel.setText("📊 Résultats: " + filtered.size());
        }
    }

    private void searchAppointments() {
        String keyword = appointmentSearchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            loadAllAppointments();
        } else {
            List<Appointment> filtered = appointmentsList.stream()
                    .filter(a -> (a.getPatientName() != null && a.getPatientName().toLowerCase().contains(keyword)) ||
                            (a.getDoctorName() != null && a.getDoctorName().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
            appointmentTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }
    @FXML
    private void openAddUserDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("➕ Ajouter un utilisateur");
        dialog.setResizable(false);

        VBox dialogVbox = new VBox(20);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        Label titleLabel = new Label("Nouvel utilisateur");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.setItems(FXCollections.observableArrayList("PATIENT", "DOCTOR", "ADMIN"));
        roleCombo.setValue("PATIENT");

        grid.add(new Label("Prénom :"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom :"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email :"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Mot de passe :"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Rôle :"), 0, 4);
        grid.add(roleCombo, 1, 4);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        Button saveBtn = new Button("✅ Enregistrer");
        Button cancelBtn = new Button("❌ Annuler");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 25; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 25; -fx-cursor: hand;");

        HBox buttonBox = new HBox(15, saveBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        dialogVbox.getChildren().addAll(titleLabel, grid, errorLabel, buttonBox);

        saveBtn.setOnAction(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleCombo.getValue();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Tous les champs sont obligatoires !");
                return;
            }

            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);

            try {
                boolean success = userDAO.ajouter(newUser);
                if (success) {
                    dialog.close();
                    refreshAll();
                    showAlert("✅ Succès", "Utilisateur ajouté avec succès !", Alert.AlertType.INFORMATION);
                } else {
                    errorLabel.setText("Cet email existe déjà !");
                }
            } catch (SQLException ex) {
                errorLabel.setText("Erreur : " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(dialogVbox, 420, 420);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void openEditUserDialog(User user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("✏️ Modifier l'utilisateur");
        dialog.setResizable(false);

        VBox dialogVbox = new VBox(20);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        Label titleLabel = new Label("Modifier l'utilisateur");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3498db;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField firstNameField = new TextField(user.getFirstName());
        firstNameField.setPromptText("Prénom");
        TextField lastNameField = new TextField(user.getLastName());
        lastNameField.setPromptText("Nom");
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.setItems(FXCollections.observableArrayList("PATIENT", "DOCTOR", "ADMIN"));
        roleCombo.setValue(user.getRole());

        grid.add(new Label("Prénom :"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom :"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email :"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Rôle :"), 0, 3);
        grid.add(roleCombo, 1, 3);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        Button saveBtn = new Button("💾 Enregistrer");
        Button cancelBtn = new Button("❌ Annuler");
        saveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 25; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 25; -fx-cursor: hand;");

        HBox buttonBox = new HBox(15, saveBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        dialogVbox.getChildren().addAll(titleLabel, grid, errorLabel, buttonBox);

        saveBtn.setOnAction(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String role = roleCombo.getValue();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                errorLabel.setText("Les champs sont obligatoires !");
                return;
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setRole(role);

            try {
                userDAO.update(user);
                dialog.close();
                refreshAll();
                showAlert("✅ Succès", "Utilisateur modifié avec succès !", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                errorLabel.setText("Erreur : " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(dialogVbox, 420, 380);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("⚠️ Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("Voulez-vous vraiment supprimer " + user.getFullName() + " ?\nCette action est irréversible !");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDAO.delete(user.getId());
                    showAlert("✅ Succès", "Utilisateur supprimé avec succès !", Alert.AlertType.INFORMATION);
                    refreshAll();
                } catch (SQLException e) {
                    showAlert("❌ Erreur", "Impossible de supprimer l'utilisateur", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void cancelAppointment(Appointment appointment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("⚠️ Confirmation");
        confirm.setHeaderText("Annuler le rendez-vous ?");
        confirm.setContentText("Voulez-vous vraiment annuler ce rendez-vous ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                appointmentDAO.cancelAppointment(appointment.getId());
                showAlert("✅ Succès", "Rendez-vous annulé", Alert.AlertType.INFORMATION);
                refreshAll();
            }
        });
    }
    @FXML
    private void refreshAll() {
        loadDashboardStats();
        loadAllUsers();
        loadAllAppointments();
        searchField.clear();
        appointmentSearchField.clear();
        avgAgeLabel.setText("📊 Moyenne des IDs");
    }
    @FXML
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