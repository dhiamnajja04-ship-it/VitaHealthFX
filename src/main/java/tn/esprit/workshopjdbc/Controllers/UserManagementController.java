package tn.esprit.workshopjdbc.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Services.UserManagementService;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagementController {
    
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colSpecialite;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private ComboBox<String> sortFieldCombo;
    @FXML private ComboBox<String> sortOrderCombo;
    
    @FXML private Label lblTotal;
    @FXML private Label lblPatients;
    @FXML private Label lblDoctors;
    @FXML private Label lblAdmins;
    @FXML private Label lblVerified;
    
    private UserManagementService service;
    private ObservableList<User> userList;
    private List<User> allUsers;
    
    @FXML
    public void initialize() {
        System.out.println("=== INITIALISATION UserManagementController ===");
        
        service = new UserManagementService();
        userList = FXCollections.observableArrayList();
        
        setupTable();
        setupCombos();
        loadAllUsers();
        loadStats();
        
        // Écouteurs pour recherche et tri
        searchField.textProperty().addListener((obs, old, val) -> {
            System.out.println("🔍 Recherche: " + val);
            applyFiltersAndSort();
        });
        roleFilterCombo.valueProperty().addListener((obs, old, val) -> {
            System.out.println("📋 Filtre rôle: " + val);
            applyFiltersAndSort();
        });
        sortFieldCombo.valueProperty().addListener((obs, old, val) -> {
            System.out.println("📊 Tri par: " + val);
            applyFiltersAndSort();
        });
        sortOrderCombo.valueProperty().addListener((obs, old, val) -> {
            System.out.println("⬆️⬇️ Ordre: " + val);
            applyFiltersAndSort();
        });
    }
    
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colSpecialite.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        
        userTable.setItems(userList);
        System.out.println("✅ Tableau configuré");
    }
    
    private void setupCombos() {
        roleFilterCombo.setItems(FXCollections.observableArrayList("Tous", "PATIENT", "DOCTOR", "ADMIN"));
        roleFilterCombo.setValue("Tous");
        
        sortFieldCombo.setItems(FXCollections.observableArrayList("id", "firstName", "email", "role"));
        sortFieldCombo.setValue("id");
        
        sortOrderCombo.setItems(FXCollections.observableArrayList("desc", "asc"));
        sortOrderCombo.setValue("desc");
        
        System.out.println("✅ Combobox configurées");
    }
    
    private void loadAllUsers() {
        try {
            allUsers = service.getAllUsers();
            userList.setAll(allUsers);
            System.out.println("✅ " + allUsers.size() + " utilisateurs chargés");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
    }
    
    private void loadStats() {
        try {
            UserManagementService.UserStats stats = service.getStats();
            lblTotal.setText(String.valueOf(stats.getTotal()));
            lblPatients.setText(String.valueOf(stats.getPatients()));
            lblDoctors.setText(String.valueOf(stats.getDoctors()));
            lblAdmins.setText(String.valueOf(stats.getAdmins()));
            lblVerified.setText(String.valueOf(stats.getVerified()));
            System.out.println("✅ Stats chargées: Total=" + stats.getTotal());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ==================== FILTRE ET TRI ====================
    
    private void applyFiltersAndSort() {
        if (allUsers == null || allUsers.isEmpty()) {
            System.out.println("⚠️ Aucun utilisateur à filtrer");
            return;
        }
        
        String keyword = searchField.getText();
        String role = roleFilterCombo.getValue();
        String sortField = sortFieldCombo.getValue();
        String sortOrder = sortOrderCombo.getValue();
        
        System.out.println("🔍 Application filtres: keyword='" + keyword + "', role='" + role + "'");
        
        // 1. Copier la liste
        List<User> filtered = allUsers;
        
        // 2. Filtrer par mot clé
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(k)) ||
                                 (u.getLastName() != null && u.getLastName().toLowerCase().contains(k)) ||
                                 (u.getEmail() != null && u.getEmail().toLowerCase().contains(k)))
                    .collect(Collectors.toList());
            System.out.println("📊 Après filtre mot clé: " + filtered.size() + " résultats");
        }
        
        // 3. Filtrer par rôle
        if (role != null && !"Tous".equals(role)) {
            filtered = filtered.stream()
                    .filter(u -> u.getRole() != null && u.getRole().equals(role))
                    .collect(Collectors.toList());
            System.out.println("📊 Après filtre rôle: " + filtered.size() + " résultats");
        }
        
        // 4. Trier
        filtered = applySorting(filtered, sortField, sortOrder);
        
        // 5. Mettre à jour la table
        userList.setAll(filtered);
        System.out.println("✅ Affichage final: " + filtered.size() + " utilisateurs");
    }
    
    private List<User> applySorting(List<User> users, String sortField, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        
        switch (sortField) {
            case "id":
                if (ascending) {
                    users.sort(Comparator.comparingInt(User::getId));
                } else {
                    users.sort(Comparator.comparingInt(User::getId).reversed());
                }
                break;
            case "firstName":
                if (ascending) {
                    users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)));
                } else {
                    users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                }
                break;
            case "email":
                if (ascending) {
                    users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareToIgnoreCase)));
                } else {
                    users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                }
                break;
            case "role":
                users.sort(Comparator.comparing(User::getRole, Comparator.nullsLast(String::compareToIgnoreCase)));
                if (!ascending) {
                    users.sort(Comparator.comparing(User::getRole, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                }
                break;
            default:
                users.sort(Comparator.comparingInt(User::getId).reversed());
        }
        return users;
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    private void handleRefresh() {
        loadAllUsers();
        loadStats();
        applyFiltersAndSort();
        showAlert("Info", "Données rafraîchies !");
    }
    
    @FXML
    private void handleAdvancedSearch() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recherche avancée");
        dialog.setHeaderText("Recherche par spécialité ou téléphone");
        dialog.setContentText("Entrez la spécialité ou le téléphone:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(keyword -> {
            searchField.setText(keyword);
            applyFiltersAndSort();
        });
    }
    
    @FXML
    private void handlePromoteToDoctor() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur");
            return;
        }
        
        if ("DOCTOR".equals(selected.getRole())) {
            showAlert("Info", "Cet utilisateur est déjà DOCTEUR");
            return;
        }
        
        try {
            service.promoteToDoctor(selected.getId());
            loadAllUsers();
            loadStats();
            applyFiltersAndSort();
            showAlert("Succès", selected.getFirstName() + " est maintenant DOCTEUR !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de promouvoir l'utilisateur");
        }
    }
    
    @FXML
    private void handlePromoteToAdmin() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur");
            return;
        }
        
        if ("ADMIN".equals(selected.getRole())) {
            showAlert("Info", "Cet utilisateur est déjà ADMIN");
            return;
        }
        
        try {
            service.promoteToAdmin(selected.getId());
            loadAllUsers();
            loadStats();
            applyFiltersAndSort();
            showAlert("Succès", selected.getFirstName() + " est maintenant ADMIN !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de promouvoir l'utilisateur");
        }
    }
    
    @FXML
    private void handleDemoteToPatient() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur");
            return;
        }
        
        if ("PATIENT".equals(selected.getRole())) {
            showAlert("Info", "Cet utilisateur est déjà PATIENT");
            return;
        }
        
        try {
            service.demoteToPatient(selected.getId());
            loadAllUsers();
            loadStats();
            applyFiltersAndSort();
            showAlert("Succès", selected.getFirstName() + " est maintenant PATIENT !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de rétrograder l'utilisateur");
        }
    }
    
    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + selected.getFirstName() + " " + selected.getLastName() + " ?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.deleteUser(selected.getId());
                loadAllUsers();
                loadStats();
                applyFiltersAndSort();
                showAlert("Succès", "Utilisateur supprimé avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'utilisateur");
            }
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}