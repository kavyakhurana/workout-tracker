package util;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class CSVExporter {
    public static void exportWorkouts(String username, String filepath) throws IOException, SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:workout_tracker.db");
             FileWriter writer = new FileWriter(filepath)) {

            writer.write("Date,Workout,Reps,Time,Calories\n");

            String query = "SELECT date, workout_name, reps, time_spent, calories_burnt FROM workouts WHERE username = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                writer.write(String.join(",",
                        rs.getString("date"),
                        rs.getString("workout_name"),
                        rs.getString("reps") == null ? "" : rs.getString("reps"),
                        rs.getString("time_spent") == null ? "" : rs.getString("time_spent"),
                        rs.getString("calories_burnt") == null ? "" : rs.getString("calories_burnt")
                ) + "\n");
            }
        }
    }
}