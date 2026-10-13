:: Script para compilar y ejecutar la aplicacion de Tareas
@echo off

:: Cambiar al directorio del script; /d permite cambiar de unidad si es necesario
cd /d "%~dp0" 

echo Compilando la aplicacion...
javac -encoding UTF-8 Main.java MainWindow.java Mainframe3270.java LoginException.java

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
echo 1. Una version de JDK instalada y configurada en PATH.
echo    - java --version
echo    - javac --version
echo 2. Debe existir el fichero:
echo    C:\Program Files\wc3270\ws3270.exe
