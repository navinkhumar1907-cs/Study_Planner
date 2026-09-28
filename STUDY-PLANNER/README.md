# Study Planner - Complete Setup & Execution Guide

## Overview
Study Planner is a full-stack student web application built with **Spring Boot 3**, **MySQL**, and **HTML/CSS/JavaScript**. It automatically generates balanced study timetables with 1-hour sessions, scheduled 15-minute breaks, subject priority weights, and real-time progress tracking.

---

## 🚀 Quick Run (1-Click)
Simply double-click:
```text
C:\Users\acer\.gemini\antigravity\scratch\STUDY-PLANNER\start-study-planner.bat
```
This batch file will:
1. Verify the Java 17 environment.
2. Connect to MySQL (`study_planner_db`).
3. Start the Spring Boot server on port `8080`.
4. Automatically open `http://localhost:8080` in your web browser.

---

## 💻 Manual Step-by-Step Instructions

### Step 1: Ensure MySQL is Running
MySQL is installed as a Windows service and should be running with password `mysql123`.
To verify via PowerShell:
```powershell
Get-Service MySQL*
```
*(If stopped, start it with `Start-Service MySQL267`)*

### Step 2: Open Terminal / PowerShell
Navigate to the Backend directory:
```powershell
cd C:\Users\acer\.gemini\antigravity\scratch\STUDY-PLANNER\Backend
```

### Step 3: Run the Application
Run using the pre-compiled JAR:
```powershell
& "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\java.exe" -jar target\study-planner-1.0.0.jar
```
Or run directly with Maven:
```powershell
mvn spring-boot:run
```

You will see:
```text
Tomcat started on port 8080 (http) with context path ''
Started StudyPlannerApplication in X.XXX seconds
```

### Step 4: Open in Web Browser
Open your browser and navigate to:
**[http://localhost:8080](http://localhost:8080)**

---

## 🧑‍🎓 Student Workflow Walkthrough

1. **Sign Up (`register.html`)**:
   - Go to [http://localhost:8080/register.html](http://localhost:8080/register.html).
   - Enter your Full Name, Email, Password, and Confirm Password. Click **Register**.
2. **Log In (`login.html`)**:
   - Enter your registered email and password. Click **Log In**.
3. **Dashboard (`dashboard.html`)**:
   - View your total subjects, planned study hours, completed/pending sessions, and completion percentage.
   - Click **Create Study Plan**.
4. **Create Study Plan (`planner.html`)**:
   - **Step 1**: Enter your **Available Study Hours** (e.g. `4`) and **Starting Time** (e.g. `09:00 AM`).
   - **Step 2**: Add your subjects and assign priorities:
     - `Java Programming` -> **High**
     - `Database Management` -> **Medium**
     - `Computer Networking` -> **Low**
   - Click **⚡ Generate Schedule**.
5. **View Timetable & Time-Bar (`schedule.html`)**:
   - See the continuous visual time-bar and the session-by-session cards.
   - Every study session is 1 hour long.
   - A 15-minute break is automatically scheduled between every session.
6. **Track Progress**:
   - Click **"Mark Complete"** on any session after studying.
   - The status updates to **Completed** and the live progress bar recalculates automatically!

---

## 🛠️ Rebuilding the Project
If you ever change Java or frontend files, double-click:
```text
C:\Users\acer\.gemini\antigravity\scratch\STUDY-PLANNER\rebuild-project.bat
```
