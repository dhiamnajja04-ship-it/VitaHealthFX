package tn.esprit.workshopjdbc.Controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import tn.esprit.workshopjdbc.Services.ParticipationService;
import java.time.LocalDate;
import java.util.Map;

public class StatsController {

    @FXML private BarChart<String, Number> eventBarChart;
    @FXML private LineChart<String, Number> trendLineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private ParticipationService pService = new ParticipationService();

    @FXML
    public void initialize() {
        loadBarChart();
        loadLineChart();
    }

    private void loadBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Participants per Event");

        Map<String, Integer> data = pService.getStatsByEvent();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        eventBarChart.getData().add(series);
    }

    private void loadLineChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Last 7 Days Activity");

        Map<LocalDate, Integer> data = pService.getRegistrationTrend();
        for (Map.Entry<LocalDate, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }

        trendLineChart.getData().add(series);
    }
}