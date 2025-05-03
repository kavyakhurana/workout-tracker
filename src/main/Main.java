package main;
import javax.swing.*;
import java.sql.*;
import gui.LoginWindow;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            String createTable = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL);";
            Statement stmt = conn.createStatement();
            stmt.execute(createTable);

            String createWorkouts = "CREATE TABLE IF NOT EXISTS workouts (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "username TEXT NOT NULL," +
            "date TEXT NOT NULL," +
            "workout_name TEXT NOT NULL," +
            "reps INTEGER," +
            "time_spent INTEGER," +
            "calories_burnt INTEGER);";

        stmt.execute(createWorkouts);
        } catch (SQLException e) {
            System.out.println("❌ DB Error: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new LoginWindow().setVisible(true);
        });

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db")) {
            String testInsert = "INSERT INTO workouts (username, date, workout_name) VALUES (?, ?, ?)";
            PreparedStatement testStmt = conn.prepareStatement(testInsert);
            testStmt.setString(1, "testuser");
            testStmt.setString(2, java.time.LocalDate.now().toString());
            testStmt.setString(3, "Test Squats");
            testStmt.executeUpdate();
            System.out.println("🧪 Test workout inserted");
        } catch (SQLException e) {
            System.out.println("❌ Test insert failed: " + e.getMessage());
        }
    }
}