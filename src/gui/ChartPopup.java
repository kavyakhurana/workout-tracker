package gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class ChartPopup extends JFrame {
    public ChartPopup(String username) {
        setTitle("Weekly Progress Chart");
        setSize(600, 400);
        setLocationRelativeTo(null);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Collect last 7 days of data
        LinkedHashMap<String, Integer> dateToCalories = new LinkedHashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.toString();
                String label = date.getMonthValue() + "/" + date.getDayOfMonth();

                String query = "SELECT SUM(calories_burnt) as total FROM workouts WHERE username = ? AND date = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, dateStr);
                ResultSet rs = stmt.executeQuery();

                int calories = rs.next() ? rs.getInt("total") : 0;
                dataset.addValue(calories, "Calories Burnt", label);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
            return;
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Calories Burned in Last 7 Days",
                "Date",
                "Calories",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(560, 370));
        setContentPane(chartPanel);
    }
}