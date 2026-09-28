@echo off
title Rebuilding Study Planner
echo ========================================================
echo            REBUILDING STUDY PLANNER BACKEND
echo ========================================================
echo.

set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Users\acer\.gemini\antigravity\scratch\tools\apache-maven-3.9.6\bin;%PATH%"

cd /d "C:\Users\acer\.gemini\antigravity\scratch\STUDY-PLANNER\Backend"

echo Synchronizing Frontend files to static resources...
xcopy /E /I /Y "..\Frontend\*" "src\main\resources\static\"

echo Running Maven Clean Package...
call mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo.
    echo ========================================================
    echo             BUILD COMPLETED SUCCESSFULLY!
    echo   You can now launch the application using start-study-planner.bat
    echo ========================================================
) else (
    echo Build failed with error code %errorlevel%.
)

pause
