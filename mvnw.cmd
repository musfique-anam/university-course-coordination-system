@REM Maven Wrapper for Windows
@echo off
setlocal

if "%JAVA_HOME%"=="" (
  echo Error: JAVA_HOME is not set.
  goto :error
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Error: JAVA_HOME points to invalid directory.
  goto :error
)

set MAVEN_PROJECTBASEDIR=%~dp0
:findBaseDir
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn" (
  cd ..
  if "%MAVEN_PROJECTBASEDIR%"=="%CD%" goto baseDirNotFound
  set MAVEN_PROJECTBASEDIR=%CD%
  goto findBaseDir
)
cd /d "%MAVEN_PROJECTBASEDIR%"
goto baseDirFound
:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%~dp0
cd /d "%MAVEN_PROJECTBASEDIR%"
:baseDirFound

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

for /f "tokens=1,2 delims==" %%A in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") do (
  if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
)

if not exist %WRAPPER_JAR% (
  echo Downloading Maven wrapper...
  powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')}"
  if errorlevel 1 (
    echo Failed to download Maven wrapper.
    goto :error
  )
)

"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
if errorlevel 1 goto :error
goto :eof

:error
endlocal
exit /b 1
