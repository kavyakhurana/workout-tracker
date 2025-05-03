package gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class ChartPopup extends JFrame {
    public ChartPopup(String username, JFrame dashboard) {
        setTitle("Weekly Progress Chart");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dbDate = date.toString();
                String label = date.getMonthValue() + "/" + date.getDayOfMonth();

                String query = "SELECT SUM(calories_burnt) as total FROM workouts WHERE username = ? AND date = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, username);
                    stmt.setString(2, dbDate);
                    ResultSet rs = stmt.executeQuery();
                    int calories = rs.next() ? rs.getInt("total") : 0;
                    dataset.addValue(calories, "Calories Burnt", label);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQLite Database Error: " + e.getMessage());
            dispose();
            dashboard.setVisible(true);
            return;
        }

        // Chart panel
        ChartPanel chartPanel;
        try {
            JFreeChart chart = ChartFactory.createBarChart(
                    "Calories Burned in Last 7 Days",
                    "Date",
                    "Calories",
                    dataset
            );
            chartPanel = new ChartPanel(chart);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chart Error: " + e.getMessage());
            dispose();
            dashboard.setVisible(true);
            return;
        }

        // Back button
        JButton backButton = new JButton(" <- Back to Dashboard");
        backButton.addActionListener(e -> {
            this.dispose();
            dashboard.setVisible(true);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);

        add(chartPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack(); 
        setSize(700, 500); 
        setLocationRelativeTo(null);
        setVisible(true);
    }
}