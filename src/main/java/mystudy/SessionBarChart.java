package mystudy;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A JavaFX Pane containing a BarChart of StudySession durations.
 */
public class SessionBarChart extends BorderPane {

    private final BarChart<String, Number> barChart;

    public SessionBarChart(List<StudySession> sessions) {
        // X axis: session start time
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Session Start");

        // Y axis: hours studied
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Hours");

        barChart = new BarChart<>(xAxis, yAxis);  
        barChart.setTitle("Study Session Durations");

        // Series for the data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Duration");

        // Format timestamps
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (StudySession s : sessions) {
            double hours = s.getSessionDuration() / (1000.0 * 60 * 60);
            String label = s.getStartTimeFormatted(); 
            // or: String label = fmt.format(s.getStartInstant().atZone(ZoneId.systemDefault()));
            series.getData().add(new XYChart.Data<>(label, hours));
        }

        barChart.getData().add(series);
        setCenter(barChart);
    }

    public BarChart<String, Number> getChart() {
        return barChart;
    }

    public void updateSessions(List<StudySession> sessions) {
        // Remove any existing series
        barChart.getData().clear();
    
        // Create a fresh series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Duration");
    
        // Timestamp formatter
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
        // Populate series from the sessions
        for (StudySession s : sessions) {
            double hours = s.getSessionDuration() / (1000.0 * 60 * 60);
            String label = s.getStartTimeFormatted(); 
            // or: fmt.format(s.getStartInstant().atZone(ZoneId.systemDefault()))
            series.getData().add(new XYChart.Data<>(label, hours));
        }
    
        // Add back to chart
        barChart.getData().add(series);
    }
}
