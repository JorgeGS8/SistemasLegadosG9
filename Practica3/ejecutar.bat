:: Script para compilar y ejecutar la aplicacion de ....
@echo off

:: Cambiar al directorio del script; /d permite cambiar de unidad si es necesario
cd /d "%~dp0" 

:: FIXME RELLENAR DESPUES DE HACER EL PROYECTO
echo Compilando la aplicacion... 
javac -encoding UTF-8 Application.java

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo compilar la aplicacion.
    call :errorMessage
    pause
    exit /b 1
)

echo Iniciando la aplicacion...
java -cp "." Main

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo iniciar la aplicacion.
    call :errorMessage
    pause
    exit /b 1
)

exit /b 0

:errorMessage
echo.
echo Requisitos para ejecutar la aplicacion:
echo 1. 
echo 2. 
