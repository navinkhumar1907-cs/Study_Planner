@echo off
title Study Planner Application
echo ========================================================
echo        STARTING STUDY PLANNER WEB APPLICATION
echo ========================================================
echo.

set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Users\acer\.gemini\antigravity\scratch\tools\apache-maven-3.9.6\bin;%PATH%"

echo [1/3] Checking Java environment...
java -version
if %errorlevel% neq 0 (
    echo Error: Java 17 was not found!
    pause
    exit /b %errorlevel%
)

echo.
echo [2/3] Checking MySQL database connection...
echo MySQL Host: localhost:3306
echo Database: study_planner_db
echo.

echo [3/3] Starting Spring Boot server on port 8080...
echo.
echo Once started, you can access the application at:
echo   - Home Page:    http://localhost:8080
echo   - Login:        http://localhost:8080/login.html
echo   - Register:     http://localhost:8080/register.html
echo   - Dashboard:    http://localhost:8080/dashboard.html
echo.
echo Press Ctrl+C at any time to stop the server.
echo ========================================================
echo.

cd /d "C:\Users\acer\.gemini\antigravity\scratch\STUDY-PLANNER\Backend"

:: Automatically open default browser after a 4-second delay
start /b cmd /c "timeout /t 4 /nobreak >nul & start http://localhost:8080"

java -jar target\study-planner-1.0.0.jar
pause
