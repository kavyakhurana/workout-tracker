# Workout Tracker

This is a Java project for NYU Tandon's CS9053 - Intro to Java. In this repo, I have developed a Java Swing application to track, visualize, and manage your workouts with calorie estimates and data exports.

The idea for this project stemmed from my recent interest in working out and gaining muscle, so making this project has beene extremely fun.

---


## Features

### 🔑 User Authentication

* This project has a Login & Registration system
* Username is tied to all workout entries
* I used a local SQLite database for user authentication

### 🏋️ Workout Logging

* Log your workout for today with:

  * Workout name
  * Reps (optional)
  * Time spent (optional)
  * Calories (optional)
    
* You can log workouts for different days, even past days!
  

### 📅 Historical View

* Top panel dropdown shows **every date** from your earliest logged workout (up to 1 year ago) to today
* Selecting a date shows all workouts for that day

### ✏️ Edit Workouts

* Edit reps, time, or calories for any existing workout using a popup form
* Changes are automatically saved to the database
* Changes are immediately reflected

### 🗑️ Delete Workouts

* Select any row and delete it instantly

### 🔥 Calorie Estimator

* Select a workout and click "Get Calorie Estimate"
* Uses external API logic, also developed in this project, to estimate based on reps/time (if available)
* Optionally overrides existing calories

### 📊 Trends Chart

* View a visual chart of your calories burned over time
* Automatically aggregates calories per day and shows trends

### 📂 CSV Export (Downloads Folder)

* One-click export of all workouts (across all dates) for the current user
* Output saved automatically as:

````
\~/Downloads/workouts\_export.csv
````

* Columns: Date, Workout, Reps, Time (min), Calories

---

## 🧱 Technologies Used

* Java 17
* Swing (GUI)
* SQLite (local storage)
* JDBC (SQL integration)
* JFreeChart (for trends visualization)
* Multithreading using Java SwingWorker

---

## 🛠️ How to Run

1. This project needs Java 17+ installed
2. Clone the repository
3. Run the following commands in your terminal after navigating to the cloned project repository
   ````
   cd workout-tracker
   cd calorieestimator
   ./mvnw clean install
   ./mvnw spring-boot:run
   ````
   This will get the SpringBoot API running for calorie estimation

   This command MUST produce and output like:

 ````
 o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
 ````

Keep this running in one terminal, and open a new window of the terminal to compile and run the UI part

   
5. Compile:
   (Run this command inside the workout-tracker directory)
   ````
    javac -cp "lib/*:src" -d out $(find src -name "*.java")
   ````

4. Run:
(Run this command inside the workout-tracker directory)
   ````
   java -cp "lib/*:src" main.Main
   ````

---

## 📎 Folder Structure

```
src/
├── api/                    # CalorieEstimatorClient logic
├── gui/                    # All UI classes
│   ├── DashboardWindow.java
│   ├── WorkoutLogWindow.java
│   ├── LogingWindow.java
│   └── ChartPopup.java
├── util/                   # CSV exporter
│   └── CSVExporter.java
└── model/                  # Table model for workouts
    └── WorkoutTableModel.java
```

Made with ❤️ by Kavya Khurana 


