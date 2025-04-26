package gui;

import api.CalorieEstimatorClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WorkoutTableButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private DashboardWindow dashboard;
    private JTable table;
    private int row;
    private int col;

    public WorkoutTableButtonEditor(JCheckBox checkBox, DashboardWindow dashboard) {
        super(checkBox);
        this.dashboard = dashboard;

        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> {
            fireEditingStopped();
            handleButtonClick();
        });
    }

    private void handleButtonClick() {
        Object idObj = ((WorkoutTableModel) table.getModel()).getRowData(row)[0];
        int workoutId = (Integer) idObj;

        if (col == 3) { // Calories Button
            Object[] rowData = ((WorkoutTableModel) table.getModel()).getRowData(row);
            String workoutName = (String) rowData[1];
            Object repsObj = rowData[2];

            Object timeObj = rowData[3];

            if (repsObj == null && timeObj == null) {
                JOptionPane.showMessageDialog(null, "Cannot estimate calories: Reps not entered.");
                return;
            }

            Integer reps = (repsObj == null) ? null : Integer.parseInt(repsObj.toString());
            Integer timeSpent = (timeObj == null) ? null : Integer.parseInt(timeObj.toString());

            Double calories = CalorieEstimatorClient.estimateCalories(workoutName.trim(), reps, timeSpent);

            if (calories != null) {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
                    String update = "UPDATE workouts SET calories_burnt = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(update);
                    stmt.setInt(1, (int) Math.round(calories));
                    stmt.setInt(2, workoutId);
                    stmt.executeUpdate();
                    dashboard.refreshWorkoutView();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "❌ We don't have the calories of this exercise now! Check again soon!");
            }
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                  boolean isSelected, int row, int column) {
        this.table = table;
        this.row = row;
        this.col = column;

        table.getSelectionModel().setSelectionInterval(row, row);

        if (column == 3) {
            Object val = table.getModel().getValueAt(row, column);
            if (val == null || val.toString().isEmpty()) {
                button.setText("Get Calorie Estimate");
            } else {
                button.setText(val.toString());
            }
        }
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}