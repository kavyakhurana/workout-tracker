package model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class WorkoutTableModel extends AbstractTableModel {
    private String[] columns = {"ID", "Workout", "Reps", "Time (min)", "Calories"};
    private ArrayList<Object[]> data;

    public WorkoutTableModel(ArrayList<Object[]> data) {
        this.data = data;
    }

    public Object[] getRowData(int rowIndex) {
        return data.get(rowIndex);
    }

    public void updateData(ArrayList<Object[]> newData) {
        this.data = newData;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex >= data.get(rowIndex).length) {
            return "N/A";  
        }
        Object value = data.get(rowIndex)[columnIndex];
        return value == null ? "N/A" : value;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 2 || column == 3 || column == 4;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < data.size() && columnIndex < data.get(rowIndex).length) {
            if (aValue == null || aValue.toString().trim().isEmpty()) {
                data.get(rowIndex)[columnIndex] = null;
            } else {
                data.get(rowIndex)[columnIndex] = aValue;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public int getRowIndexById(int id) {
        for (int i = 0; i < data.size(); i++) {
            if ((int) data.get(i)[0] == id) return i;
        }
        return -1;
    }
}