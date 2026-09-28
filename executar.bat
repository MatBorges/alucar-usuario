@echo off
title Alucar - Cadastro de Usuarios
cd /d "%~dp0"
"C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe" -jar target\alucar-usuario-1.0.0-jar-with-dependencies.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocorreu um erro ao executar a aplicacao.
    pause
)
