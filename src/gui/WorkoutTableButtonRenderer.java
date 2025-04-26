package gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class WorkoutTableButtonRenderer extends JButton implements TableCellRenderer {
    public WorkoutTableButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (column == 3) { // Calories column
            if (value == null || value.toString().isEmpty()) {
                setText("Get Calorie Estimate");
            } else {
                setText(value.toString()); // Just text if manually filled
            }
        }
        return this;
    }
}