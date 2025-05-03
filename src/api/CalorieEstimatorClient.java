package api;

import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CalorieEstimatorClient {
    private static final String API_URL = "http://localhost:8080/estimateCalories";
    private static final Gson gson = new Gson();

    public static Double estimateCalories(String exercise, Integer reps, Integer timeSpent) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> payload = new HashMap<>();
            payload.put("exercise", exercise);
            payload.put("reps", reps == null ? 0 : reps); // fallback to 0 in case reps are missing 
            if (timeSpent != null) {
                payload.put("timeSpent", timeSpent);
            }

            String jsonInput = gson.toJson(payload);
            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                Map<String, Double> map = gson.fromJson(response, Map.class);
                return map.get("calories");
            } else {
                System.out.println("API call failed!!!! Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}