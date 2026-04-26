@echo off
setlocal

rem === Set your JAVA_HOME here if it is not configured on your system ===
set "JAVA_HOME=C:\Program Files\Java\jdk-21"

set MAVEN_VERSION=3.8.8
set MAVEN_ZIP=apache-maven-%MAVEN_VERSION%-bin.zip
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/%MAVEN_ZIP%
set MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%

if not exist "%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" (
    echo Downloading Maven %MAVEN_VERSION%...
    mkdir "%MAVEN_DIR%" 2>nul
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_DIR%\%MAVEN_ZIP%'"
    powershell -Command "Expand-Archive -Path '%MAVEN_DIR%\%MAVEN_ZIP%' -DestinationPath '%MAVEN_DIR%' -Force"
    del "%MAVEN_DIR%\%MAVEN_ZIP%"
)

"%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" %*
endlocal
