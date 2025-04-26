package gui;

import api.CalorieEstimatorClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DashboardWindow extends JFrame {
    private String username;
    private JTable table;
    private WorkoutTableModel tableModel;
    private JComboBox<String> dateDropdown;
    private HashMap<String, String> prettyToDbDate = new HashMap<>();

    public DashboardWindow(String username) {
        this.username = username;

        setTitle("Dashboard - " + username);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        addTopPanel();
        addCenterTable();
        addBottomButtons();

        loadAvailableDates();
        refreshWorkoutView();
    }

    private JPanel topPanel;
    private JScrollPane scrollPane;
    private JPanel bottomPanel;

    private void addTopPanel() {
        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JLabel welcome = new JLabel("Welcome, " + username + "!");
        welcome.setFont(new Font("Arial", Font.BOLD, 18));

        JButton logButton = new JButton("Log Today's Workout");
        logButton.addActionListener(e -> new WorkoutLogWindow(username, this).setVisible(true));

        dateDropdown = new JComboBox<>();
        dateDropdown.addActionListener(e -> refreshWorkoutView());

        topPanel.add(welcome);
        topPanel.add(logButton);
        topPanel.add(new JLabel("Previous Workouts:"));
        topPanel.add(dateDropdown);

        add(topPanel, BorderLayout.NORTH);
    }

    private void addCenterTable() {
        tableModel = new WorkoutTableModel(new ArrayList<>());
        table = new JTable(tableModel);

        table.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                Object[] rowData = tableModel.getRowData(row);
                saveEditedRowToDatabase(rowData);
            }
        });

        table.getColumn("Calories").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                setText((value == null) ? "N/A" : value.toString());
            }
        });

        table.setRowHeight(30);

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addBottomButtons() {
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    
        JButton deleteButton = new JButton("Delete Selected Row");
        deleteButton.addActionListener(e -> deleteSelectedRow());
    
        JButton estimateButton = new JButton("Get Calorie Estimate");
        estimateButton.addActionListener(e -> estimateCaloriesForSelectedRow());
    
        JButton editButton = new JButton("Edit Selected Row"); // ✨ new button
        editButton.addActionListener(e -> editSelectedRow());

        JButton trendsButton = new JButton("View Workout Trends 📈");
        trendsButton.addActionListener(e -> showTrendsChart());

        bottomPanel.add(trendsButton);
    
        bottomPanel.add(deleteButton);
        bottomPanel.add(estimateButton);
        bottomPanel.add(editButton);
    
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showTrendsChart() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            String query = "SELECT date, SUM(calories_burnt) AS total_calories, SUM(time_spent) AS total_time " +
                           "FROM workouts WHERE username = ? GROUP BY date ORDER BY date ASC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
    
            // Create datasets
            var caloriesDataset = new org.jfree.data.category.DefaultCategoryDataset();
            var timeDataset = new org.jfree.data.category.DefaultCategoryDataset();
    
            while (rs.next()) {
                String date = rs.getString("date");
                int calories = rs.getInt("total_calories");
                int time = rs.getInt("total_time");
    
                caloriesDataset.addValue(calories, "Calories Burnt", date);
                timeDataset.addValue(time, "Time Spent (min)", date);
            }
    
            // Create two charts
            var caloriesChart = org.jfree.chart.ChartFactory.createLineChart(
                    "Calories Burnt Per Day",
                    "Date",
                    "Calories",
                    caloriesDataset
            );
    
            var timeChart = org.jfree.chart.ChartFactory.createBarChart(
                    "Time Spent Working Out Per Day",
                    "Date",
                    "Time (min)",
                    timeDataset
            );
    
            // Panels
            JPanel chartPanel = new JPanel(new GridLayout(2, 1));
            chartPanel.add(new org.jfree.chart.ChartPanel(caloriesChart));
            chartPanel.add(new org.jfree.chart.ChartPanel(timeChart));
    
            // Show in new window
            JFrame trendsFrame = new JFrame("Workout Trends for " + username);
            trendsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            trendsFrame.setSize(900, 700);
            trendsFrame.setLocationRelativeTo(null);
            trendsFrame.add(chartPanel);
            trendsFrame.setVisible(true);
    
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error generating charts: " + e.getMessage());
        }
    }

    private void editSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            return;
        }
    
        Object[] rowData = tableModel.getRowData(selectedRow);
        int id = (int) rowData[0];
        String workoutName = (String) rowData[1];
        String reps = rowData[2] == null ? "" : rowData[2].toString();
        String time = rowData[3] == null ? "" : rowData[3].toString();
        String calories = rowData[4] == null ? "" : rowData[4].toString();
    
        JTextField repsField = new JTextField(reps);
        JTextField timeField = new JTextField(time);
        JTextField caloriesField = new JTextField(calories);
    
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Reps:"));
        panel.add(repsField);
        panel.add(new JLabel("Time (min):"));
        panel.add(timeField);
        panel.add(new JLabel("Calories:"));
        panel.add(caloriesField);
    
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Workout - " + workoutName,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                String update = "UPDATE workouts SET reps = ?, time_spent = ?, calories_burnt = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(update);
    
                if (repsField.getText().trim().isEmpty()) {
                    stmt.setNull(1, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(1, Integer.parseInt(repsField.getText().trim()));
                }
    
                if (timeField.getText().trim().isEmpty()) {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(2, Integer.parseInt(timeField.getText().trim()));
                }
    
                if (caloriesField.getText().trim().isEmpty()) {
                    stmt.setNull(3, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(3, Integer.parseInt(caloriesField.getText().trim()));
                }
    
                stmt.setInt(4, id);
                stmt.executeUpdate();
    
                JOptionPane.showMessageDialog(this, "✅ Workout updated!");
                refreshWorkoutView();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Failed to save changes: " + ex.getMessage());
            }
        }
    }

    private void loadAvailableDates() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            String query = "SELECT DISTINCT date FROM workouts WHERE username = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            dateDropdown.removeAllItems();
            prettyToDbDate.clear();

            while (rs.next()) {
                String dbDate = rs.getString("date");
                String prettyDate = formatPrettyDate(LocalDate.parse(dbDate));
                dateDropdown.addItem(prettyDate);
                prettyToDbDate.put(prettyDate, dbDate);
            }

            String todayPretty = formatPrettyDate(LocalDate.now());
            dateDropdown.setSelectedItem(todayPretty);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshWorkoutView() {
        new SwingWorker<ArrayList<Object[]>, Void>() {
            @Override
            protected ArrayList<Object[]> doInBackground() throws Exception {
                ArrayList<Object[]> workouts = new ArrayList<>();
                String selectedPretty = (String) dateDropdown.getSelectedItem();
                String selectedDbDate = prettyToDbDate.get(selectedPretty);

                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                    String query = "SELECT id, workout_name, reps, time_spent, calories_burnt FROM workouts WHERE username = ? AND date = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    stmt.setString(2, selectedDbDate);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        workouts.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("workout_name"),
                            rs.getObject("reps"),
                            rs.getObject("time_spent"),
                            rs.getObject("calories_burnt")
                        });
                    }
                }
                return workouts;
            }

            @Override
            protected void done() {
                try {
                    ArrayList<Object[]> workouts = get();
                    tableModel.updateData(workouts);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardWindow.this, "❌ Error loading workouts: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void deleteSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }
        int id = (int) tableModel.getRowData(selectedRow)[0];
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            String delete = "DELETE FROM workouts WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(delete);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Row deleted!");
            refreshWorkoutView();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveEditedRowToDatabase(Object[] rowData) {
        int id = (int) rowData[0];
        Object repsObj = rowData[2];
        Object timeObj = rowData[3];
        Object caloriesObj = rowData[4];
    
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            String update = "UPDATE workouts SET reps = ?, time_spent = ?, calories_burnt = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(update);
    
            if (repsObj == null || repsObj.toString().equals("N/A")) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, Integer.parseInt(repsObj.toString()));
            }
    
            if (timeObj == null || timeObj.toString().equals("N/A")) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, Integer.parseInt(timeObj.toString()));
            }
    
            if (caloriesObj == null || caloriesObj.toString().equals("N/A")) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, Integer.parseInt(caloriesObj.toString()));
            }
    
            stmt.setInt(4, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to save changes: " + e.getMessage());
        }
    }

    private void estimateCaloriesForSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
    
        Object[] rowData = tableModel.getRowData(selectedRow);
        int id = (int) rowData[0];
        String workoutName = (String) rowData[1];
        Object repsObj = rowData[2];
        Object timeObj = rowData[3];
        Object caloriesObj = rowData[4];
    
        // Ask if calories already exist
        if (caloriesObj != null && !caloriesObj.toString().equalsIgnoreCase("N/A")) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Calories already exist. Do you want to override them?",
                    "Confirm Override",
                    JOptionPane.YES_NO_OPTION);
    
            if (choice != JOptionPane.YES_OPTION) {
                return; // user cancelled
            }
        }
    
        Integer reps = (repsObj == null || repsObj.toString().equals("N/A")) ? null : Integer.parseInt(repsObj.toString());
        Integer timeSpent = (timeObj == null || timeObj.toString().equals("N/A")) ? null : Integer.parseInt(timeObj.toString());
    
        Double calories = CalorieEstimatorClient.estimateCalories(workoutName.trim(), reps, timeSpent);
    
        if (calories != null) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                String update = "UPDATE workouts SET calories_burnt = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(update);
                stmt.setInt(1, (int) Math.round(calories));
                stmt.setInt(2, id);
                stmt.executeUpdate();
                refreshWorkoutView();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "❌ We don't have the calories of this exercise now! Check again soon!");
        }
    }

    private String formatPrettyDate(LocalDate date) {
        int day = date.getDayOfMonth();
        String suffix = (day >= 11 && day <= 13) ? "th" : switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };

        String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = date.getYear();

        return day + suffix + " " + month + " " + year;
    }
}