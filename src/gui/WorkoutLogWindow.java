package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Types;
import java.time.LocalDate;

public class WorkoutLogWindow extends JFrame {
    private JTextField workoutField, repsField, timeField, caloriesField;
    private String username;
    private DashboardWindow dashboard;

    public WorkoutLogWindow(String username, DashboardWindow dashboard) {
        this.username = username;
        this.dashboard = dashboard;

        setTitle("Log Workout");
        setSize(350, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Workout Name:"));
        workoutField = new JTextField();
        panel.add(workoutField);

        panel.add(new JLabel("Reps:"));
        repsField = new JTextField();
        panel.add(repsField);

        panel.add(new JLabel("Time (min):"));
        timeField = new JTextField();
        panel.add(timeField);

        panel.add(new JLabel("Calories Burnt:"));
        caloriesField = new JTextField();
        panel.add(caloriesField);

        JButton saveButton = new JButton("Save Workout");
        panel.add(saveButton);

        add(panel);

        saveButton.addActionListener(e -> saveWorkout());
    }

    private void saveWorkout() {
        System.out.println("Save workout called");
        String workout = workoutField.getText().trim();
        String reps = repsField.getText().trim();
        String time = timeField.getText().trim();
        String calories = caloriesField.getText().trim();
        String date = LocalDate.now().toString();

        if (workout.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Workout name is required.");
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                    String insert = "INSERT INTO workouts (username, date, workout_name, reps, time_spent, calories_burnt) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(insert);
                    stmt.setString(1, username);
                    stmt.setString(2, date);
                    stmt.setString(3, workout);

                    if (reps.isEmpty()) {
                        stmt.setNull(4, Types.INTEGER);
                    } else {
                        stmt.setInt(4, Integer.parseInt(reps));
                    }

                    if (time.isEmpty()) {
                        stmt.setNull(5, Types.INTEGER);
                    } else {
                        stmt.setInt(5, Integer.parseInt(time));
                    }

                    if (calories.isEmpty()) {
                        stmt.setNull(6, Types.INTEGER);
                    } else {
                        stmt.setInt(6, Integer.parseInt(calories));
                    }

                    int rows = stmt.executeUpdate();
                    System.out.println("Inserted rows: " + rows);
                    return rows > 0;
                } catch (SQLException | NumberFormatException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(WorkoutLogWindow.this, "Workout saved!");
                        dashboard.refreshWorkoutView();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(WorkoutLogWindow.this, "Failed to save workout.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(WorkoutLogWindow.this, "Unexpected error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}