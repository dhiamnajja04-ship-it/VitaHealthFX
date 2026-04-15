package com.vitahealth.controller.admin;

import com.vitahealth.dao.AppointmentDAO;
import com.vitahealth.dao.UserDAO;
import com.vitahealth.model.Appointment;
import com.vitahealth.model.User;
import com.vitahealth.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminController {

    @FXML private Label userLabel;
    @FXML private Button logoutBtn;

    // Dashboard stats
    @FXML private Label totalUsersLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label newUsersLabel;
    @FXML private Label monthAppointmentsLabel;

    // User management
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

    // Appointment management
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
    }

    private void setupUserTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 12;");
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

        // ✅ CORRECTION ICI : setCellFactory pour la date
        appColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        appColDate.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        // Style des statuts
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
                        case "SCHEDULED":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "CONFIRMED":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "COMPLETED":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default: setStyle("");
                    }
                }
            }
        });

        appColActions.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("❌");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10;");
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
        List<User> allUsers = userDAO.getAllUsers();
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
    }

    private void loadAllUsers() {
        usersList = FXCollections.observableArrayList(userDAO.getAllUsers());
        userTable.setItems(usersList);
        double avgId = usersList.stream().mapToInt(User::getId).average().orElse(0);
        avgAgeLabel.setText(String.format("%.1f", avgId));
    }

    private void loadAllAppointments() {
        appointmentsList = FXCollections.observableArrayList(appointmentDAO.getAllAppointments());
        appointmentTable.setItems(appointmentsList);
    }

    private void searchUsers() {
        String keyword = searchField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            loadAllUsers();
        } else {
            List<User> filtered = usersList.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(keyword) ||
                            u.getFirstName().toLowerCase().contains(keyword) ||
                            u.getLastName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            userTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void searchAppointments() {
        String keyword = appointmentSearchField.getText().toLowerCase();
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

    private void openAddUserDialog() {
        showAlert("Information", "Fonctionnalite a implementer", Alert.AlertType.INFORMATION);
    }

    private void openEditUserDialog(User user) {
        showAlert("Information", "Fonctionnalite a implementer", Alert.AlertType.INFORMATION);
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("Voulez-vous vraiment supprimer " + user.getFullName() + " ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && userDAO.deleteUser(user.getId())) {
                showAlert("Succes", "Utilisateur supprime", Alert.AlertType.INFORMATION);
                refreshAll();
            }
        });
    }

    private void cancelAppointment(Appointment appointment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler le rendez-vous ?");
        confirm.setContentText("Voulez-vous vraiment annuler ce rendez-vous ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && appointmentDAO.cancelAppointment(appointment.getId())) {
                showAlert("Succes", "Rendez-vous annule", Alert.AlertType.INFORMATION);
                refreshAll();
            }
        });
    }

    private void refreshAll() {
        loadDashboardStats();
        loadAllUsers();
        loadAllAppointments();
        searchField.clear();
        appointmentSearchField.clear();
    }

    private void logout() {
        SessionManager.getInstance().logout();
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loginView, 450, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}