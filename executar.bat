@echo off
title Alucar - Sistema de Locacao de Veiculos
cd /d "%~dp0"

set "JAVA_CMD=java"

if exist "C:\Users\pmarq\.jdks\openjdk-26.0.2\bin\java.exe" (
    set "JAVA_CMD=C:\Users\pmarq\.jdks\openjdk-26.0.2\bin\java.exe"
) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\jbr\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\jbr\bin\java.exe"
) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe"
)

"%JAVA_CMD%" -jar target\alucar-usuario-1.0.0-jar-with-dependencies.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocorreu um erro ao executar a aplicacao.
    pause
)
