package gui;

import api.CalorieEstimatorClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import model.WorkoutTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DashboardWindow extends JFrame {
    private String username;
    private JTable table;
    private JButton logButton;
    private WorkoutTableModel tableModel;
    private JComboBox<String> dateDropdown;
    private HashMap<String, String> prettyToDbDate = new HashMap<>();
    private String lastOldReps = null;
    private String lastOldTime = null;
    private String lastOldCal = null;
    private boolean isRefreshing = false;
    private boolean isProgrammaticUpdate = false;

    public DashboardWindow(String username) {
        this.username = username;
        setTitle(username + "'s dashboard");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        addTopPanel();
        addCenterTable();
        addBottomButtons();
        loadAvailableDates();
        // SwingUtilities.invokeLater(this::refreshWorkoutView);
    }

    private void updateLogButtonLabel() {
        String selected = (String) dateDropdown.getSelectedItem();
        if (selected != null) {
            logButton.setText("Log " + selected + "'s Workout");
        } else {
            logButton.setText("Log Workout");
        }
    }

    private void addTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JLabel welcome = new JLabel("Welcome, " + username + "!");
        welcome.setFont(new Font("Arial", Font.BOLD, 18));

        logButton = new JButton();
        dateDropdown = new JComboBox<>();

        logButton.addActionListener(e -> {
            String selected = (String) dateDropdown.getSelectedItem();
            String dbDate = prettyToDbDate.getOrDefault(selected, LocalDate.now().toString());
            new WorkoutLogWindow(username, this, dbDate).setVisible(true);
        });

        dateDropdown.addActionListener(e -> {
            refreshWorkoutView(LocalDate.now());
            updateLogButtonLabel();
        });

        updateLogButtonLabel();

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
                if (isProgrammaticUpdate) return;
                int row = e.getFirstRow();
                if (row == -1) return;
                Object[] rowData = tableModel.getRowData(row);
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                    saveEditedRowToDatabase(conn, rowData, lastOldReps, lastOldTime, lastOldCal);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        table.getColumn("Calories").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                setText((value == null) ? "N/A" : value.toString());
            }
        });

        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addBottomButtons() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton deleteButton = new JButton("Delete Selected Row");
        deleteButton.addActionListener(e -> deleteSelectedRow());

        JButton estimateButton = new JButton("Get Calorie Estimate");
        estimateButton.addActionListener(e -> estimateCaloriesForSelectedRow());

        JButton editButton = new JButton("Edit Selected Row");
        editButton.addActionListener(e -> editSelectedRow());

        JButton trendsButton = new JButton("View Workout Trends");
        trendsButton.addActionListener(e -> new ChartPopup(username, this).setVisible(true));

        JButton exportButton = new JButton("Export CSV");
        exportButton.addActionListener(e -> exportAllWorkoutsToCSV());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose(); 
            new LoginWindow().setVisible(true); 
        });
        

        bottomPanel.add(exportButton);
        bottomPanel.add(trendsButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(estimateButton);
        bottomPanel.add(editButton);
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshWorkoutView(LocalDate fallbackDate) {
        if (isRefreshing) return;
        isRefreshing = true;
    
        String selectedPretty = (String) dateDropdown.getSelectedItem();
        String selectedDbDate = null;
    
        if (selectedPretty != null && prettyToDbDate.containsKey(selectedPretty)) {
            selectedDbDate = prettyToDbDate.get(selectedPretty);
        } else {
            selectedDbDate = fallbackDate.toString();
        }
    
        final String dbDateToUse = selectedDbDate;
    
        new SwingWorker<ArrayList<Object[]>, Void>() {
            @Override
            protected ArrayList<Object[]> doInBackground() throws Exception {
                ArrayList<Object[]> workouts = new ArrayList<>();
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                    String query = "SELECT id, workout_name, reps, time_spent, calories_burnt FROM workouts WHERE username = ? AND date = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    stmt.setString(2, dbDateToUse);
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
                    e.printStackTrace();
                } finally {
                    isRefreshing = false;
                }
            }
        }.execute();
    }

    private void deleteSelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) tableModel.getRowData(row)[0];
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM workouts WHERE id = ?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
            refreshWorkoutView(LocalDate.now());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void estimateCaloriesForSelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        Object[] rowData = tableModel.getRowData(row);
        int id = (int) rowData[0];
        String workout = (String) rowData[1];
        Integer reps = rowData[2] == null ? null : Integer.parseInt(rowData[2].toString());
        Integer time = rowData[3] == null ? null : Integer.parseInt(rowData[3].toString());
        Double est = CalorieEstimatorClient.estimateCalories(workout, reps, time);
        if (est == null) {
            JOptionPane.showMessageDialog(this,
                "We don't have an estimate for calories for this workout yet! Check back soon!",
                "Estimate Not Available",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int calories = (int) Math.round(est);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE workouts SET calories_burnt = ? WHERE id = ?");
            stmt.setInt(1, calories);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            refreshWorkoutView(LocalDate.now());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void editSelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        Object[] rowData = tableModel.getRowData(row);
        String reps = rowData[2] == null ? "" : rowData[2].toString();
        String time = rowData[3] == null ? "" : rowData[3].toString();
        String cal = rowData[4] == null ? "" : rowData[4].toString();

        lastOldReps = reps;
        lastOldTime = time;
        lastOldCal = cal;

        JTextField repsField = new JTextField(reps);
        JTextField timeField = new JTextField(time);
        JTextField calField = new JTextField(cal);
        JPanel p = new JPanel(new GridLayout(3, 2));
        p.add(new JLabel("Reps:")); p.add(repsField);
        p.add(new JLabel("Time:")); p.add(timeField);
        p.add(new JLabel("Calories:")); p.add(calField);

        int result = JOptionPane.showConfirmDialog(this, p, "Edit", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            isProgrammaticUpdate = true;
            tableModel.setValueAt(repsField.getText(), row, 2);
            tableModel.setValueAt(timeField.getText(), row, 3);
            tableModel.setValueAt(calField.getText(), row, 4);
            isProgrammaticUpdate = false;
        
            Object[] data = tableModel.getRowData(row);
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                saveEditedRowToDatabase(conn, data, lastOldReps, lastOldTime, lastOldCal);
                refreshWorkoutView(LocalDate.now());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void saveEditedRowToDatabase(Connection conn, Object[] rowData, String oldReps, String oldTime, String oldCal) throws SQLException {
        int id = (int) rowData[0];
        String name = (String) rowData[1];
        String repsStr = rowData[2] == null ? null : rowData[2].toString();
        String timeStr = rowData[3] == null ? null : rowData[3].toString();
        String calStr = rowData[4] == null ? null : rowData[4].toString();

        Integer reps = (repsStr == null || repsStr.isEmpty()) ? null : Integer.parseInt(repsStr);
        Integer time = (timeStr == null || timeStr.isEmpty()) ? null : Integer.parseInt(timeStr);
        Integer cal = (calStr == null || calStr.isEmpty()) ? null : Integer.parseInt(calStr);

        boolean calUnchanged = Objects.equals(oldCal, calStr);
        boolean repsChanged = !Objects.equals(oldReps, repsStr);
        boolean timeChanged = !Objects.equals(oldTime, timeStr);

        if (calUnchanged && (repsChanged || timeChanged)) {
            Double est = CalorieEstimatorClient.estimateCalories(name, reps, time);
            if (est != null) cal = (int) Math.round(est);
            else {
                JOptionPane.showMessageDialog(this,
                    "We don't have an estimate for calories for this workout yet! Check back soon!",
                    "Estimate Not Available",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        PreparedStatement stmt = conn.prepareStatement("UPDATE workouts SET reps = ?, time_spent = ?, calories_burnt = ? WHERE id = ?");
        if (reps == null) stmt.setNull(1, Types.INTEGER); else stmt.setInt(1, reps);
        if (time == null) stmt.setNull(2, Types.INTEGER); else stmt.setInt(2, time);
        if (cal == null) stmt.setNull(3, Types.INTEGER); else stmt.setInt(3, cal);
        stmt.setInt(4, id);
        stmt.executeUpdate();
    }

    private void exportAllWorkoutsToCSV() {
        try {
            String downloadsPath = System.getProperty("user.home") + "/Downloads/workouts_export.csv";
            util.CSVExporter.exportWorkouts(username, downloadsPath);
            JOptionPane.showMessageDialog(this, "Exported to Downloads!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatPrettyDate(LocalDate date) {
        int d = date.getDayOfMonth();
        String suffix;

        if (d >= 11 && d <= 13) {
            suffix = "th";
        } else {
            switch (d % 10) {
                case 1: suffix = "st"; break;
                case 2: suffix = "nd"; break;
                case 3: suffix = "rd"; break;
                default: suffix = "th"; break;
            }
        }

        String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return d + suffix + " " + month + " " + date.getYear();
    }
    public void loadAvailableDates() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            dateDropdown.removeAllItems();
            prettyToDbDate.clear();
    
            LocalDate today = LocalDate.now();
            LocalDate oneYearAgo = today.minusYears(1);
            LocalDate startDate = oneYearAgo;
    
            PreparedStatement stmt = conn.prepareStatement("SELECT MIN(date) AS earliest FROM workouts WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("earliest") != null) {
                LocalDate dbDate = LocalDate.parse(rs.getString("earliest"));
                if (dbDate.isBefore(oneYearAgo)) startDate = dbDate;
            }
    
            for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
                String pretty = formatPrettyDate(d);
                dateDropdown.addItem(pretty);
                prettyToDbDate.put(pretty, d.toString());
            }
    
            String todayPretty = formatPrettyDate(today);
            dateDropdown.setSelectedItem(todayPretty);

            refreshWorkoutView(LocalDate.now());
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
