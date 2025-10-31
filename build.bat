@echo off
title "Build & Deploy DataCore Plugin"
echo =========================================
echo Building DataCore via Maven...
echo =========================================

rem === Projektpfad (wo deine pom.xml liegt) ===
set PROJECT_DIR=%~dp0

rem === Zielordner deines Servers (ändern!) ===
set SERVER_PLUGIN_DIR=D:\Server\paper-1.21.8\plugins

rem === Namen / Muster deiner Plugin-Datei ===
set PLUGIN_NAME=datacore
set PLUGIN_VERSION=1.0

rem === Baue das Projekt ===
cd /d "%PROJECT_DIR%"
call mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed! Aborting...
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Maven build completed successfully!
echo.

rem === Pfad zur fertigen JAR finden ===
set TARGET_DIR=%PROJECT_DIR%target
set SHADED_JAR=%TARGET_DIR%\%PLUGIN_NAME%-%PLUGIN_VERSION%-shaded.jar
set NORMAL_JAR=%TARGET_DIR%\%PLUGIN_NAME%-%PLUGIN_VERSION%.jar

if exist "%SHADED_JAR%" (
    set FINAL_JAR=%SHADED_JAR%
) else (
    set FINAL_JAR=%NORMAL_JAR%
)

if not exist "%FINAL_JAR%" (
    echo Could not find built JAR file in %TARGET_DIR%
    pause
    exit /b 1
)

echo Deploying "%FINAL_JAR%" to "%SERVER_PLUGIN_DIR%" ...
copy /Y "%FINAL_JAR%" "%SERVER_PLUGIN_DIR%\%PLUGIN_NAME%.jar" >nul

if %ERRORLEVEL% EQU 0 (
    echo Deployment successful!
) else (
    echo Failed to copy file to server folder!
)

echo.
exit
