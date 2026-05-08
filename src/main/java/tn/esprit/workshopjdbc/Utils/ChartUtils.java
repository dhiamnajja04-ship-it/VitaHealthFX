package tn.esprit.workshopjdbc.Utils;

import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.workshopjdbc.Entities.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartUtils {
    
    /**
     * Crée un graphique en camembert pour la répartition des rôles
     */
    public static PieChart createRolePieChart(List<User> users) {
        long patients = users.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
        long doctors = users.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();
        long admins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        
        PieChart pieChart = new PieChart();
        pieChart.setTitle("👥 Répartition des utilisateurs");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(350, 280);
        
        PieChart.Data patientData = new PieChart.Data("Patients (" + patients + ")", patients);
        PieChart.Data doctorData = new PieChart.Data("Médecins (" + doctors + ")", doctors);
        PieChart.Data adminData = new PieChart.Data("Admins (" + admins + ")", admins);
        
        pieChart.getData().addAll(patientData, doctorData, adminData);
        
        // Appliquer les couleurs après l'affichage
        patientData.getNode().setStyle("-fx-pie-color: #27ae60;");
        doctorData.getNode().setStyle("-fx-pie-color: #3498db;");
        adminData.getNode().setStyle("-fx-pie-color: #9b59b6;");
        
        return pieChart;
    }
    
    /**
     * Crée un graphique en barres pour les catégories IMC
     */
    public static BarChart<String, Number> createBMIBarChart(List<User> patients) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Catégorie IMC");
        yAxis.setLabel("Nombre de patients");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("📊 Distribution IMC des patients");
        barChart.setPrefSize(400, 280);
        barChart.setLegendVisible(false);
        
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
        series.getData().add(new XYChart.Data<>("Insuffisance (<18.5)", underweight));
        series.getData().add(new XYChart.Data<>("Normal (18.5-25)", normal));
        series.getData().add(new XYChart.Data<>("Surpoids (25-30)", overweight));
        series.getData().add(new XYChart.Data<>("Obésité (>30)", obese));
        
        barChart.getData().add(series);
        
        return barChart;
    }
    
    /**
     * Crée une carte de statistique stylisée
     */
    public static VBox createStatCard(String icon, String title, String value, String color) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(180);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 30px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-opacity: 0.8;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }
}