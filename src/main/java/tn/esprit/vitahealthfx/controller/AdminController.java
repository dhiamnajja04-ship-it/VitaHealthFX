package tn.esprit.vitahealthfx.controller;

import tn.esprit.vitahealthfx.dao.UserDAO;
import tn.esprit.vitahealthfx.entity.User;
import tn.esprit.vitahealthfx.util.SessionManager;
import tn.esprit.vitahealthfx.util.PDFExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    @FXML private Label adminNameLabel, totalUsersLabel, totalDoctorsLabel, totalPatientsLabel, newUsersLabel, statusLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colFirstName, colLastName, colEmail, colRole, colPhone;
    @FXML private TableColumn<User, LocalDateTime> colCreatedAt;

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> masterList = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        User currentAdmin = SessionManager.getInstance().getCurrentUser();
        if (currentAdmin != null) adminNameLabel.setText("👋 " + currentAdmin.getFullName());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colCreatedAt.setCellFactory(column -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(formatter));
            }
        });

        roleFilter.setValue("Tous");
        roleFilter.setOnAction(e -> searchUsers());
        loadAllUsers();
        updateStats();
    }

    private void loadAllUsers() {
        try {
            masterList.setAll(userDAO.findAll());
            userTable.setItems(masterList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs");
        }
    }

    private void updateStats() {
        long total = masterList.size();
        long doctors = masterList.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();
        long patients = masterList.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
        long newUsers = masterList.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30))).count();

        totalUsersLabel.setText(String.valueOf(total));
        totalDoctorsLabel.setText(String.valueOf(doctors));
        totalPatientsLabel.setText(String.valueOf(patients));
        newUsersLabel.setText(String.valueOf(newUsers));
    }

    @FXML
    public void searchUsers() {
        String keyword = searchField.getText().toLowerCase().trim();
        String role = roleFilter.getValue();

        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User u : masterList) {
            boolean matchKeyword = keyword.isEmpty() ||
                    u.getFirstName().toLowerCase().contains(keyword) ||
                    u.getLastName().toLowerCase().contains(keyword) ||
                    u.getEmail().toLowerCase().contains(keyword);
            boolean matchRole = role.equals("Tous") || u.getRole().equals(role);
            if (matchKeyword && matchRole) filtered.add(u);
        }
        userTable.setItems(filtered);
        statusLabel.setText("🔍 " + filtered.size() + " résultat(s)");
    }

    @FXML
    public void refreshTable() {
        searchField.clear();
        roleFilter.setValue("Tous");
        loadAllUsers();
        updateStats();
        statusLabel.setText("✅ Liste actualisée");
    }

    @FXML
    public void openAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("➕ Ajouter un utilisateur");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField firstName = new TextField(); firstName.setPromptText("Prénom");
        TextField lastName = new TextField(); lastName.setPromptText("Nom");
        TextField email = new TextField(); email.setPromptText("Email");
        TextField phone = new TextField(); phone.setPromptText("Téléphone");
        PasswordField password = new PasswordField(); password.setPromptText("Mot de passe");
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("PATIENT", "DOCTOR", "ADMIN"));
        role.setValue("PATIENT");

        grid.add(new Label("Prénom :"), 0, 0); grid.add(firstName, 1, 0);
        grid.add(new Label("Nom :"), 0, 1); grid.add(lastName, 1, 1);
        grid.add(new Label("Email :"), 0, 2); grid.add(email, 1, 2);
        grid.add(new Label("Téléphone :"), 0, 3); grid.add(phone, 1, 3);
        grid.add(new Label("Mot de passe :"), 0, 4); grid.add(password, 1, 4);
        grid.add(new Label("Rôle :"), 0, 5); grid.add(role, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                User u = new User();
                u.setFirstName(firstName.getText().trim());
                u.setLastName(lastName.getText().trim());
                u.setEmail(email.getText().trim());
                u.setPhone(phone.getText().trim());
                u.setPassword(BCrypt.hashpw(password.getText(), BCrypt.gensalt()));
                u.setRole(role.getValue());
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            try {
                if (userDAO.register(user)) {
                    loadAllUsers();
                    updateStats();
                    statusLabel.setText("✅ Utilisateur ajouté !");
                } else {
                    statusLabel.setText("❌ Email déjà utilisé");
                }
            } catch (Exception e) {
                statusLabel.setText("❌ Erreur : " + e.getMessage());
            }
        });
    }

    @FXML
    public void openEditUserDialog() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Sélectionnez un utilisateur");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("✏️ Modifier l'utilisateur");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField firstName = new TextField(selected.getFirstName());
        TextField lastName = new TextField(selected.getLastName());
        TextField email = new TextField(selected.getEmail());
        TextField phone = new TextField(selected.getPhone());
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("PATIENT", "DOCTOR", "ADMIN"));
        role.setValue(selected.getRole());

        grid.add(new Label("Prénom :"), 0, 0); grid.add(firstName, 1, 0);
        grid.add(new Label("Nom :"), 0, 1); grid.add(lastName, 1, 1);
        grid.add(new Label("Email :"), 0, 2); grid.add(email, 1, 2);
        grid.add(new Label("Téléphone :"), 0, 3); grid.add(phone, 1, 3);
        grid.add(new Label("Rôle :"), 0, 4); grid.add(role, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                selected.setFirstName(firstName.getText().trim());
                selected.setLastName(lastName.getText().trim());
                selected.setEmail(email.getText().trim());
                selected.setPhone(phone.getText().trim());
                selected.setRole(role.getValue());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            try {
                if (userDAO.update(user)) {
                    loadAllUsers();
                    updateStats();
                    statusLabel.setText("✅ Utilisateur modifié !");
                } else {
                    statusLabel.setText("❌ Erreur modification");
                }
            } catch (SQLException e) {
                statusLabel.setText("❌ Erreur : " + e.getMessage());
            }
        });
    }

    @FXML
    public void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Sélectionnez un utilisateur");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selected.getFullName() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userDAO.delete(selected.getId());
                    loadAllUsers();
                    updateStats();
                    statusLabel.setText("✅ Utilisateur supprimé");
                } catch (SQLException e) {
                    statusLabel.setText("❌ Erreur suppression");
                }
            }
        });
    }

    @FXML
    public void exportCSV() {
        try (PrintWriter writer = new PrintWriter(new File("utilisateurs_" + LocalDate.now() + ".csv"))) {
            writer.println("ID,Prénom,Nom,Email,Rôle,Téléphone,Date inscription");
            for (User u : userTable.getItems()) {
                String createdAt = u.getCreatedAt() != null ? u.getCreatedAt().format(formatter) : "";
                writer.printf("%d,%s,%s,%s,%s,%s,%s%n",
                        u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                        u.getRole(), u.getPhone(), createdAt);
            }
            statusLabel.setText("✅ Export CSV réussi !");
        } catch (FileNotFoundException e) {
            statusLabel.setText("❌ Erreur export");
        }
    }

    @FXML
    public void exportPDF() {
        List<User> users = new ArrayList<>(userTable.getItems());
        PDFExporter.exportUsersToPDF(users, (Stage) userTable.getScene().getWindow());
    }

    @FXML
    public void logout() {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion - VITA");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}