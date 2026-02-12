@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties" (
  echo Missing maven-wrapper.properties
  exit /b 1
)

for /f "usebackq tokens=1,2 delims==" %%A in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") do (
  if "%%A"=="distributionUrl" set MAVEN_WRAPPER_DISTRIBUTION_URL=%%B
  if "%%A"=="wrapperUrl" set MAVEN_WRAPPER_JAR_URL=%%B
)

set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar

if not exist "%MAVEN_WRAPPER_JAR%" (
  if "%MAVEN_WRAPPER_JAR_URL%"=="" set MAVEN_WRAPPER_JAR_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
  powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_WRAPPER_JAR_URL%' -OutFile '%MAVEN_WRAPPER_JAR%'" || exit /b 1
)

set JAVA_CMD=java
if not "%JAVA_HOME%"=="" set JAVA_CMD=%JAVA_HOME%\bin\java

"%JAVA_CMD%" -classpath "%MAVEN_WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
