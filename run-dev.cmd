@echo off
cd /d "%~dp0"
echo Building project...
call mvn -DskipTests package
if errorlevel 1 exit /b 1
echo Starting Video Factory at http://localhost:8080
java -jar target\tool-0.0.1-SNAPSHOT.jar
