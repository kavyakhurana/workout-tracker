# Workout Tracker

This is a Java project for NYU Tandon's CS9053 - Intro to Java. In this repo, I have developed a Java Swing application to track, visualize, and manage your workouts with calorie estimates and data exports.

The idea for this project stemmed from my recent interest in working out and gaining muscle, so making this project has been extremely fun.

---

## Scope

This project begins with user authentication. The user can register or log in to their account, the details of which are saved on locally, in an SQLite Database.
Netx, the user is presented with a dashboard where they can log that day's workout, or a workout in the past (up to 1 year prior). They can log the name of their workout and optionally - the number of reps, time spent on the exercise and the calories burnt. 

This project also hosts an API, running on SpringBoot, which will provide an estimate for the number of calories burnt by the exercise, and the API consists of some of the most popular exercises, and it is flexible enough that more can be added.

The user can also view a chart of their workout history, ie, the amount of time they're spent working out over the previous week. Thir project provides a bar graph representation.

The user can also edit a workout, and if they do not enter the new calories for the new number of reps or new time, the app will automatically estimate the calories using the API. Incase the exercise does not exist, the app will present a dialog box providing feedback.

The user can also export their workouts into a CSV which will be saved in their Downloads folder.

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

* Top panel dropdown shows every date from your earliest logged workout (up to 1 year ago) to today
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

Incase it does not, re-run
   ````
   ./mvnw clean install
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
   java -cp "lib/*:out" main.Main
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
calorieestimator/
├── src/                    # CalorieEstimator API               
│   ├── main/
│       └── java/com/kavyakhuranakk5554/calorieestimator
│           ├── CalorieEstimatorController.java
│           ├── CaloriesEstimatorApiApplication.java
│           └── WorkoutRequest.java

```

---

## 🎉 3 Advanced Java Concepts Used

* Multithreading - inside the refreshWorkoutView() function (using SwingWorker)
  
    This ensures that the UI is not frozen when querying workouts from the SQLite database, which can take time.
    The doInBackground() function runs on a separate background thread, fetching workout data from the SQLite database.

  
* Databases

    This project uses a local SQLite Database to store the user's login data and workout data.

 
* API Design with SpringBoot

   I designed and implemented a RESTful endpoint using SpringBoot to get an estimate of the calories burnt in an exercise. 
---

Made with ❤️ by Kavya Khurana 


