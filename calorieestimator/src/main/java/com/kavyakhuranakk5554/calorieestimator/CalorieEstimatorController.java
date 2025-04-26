package com.kavyakhuranakk5554.calorieestimator;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/estimateCalories")
public class CalorieEstimatorController {

    private static final Map<String, Double> exerciseCaloriesPerUnit = new HashMap<>();

    static {
        // Strength (lowercased keys)
        exerciseCaloriesPerUnit.put("bench press", 0.6);
        exerciseCaloriesPerUnit.put("chest press", 0.5);
        exerciseCaloriesPerUnit.put("pushups", 0.4);
        exerciseCaloriesPerUnit.put("deadlift", 0.8);
        exerciseCaloriesPerUnit.put("lat pulldown", 0.45);
        exerciseCaloriesPerUnit.put("pull-ups", 0.5);
        exerciseCaloriesPerUnit.put("squat", 0.7);
        exerciseCaloriesPerUnit.put("leg press", 0.6);
        exerciseCaloriesPerUnit.put("lunges", 0.5);
        exerciseCaloriesPerUnit.put("shoulder press", 0.5);
        exerciseCaloriesPerUnit.put("lateral raise", 0.3);
        exerciseCaloriesPerUnit.put("bicep curl", 0.4);
        exerciseCaloriesPerUnit.put("tricep extension", 0.4);
        exerciseCaloriesPerUnit.put("hammer curl", 0.4);
        exerciseCaloriesPerUnit.put("sit-ups", 0.25);
        exerciseCaloriesPerUnit.put("russian twists", 0.2);

        // Core
        exerciseCaloriesPerUnit.put("plank", 0.07); // per second

        // Cardio
        exerciseCaloriesPerUnit.put("running", 10.0);
        exerciseCaloriesPerUnit.put("cycling", 8.0);
        exerciseCaloriesPerUnit.put("rowing", 9.0);
        exerciseCaloriesPerUnit.put("stair climber", 7.0);
    }

    @PostMapping
    public Map<String, Double> estimateCalories(@RequestBody WorkoutRequest request) {
        // Preprocess exercise name smartly
        String exerciseKey = request.getExercise().trim().toLowerCase().replaceAll("\\s+", " ");

        double perUnit = exerciseCaloriesPerUnit.getOrDefault(exerciseKey, 0.4);

        double calories;

        if (request.getTimeSpent() != null && perUnit > 2.0) {
            calories = perUnit * request.getTimeSpent();
        } else if (exerciseKey.equals("plank")) {
            calories = perUnit * request.getReps();
        } else {
            calories = perUnit * request.getReps();
        }

        Map<String, Double> response = new HashMap<>();
        response.put("calories", calories);
        return response;
    }
}