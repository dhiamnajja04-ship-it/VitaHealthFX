package tn.esprit.workshopjdbc.Controllers;

import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import tn.esprit.workshopjdbc.Services.UserManagementService;
import tn.esprit.workshopjdbc.dao.AppointmentDAO;
import tn.esprit.workshopjdbc.dao.UserDAO;
import tn.esprit.workshopjdbc.Entities.Appointment;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.QRCodeGenerator;
import tn.esprit.workshopjdbc.Utils.SessionManager;
import tn.esprit.workshopjdbc.Utils.ThemeManager;
import tn.esprit.workshopjdbc.Utils.NotificationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label userLabel;
    @FXML private Button logoutBtn;
    @FXML private Button themeToggleBtn;

    // Dashboard Stats Labels
    @FXML private Label totalUsersLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label newUsersLabel;
    @FXML private Label monthAppointmentsLabel;
    @FXML private Label avgPoidsLabel;
    @FXML private Label avgBmiLabel;

    // Users Tab
    @FXML private TextField searchField;
    @FXML private TableView<User> userTable;
    @FXML private Pagination userPagination;
    @FXML private ComboBox<String> sortFieldCombo;
    @FXML private ComboBox<String> sortOrderCombo;
    @FXML private TableColumn<User, String> colPhone;

    // Appointments Tab
    @FXML private TextField appointmentSearchField;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private Pagination appointmentPagination;

    // Stats Container
    @FXML private HBox statsContainer;
    
    // Dashboard Charts Container
    @FXML private VBox chartsContainer;
    @FXML private StackPane forumModerationContainer;

    // Variables pour les graphiques dynamiques
    private PieChart rolePieChart;
    private BarChart<String, Number> bmiBarChart;

    // DAO and Data
    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final UserManagementService userManagementService = new UserManagementService();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    private List<User> currentUserPageSource = List.of();
    private List<Appointment> currentAppointmentPageSource = List.of();
    private final User currentUser;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int ADMIN_ROWS_PER_PAGE = 8;

    // Tri state
    private String currentSortField = "id";
    private String currentSortOrder = "desc";

    public AdminDashboardController(User currentUser) {
        this.currentUser = currentUser;
    }

    public Scene getScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            System.out.println("DEBUG: FXML loaded and controller linked.");

            if (userLabel != null && currentUser != null) {
                userLabel.setText("👑 " + currentUser.getFullName());
            }

            setupTableColumns();
            setupSortingListeners();
            setupStatsContainer();  // ← CRÉER LES CARTES STATS
            setupDashboardCharts();  // Initialise les graphiques
            loadForumModerationModule();
            refreshAll();            // Charge les données et met à jour tout

            Scene scene = new Scene(root, 1300, 750);
            scene.getStylesheets().add(getClass().getResource("/css/style-modern.css").toExternalForm());
            return scene;
        } catch (IOException e) {
            System.err.println("FATAL: Could not load FXML file.");
            e.printStackTrace();
            return null;
        }
    }

    private void loadForumModerationModule() {
        if (forumModerationContainer == null) return;

        try {
            Parent forumModerationView = FXMLLoader.load(getClass().getResource("/fxml/forum/ForumModerationView.fxml"));
            forumModerationContainer.getChildren().setAll(forumModerationView);
        } catch (IOException e) {
            System.err.println("Could not load forum moderation module: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== CRÉATION DES CARTES STATS ====================
    
    private void setupStatsContainer() {
        if (statsContainer == null) return;
        
        statsContainer.getChildren().clear();
        statsContainer.setSpacing(15);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(10));
        
        statsContainer.getChildren().addAll(
            createStatCardStyled("👥", "Total Utilisateurs", "0", "#667eea"),
            createStatCardStyled("🩺", "Patients", "0", "#27ae60"),
            createStatCardStyled("👨‍⚕️", "Médecins", "0", "#3498db"),
            createStatCardStyled("👑", "Administrateurs", "0", "#9b59b6"),
            createStatCardStyled("📅", "Rendez-vous", "0", "#f39c12"),
            createStatCardStyled("✅", "Taux vérification", "0%", "#1abc9c")
        );
    }
    
    private VBox createStatCardStyled(String icon, String title, String defaultValue, String color) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);");
        card.setPrefWidth(180);
        card.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        Label valueLabel = new Label(defaultValue);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        valueLabel.setUserData(title);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-opacity: 0.85;");
        
        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        addHoverAnimation(card);
        
        return card;
    }
    
    private void updateDashboardCards() {
        if (statsContainer == null) return;
        
        long total = usersList.size();
        long patients = usersList.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
        long doctors = usersList.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();
        long admins = usersList.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long appointments = appointmentsList.size();
        long verified = usersList.stream().filter(User::isVerified).count();
        double verificationRate = total == 0 ? 0 : (double) verified / total * 100;
        
        System.out.println("📊 Mise à jour cartes - Total: " + total + ", Patients: " + patients + ", Médecins: " + doctors + ", Admins: " + admins);
        
        for (Node node : statsContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                if (card.getChildren().size() >= 2) {
                    Node valueNode = card.getChildren().get(1);
                    if (valueNode instanceof Label) {
                        Label valueLabel = (Label) valueNode;
                        String title = (String) valueLabel.getUserData();
                        if (title == null) continue;
                        
                        switch (title) {
                            case "Total Utilisateurs":
                                valueLabel.setText(String.valueOf(total));
                                break;
                            case "Patients":
                                valueLabel.setText(String.valueOf(patients));
                                break;
                            case "Médecins":
                                valueLabel.setText(String.valueOf(doctors));
                                break;
                            case "Administrateurs":
                                valueLabel.setText(String.valueOf(admins));
                                break;
                            case "Rendez-vous":
                                valueLabel.setText(String.valueOf(appointments));
                                break;
                            case "Taux vérification":
                                valueLabel.setText(String.format("%.1f%%", verificationRate));
                                break;
                        }
                    }
                }
            }
        }
    }

    // ==================== DASHBOARD GRAPHIQUES ====================
    
    private void setupDashboardCharts() {
        if (chartsContainer != null) {
            chartsContainer.getChildren().clear();
            chartsContainer.setSpacing(20);
            chartsContainer.setPadding(new Insets(20));
            chartsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            
            Label title = new Label("📊 ANALYSE STATISTIQUE");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e66;");
            
            HBox chartsRow = new HBox(20);
            chartsRow.setAlignment(Pos.CENTER);
            
            rolePieChart = new PieChart();
            rolePieChart.setTitle("👥 Répartition des utilisateurs");
            rolePieChart.setPrefSize(350, 280);
            rolePieChart.setLabelsVisible(true);
            
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Catégorie IMC");
            yAxis.setLabel("Nombre de patients");
            bmiBarChart = new BarChart<>(xAxis, yAxis);
            bmiBarChart.setTitle("📊 Distribution IMC des patients");
            bmiBarChart.setPrefSize(450, 280);
            bmiBarChart.setLegendVisible(false);
            
            chartsRow.getChildren().addAll(rolePieChart, bmiBarChart);
            chartsContainer.getChildren().addAll(title, chartsRow);
        }
    }
    
    private void updateCharts() {
        if (rolePieChart != null) {
            rolePieChart.getData().clear();
            
            long patients = usersList.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
            long doctors = usersList.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();
            long admins = usersList.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            
            PieChart.Data patientData = new PieChart.Data("Patients (" + patients + ")", patients);
            PieChart.Data doctorData = new PieChart.Data("Médecins (" + doctors + ")", doctors);
            PieChart.Data adminData = new PieChart.Data("Admins (" + admins + ")", admins);
            
            rolePieChart.getData().addAll(patientData, doctorData, adminData);
            
            patientData.getNode().setStyle("-fx-pie-color: #27ae60;");
            doctorData.getNode().setStyle("-fx-pie-color: #3498db;");
            adminData.getNode().setStyle("-fx-pie-color: #9b59b6;");
        }
        
        if (bmiBarChart != null) {
            bmiBarChart.getData().clear();
            
            List<User> patients = usersList.stream()
                    .filter(u -> "PATIENT".equals(u.getRole()))
                    .collect(Collectors.toList());
            
            long underweight = patients.stream()
                    .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                    .filter(p -> (p.getPoids() / (p.getTaille() * p.getTaille())) < 18.5)
                    .count();
            
            long normal = patients.stream()
                    .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                    .filter(p -> {
                        double bmi = p.getPoids() / (p.getTaille() * p.getTaille());
                        return bmi >= 18.5 && bmi < 25;
                    })
                    .count();
            
            long overweight = patients.stream()
                    .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                    .filter(p -> {
                        double bmi = p.getPoids() / (p.getTaille() * p.getTaille());
                        return bmi >= 25 && bmi < 30;
                    })
                    .count();
            
            long obese = patients.stream()
                    .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                    .filter(p -> {
                        double bmi = p.getPoids() / (p.getTaille() * p.getTaille());
                        return bmi >= 30;
                    })
                    .count();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Patients");
            series.getData().add(new XYChart.Data<>("Insuffisance\n(<18.5)", underweight));
            series.getData().add(new XYChart.Data<>("Normal\n(18.5-25)", normal));
            series.getData().add(new XYChart.Data<>("Surpoids\n(25-30)", overweight));
            series.getData().add(new XYChart.Data<>("Obésité\n(>30)", obese));
            
            bmiBarChart.getData().add(series);
        }
    }

    // ==================== CONFIGURATION TABLEAU ====================
    
    private void setupTableColumns() {
        if (userTable == null) return;
        
        userTable.getColumns().clear();
        
        TableColumn<User, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        
        TableColumn<User, String> colFirstName = new TableColumn<>("Prénom");
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colFirstName.setPrefWidth(120);
        
        TableColumn<User, String> colLastName = new TableColumn<>("Nom");
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colLastName.setPrefWidth(120);
        
        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);
        
        TableColumn<User, String> colRole = new TableColumn<>("Rôle");
        colRole.setPrefWidth(100);
        colRole.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(role.toUpperCase());
                    badge.getStyleClass().addAll("badge");
                    if (role.equalsIgnoreCase("ADMIN")) {
                        badge.getStyleClass().add("badge-admin");
                    } else if (role.equalsIgnoreCase("DOCTOR")) {
                        badge.getStyleClass().add("badge-doctor");
                    } else {
                        badge.getStyleClass().add("badge-patient");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        TableColumn<User, String> colPhone = new TableColumn<>("Téléphone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(120);
        
        TableColumn<User, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(360);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button promoteBtn = new Button("⭐");
            private final Button viewStatsBtn = new Button("📊");
            private final Button qrCodeBtn = new Button("📱 QR");
            private final HBox box = new HBox(5, editBtn, deleteBtn, promoteBtn, viewStatsBtn, qrCodeBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                promoteBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                viewStatsBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                qrCodeBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-cursor: hand;");
                
                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    openEditUserDialog(user);
                });
                
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
                
                promoteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    promoteUser(user);
                });
                
                viewStatsBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showUserStats(user);
                });
                
                qrCodeBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    showQRCodeDialog(user);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        
        userTable.getColumns().addAll(colId, colFirstName, colLastName, colEmail, colRole, colPhone, colActions);
        userTable.setItems(usersList);
        
        System.out.println("✅ Tableau configuré");
    }
    
    // ==================== QR CODE ====================
    
    private void showQRCodeDialog(User user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("QR Code - " + user.getFullName());
        
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));
        vbox.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        String qrPayload = QRCodeGenerator.buildUserPayload(user);
        Image qrImage = QRCodeGenerator.generateQRCode(qrPayload);
        
        if (qrImage != null) {
            ImageView imageView = new ImageView(qrImage);
            imageView.setFitWidth(320);
            imageView.setFitHeight(320);
            
            Label titleLabel = new Label("📱 QR Code - " + user.getFullName());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e66;");
            
            Label infoLabel = new Label("ID: " + user.getId() + " | " + user.getEmail());
            infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
            
            Label scanLabel = new Label("Scanner ce code pour identifier l'utilisateur");
            scanLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");

            TextArea payloadPreview = new TextArea(qrPayload);
            payloadPreview.setEditable(false);
            payloadPreview.setWrapText(true);
            payloadPreview.setPrefRowCount(7);
            payloadPreview.setMaxWidth(360);
            payloadPreview.getStyleClass().add("qr-payload-preview");
            
            Button closeBtn = new Button("Fermer");
            closeBtn.setStyle("-fx-background-color: #2c3e66; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> dialog.close());
            
            Button saveBtn = new Button("💾 Sauvegarder");
            saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand");
            saveBtn.setOnAction(e -> saveQRCodeImage(qrImage, user));
            
            HBox buttonBox = new HBox(10, saveBtn, closeBtn);
            buttonBox.setAlignment(Pos.CENTER);
            
            vbox.getChildren().addAll(titleLabel, imageView, infoLabel, scanLabel, payloadPreview, buttonBox);
        } else {
            Label errorLabel = new Label("❌ Erreur lors de la génération du QR Code");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            vbox.getChildren().add(errorLabel);
        }
        
        Scene scene = new Scene(vbox, 440, 720);
        ThemeManager.apply(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void saveQRCodeImage(Image qrImage, User user) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le QR Code");
        fileChooser.setInitialFileName("QRCode_" + user.getId() + "_" + user.getLastName() + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(qrImage, null);
                ImageIO.write(bufferedImage, "png", file);
                showToast("✅ QR Code sauvegardé !");
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de sauvegarder l'image", Alert.AlertType.ERROR);
            }
        }
    }
    
    // ==================== USER STATS ====================
    
    private void showUserStats(User user) {
        double bmi = userManagementService.calculateBMI(user);
        String bmiCategory = getBMICategory(bmi);
        
        String message = "═══════════════════════════════════\n" +
                         "     📊 STATISTIQUES UTILISATEUR     \n" +
                         "═══════════════════════════════════\n" +
                         " 👤 " + user.getFullName() + "\n" +
                         " 📧 " + (user.getEmail() != null ? user.getEmail() : "N/A") + "\n" +
                         " 🩺 Rôle: " + user.getRole() + "\n" +
                         " ✅ Vérifié: " + (user.isVerified() ? "Oui" : "Non") + "\n" +
                         "───────────────────────────────────\n";
        
        if ("PATIENT".equals(user.getRole())) {
            message += " 📊 DONNÉES MÉDICALES\n" +
                       "    • Poids: " + (user.getPoids() != null ? user.getPoids() + " kg" : "N/A") + "\n" +
                       "    • Taille: " + (user.getTaille() != null ? user.getTaille() + " m" : "N/A") + "\n" +
                       "    • IMC: " + (bmi > 0 ? String.format("%.1f", bmi) + " (" + bmiCategory + ")" : "N/A") + "\n" +
                       "    • Glycémie: " + (user.getGlycemie() != null ? user.getGlycemie() + " g/L" : "N/A") + "\n" +
                       "    • Tension: " + (user.getTension() != null ? user.getTension() : "N/A") + "\n";
        } else if ("DOCTOR".equals(user.getRole())) {
            message += " 📊 INFORMATIONS PROFESSIONNELLES\n" +
                       "    • Spécialité: " + (user.getSpecialite() != null ? user.getSpecialite() : "N/A") + "\n" +
                       "    • Diplôme: " + (user.getDiplome() != null ? user.getDiplome() : "N/A") + "\n";
        }
        
        message += "═══════════════════════════════════";
        
        showAlert("Statistiques de " + user.getFirstName(), message, Alert.AlertType.INFORMATION);
    }
    
    private String getBMICategory(double bmi) {
        if (bmi == 0) return "Non calculé";
        if (bmi < 18.5) return "Insuffisance pondérale";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Surpoids";
        return "Obésité";
    }

    private void promoteUser(User user) {
        if ("ADMIN".equals(user.getRole())) {
            showToast("⚠️ Impossible de promouvoir un administrateur");
            return;
        }
        
        String newRole = "DOCTOR".equals(user.getRole()) ? "ADMIN" : "DOCTOR";
        String message = "DOCTOR".equals(user.getRole()) ? "promu ADMIN" : "promu DOCTEUR";
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Voulez-vous promouvoir " + user.getFullName() + " en " + newRole + " ?",
            ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Promotion utilisateur");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDAO.updateUserRole(user.getId(), newRole);
                    refreshAll();
                    showToast("✅ " + user.getFullName() + " a été " + message);
                } catch (SQLException e) {
                    showAlert("Erreur", "Promotion impossible", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void setupSortingListeners() {
        if (sortFieldCombo != null) {
            sortFieldCombo.setItems(FXCollections.observableArrayList("id", "Prénom", "Nom", "email", "rôle"));
            sortFieldCombo.setValue("id");
            sortFieldCombo.valueProperty().addListener((obs, old, val) -> {
                updateSortField(val);
                searchUsers();
            });
        }
        
        if (sortOrderCombo != null) {
            sortOrderCombo.setItems(FXCollections.observableArrayList("desc", "asc"));
            sortOrderCombo.setValue("desc");
            sortOrderCombo.valueProperty().addListener((obs, old, val) -> {
                currentSortOrder = val;
                searchUsers();
            });
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> searchUsers());
        }
        if (appointmentSearchField != null) {
            appointmentSearchField.textProperty().addListener((obs, old, val) -> searchAppointments());
        }
        if (userPagination != null) {
            userPagination.currentPageIndexProperty().addListener((obs, old, val) -> updateUserPage());
        }
        if (appointmentPagination != null) {
            appointmentPagination.currentPageIndexProperty().addListener((obs, old, val) -> updateAppointmentPage());
        }
    }

    private void updateSortField(String field) {
        switch (field) {
            case "Prénom": currentSortField = "firstName"; break;
            case "Nom": currentSortField = "lastName"; break;
            case "email": currentSortField = "email"; break;
            case "rôle": currentSortField = "role"; break;
            default: currentSortField = "id"; break;
        }
    }

    // ==================== USERS TAB ====================
    
    @FXML
    private void searchUsers() {
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        
        List<User> filtered = usersList.stream()
                .filter(u -> {
                    if (keyword.isEmpty()) return true;
                    return (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(keyword)) ||
                           (u.getLastName() != null && u.getLastName().toLowerCase().contains(keyword)) ||
                           (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword));
                })
                .collect(Collectors.toList());
        
        filtered = applySorting(filtered);
        
        currentUserPageSource = filtered;
        updateUserPagination();
    }

    private void updateUserPagination() {
        if (userPagination == null) {
            if (userTable != null) userTable.setItems(FXCollections.observableArrayList(currentUserPageSource));
            return;
        }
        int pageCount = Math.max(1, (int) Math.ceil(currentUserPageSource.size() / (double) ADMIN_ROWS_PER_PAGE));
        userPagination.setPageCount(pageCount);
        if (userPagination.getCurrentPageIndex() >= pageCount) {
            userPagination.setCurrentPageIndex(0);
        }
        updateUserPage();
    }

    private void updateUserPage() {
        if (userTable == null) return;
        int page = userPagination == null ? 0 : userPagination.getCurrentPageIndex();
        int from = Math.min(page * ADMIN_ROWS_PER_PAGE, currentUserPageSource.size());
        int to = Math.min(from + ADMIN_ROWS_PER_PAGE, currentUserPageSource.size());
        userTable.setItems(FXCollections.observableArrayList(currentUserPageSource.subList(from, to)));
    }

    private List<User> applySorting(List<User> users) {
        boolean ascending = "asc".equalsIgnoreCase(currentSortOrder);
        
        switch (currentSortField) {
            case "id":
                if (ascending) users.sort(Comparator.comparingInt(User::getId));
                else users.sort(Comparator.comparingInt(User::getId).reversed());
                break;
            case "firstName":
                if (ascending) users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)));
                else users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                break;
            case "lastName":
                if (ascending) users.sort(Comparator.comparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase)));
                else users.sort(Comparator.comparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                break;
            case "email":
                if (ascending) users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareToIgnoreCase)));
                else users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                break;
            case "role":
                users.sort(Comparator.comparing(User::getRole, Comparator.nullsLast(String::compareToIgnoreCase)));
                if (!ascending) users = users.reversed();
                break;
            default:
                users.sort(Comparator.comparingInt(User::getId).reversed());
        }
        return users;
    }

    @FXML
    private void loadAllUsers() {
        try {
            usersList.setAll(userDAO.getAllUsers());
            searchUsers();
            
            updateCharts();
            updateDashboardCards();
            
            System.out.println("✅ " + usersList.size() + " utilisateurs chargés");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les utilisateurs", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void exportUsersToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les utilisateurs");
        fileChooser.setInitialFileName("utilisateurs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        
        File file = fileChooser.showSaveDialog(userTable.getScene().getWindow());
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("ID,Prénom,Nom,Email,Rôle,Téléphone");
                for (User u : usersList) {
                    writer.printf("%d,%s,%s,%s,%s,%s%n",
                        u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getRole(), 
                        u.getPhone() != null ? u.getPhone() : "");
                }
                showToast("✅ Export CSV réussi ! " + usersList.size() + " utilisateurs exportés.");
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de l'export CSV", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
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
        saveBtn.getStyleClass().add("btn-primary");
        cancelBtn.getStyleClass().add("btn-delete");
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
                showToast("✅ Utilisateur ajouté avec succès!");
            } else {
                errorLbl.setText("Cet email existe déjà.");
            }
        });

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
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
        saveBtn.getStyleClass().add("btn-primary");
        cancelBtn.getStyleClass().add("btn-delete");
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
                showToast("✅ Utilisateur modifié avec succès!");
            } else {
                errorLbl.setText("Erreur lors de la mise à jour.");
            }
        });

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
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
                    showToast("✅ Utilisateur supprimé avec succès!");
                } else {
                    showAlert("Erreur", "Suppression impossible.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ==================== APPOINTMENTS ====================
    
    private void loadAllAppointments() {
        try {
            List<Appointment> apps = appointmentDAO.getAllAppointments();
            appointmentsList.setAll(apps);
            searchAppointments();
            System.out.println("✅ " + appointmentsList.size() + " rendez-vous chargés");
        } catch (Exception e) {
            System.err.println("Erreur chargement rendez-vous: " + e.getMessage());
        }
    }

    @FXML
    private void searchAppointments() {
        String kw = appointmentSearchField != null ? appointmentSearchField.getText().trim().toLowerCase() : "";
        if (kw.isEmpty()) {
            currentAppointmentPageSource = appointmentsList;
        } else {
            currentAppointmentPageSource = appointmentsList.stream()
                    .filter(a -> (a.getPatientName() != null && a.getPatientName().toLowerCase().contains(kw))
                            || (a.getDoctorName() != null && a.getDoctorName().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        updateAppointmentPagination();
    }

    private void updateAppointmentPagination() {
        if (appointmentPagination == null) {
            if (appointmentTable != null) appointmentTable.setItems(FXCollections.observableArrayList(currentAppointmentPageSource));
            return;
        }
        int pageCount = Math.max(1, (int) Math.ceil(currentAppointmentPageSource.size() / (double) ADMIN_ROWS_PER_PAGE));
        appointmentPagination.setPageCount(pageCount);
        if (appointmentPagination.getCurrentPageIndex() >= pageCount) {
            appointmentPagination.setCurrentPageIndex(0);
        }
        updateAppointmentPage();
    }

    private void updateAppointmentPage() {
        if (appointmentTable == null) return;
        int page = appointmentPagination == null ? 0 : appointmentPagination.getCurrentPageIndex();
        int from = Math.min(page * ADMIN_ROWS_PER_PAGE, currentAppointmentPageSource.size());
        int to = Math.min(from + ADMIN_ROWS_PER_PAGE, currentAppointmentPageSource.size());
        appointmentTable.setItems(FXCollections.observableArrayList(currentAppointmentPageSource.subList(from, to)));
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
                    showToast("✅ Rendez-vous annulé!");
                } else {
                    showAlert("Erreur", "Annulation impossible.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ==================== DASHBOARD STATS ====================
    
    private void loadDashboardStats() {
        try {
            List<User> allUsers = userDAO.getAllUsers();
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(allUsers.size()));
            if (totalPatientsLabel != null) {
                long patients = allUsers.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
                totalPatientsLabel.setText(String.valueOf(patients));
            }
            if (totalDoctorsLabel != null) {
                long doctors = allUsers.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();
                totalDoctorsLabel.setText(String.valueOf(doctors));
            }
            if (totalAppointmentsLabel != null) {
                List<Appointment> allAppointments = appointmentDAO.getAllAppointments();
                totalAppointmentsLabel.setText(String.valueOf(allAppointments.size()));
            }
            if (avgBmiLabel != null) {
                double avgBmi = allUsers.stream()
                        .filter(u -> "PATIENT".equals(u.getRole()))
                        .filter(u -> u.getPoids() != null && u.getTaille() != null && u.getTaille() > 0)
                        .mapToDouble(u -> u.getPoids() / (u.getTaille() * u.getTaille()))
                        .average()
                        .orElse(0.0);
                avgBmiLabel.setText(String.format("%.1f", avgBmi));
            }
            if (avgPoidsLabel != null) {
                double avgPoids = allUsers.stream()
                        .filter(u -> "PATIENT".equals(u.getRole()))
                        .filter(u -> u.getPoids() != null && u.getPoids() > 0)
                        .mapToDouble(User::getPoids)
                        .average()
                        .orElse(0.0);
                avgPoidsLabel.setText(String.format("%.1f kg", avgPoids));
            }
        } catch (SQLException e) {
            System.err.println("Database error during stats load: " + e.getMessage());
        }
    }

    private void addHoverAnimation(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // ==================== TOAST ====================
    
    private void showToast(String message) {
        NotificationManager.showToast(logoutBtn != null ? logoutBtn.getScene() : null,
                "VitaHealthFX", message, NotificationManager.Type.SUCCESS);
    }

    // ==================== STATS AVANCÉES ====================

    @FXML
    private void showAdvancedStats() {
        try {
            UserManagementService.AdvancedStats stats = userManagementService.getAdvancedStats();
            
            String message = "═══════════════════════════════════\n" +
                             "     📊 STATISTIQUES AVANCÉES      \n" +
                             "═══════════════════════════════════\n" +
                             " 📈 IMC MOYEN: " + String.format("%.1f", stats.getAvgBMI()) + " kg/m²\n\n" +
                             " 🏋️ CATÉGORIES IMC:\n" +
                             "    • Insuffisance pondérale: " + stats.getUnderweight() + "\n" +
                             "    • Normal: " + stats.getNormal() + "\n" +
                             "    • Surpoids: " + stats.getOverweight() + "\n" +
                             "    • Obésité: " + stats.getObese() + "\n\n" +
                             " 🩸 GLYCÉMIE:\n" +
                             "    • Élevée (>1.26): " + stats.getHighGlycemia() + "\n" +
                             "    • Basse (<0.70): " + stats.getLowGlycemia() + "\n" +
                             "═══════════════════════════════════";
            
            showAlert("Statistiques Avancées", message, Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les statistiques", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void showMedicalAlerts() {
        try {
            List<UserManagementService.MedicalAlert> alerts = userManagementService.getMedicalAlerts();
            
            if (alerts.isEmpty()) {
                showAlert("Alertes Médicales", "✅ Aucune alerte médicale à signaler", Alert.AlertType.INFORMATION);
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════\n");
            sb.append("     🚨 ALERTES MÉDICALES          \n");
            sb.append("═══════════════════════════════════\n\n");
            
            for (UserManagementService.MedicalAlert alert : alerts) {
                sb.append("👤 ").append(alert.getUser().getFullName()).append("\n");
                for (String reason : alert.getReasons()) {
                    sb.append("   • ").append(reason).append("\n");
                }
                sb.append("\n");
            }
            sb.append("═══════════════════════════════════");
            
            showAlert("Alertes Médicales", sb.toString(), Alert.AlertType.WARNING);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les alertes", Alert.AlertType.ERROR);
        }
    }

    // ==================== UTILS ====================
    
    @FXML
    private void refreshAll() {
        if (searchField != null) searchField.clear();
        if (appointmentSearchField != null) appointmentSearchField.clear();
        loadDashboardStats();
        loadAllUsers();
        loadAllAppointments();
        updateDashboardCards();
        updateCharts();
        System.out.println("✅ Tout a été rafraîchi !");
    }

    @FXML
    public void logout() {
        try {
            SessionManager.getInstance().logout();
            if (logoutBtn != null) {
                LoginController loginController = new LoginController();
                Scene scene = loginController.getScene();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("VitaHealthFX - Connexion");
                stage.centerOnScreen();
                stage.show();
                System.out.println("âœ… DÃ©connexion rÃ©ussie");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("VitaHealthFX - Connexion");
            stage.show();
            System.out.println("✅ Déconnexion réussie");
        } catch (IOException e) {
            System.err.println("Logout failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleTheme() {
        Scene scene = logoutBtn != null ? logoutBtn.getScene() : null;
        ThemeManager.toggle(scene);
        if (themeToggleBtn != null) themeToggleBtn.setText(ThemeManager.toggleText());
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        NotificationManager.showAlert(title, message, type);
    }
}
