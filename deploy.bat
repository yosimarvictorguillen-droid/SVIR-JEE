@echo off
setlocal

set "TOMCAT_HOME=C:\apache-tomcat-11.0.24"
set "PROJECT_DIR=%~dp0"
set "WAR_FILE=%PROJECT_DIR%target\svir-jee.war"

echo.
echo === Desplegando SVIR-JEE en Tomcat ===
echo.

if not exist "%WAR_FILE%" (
    echo ERROR: no se encontro %WAR_FILE%
    echo Corre "Clean and Build" en NetBeans primero.
    pause
    exit /b 1
)

echo Borrando despliegue anterior...
if exist "%TOMCAT_HOME%\webapps\svir-jee" rmdir /s /q "%TOMCAT_HOME%\webapps\svir-jee"
if exist "%TOMCAT_HOME%\webapps\svir-jee.war" del /f /q "%TOMCAT_HOME%\webapps\svir-jee.war"

echo Copiando el nuevo .war...
copy /y "%WAR_FILE%" "%TOMCAT_HOME%\webapps\svir-jee.war" >nul

echo.
echo Listo. Si Tomcat ya esta corriendo, espera unos segundos y abre:
echo   http://localhost:8080/svir-jee/
echo.
echo Si Tomcat NO esta corriendo, se abrira ahora...
echo.

tasklist /FI "IMAGENAME eq java.exe" | find /I "java.exe" >nul
if errorlevel 1 (
    start "" "%TOMCAT_HOME%\bin\startup.bat"
    echo Esperando a que Tomcat arranque...
    timeout /t 8 /nobreak >nul
)

start "" "http://localhost:8080/svir-jee/"

endlocal
