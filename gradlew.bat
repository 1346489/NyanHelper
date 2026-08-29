@echo off
if defined GRADLE_HOME (
  "%GRADLE_HOME%\bin\gradle" %*
) else (
  where gradle >nul 2>&1 && gradle %* || (echo Gradle not found & exit /b 1)
)
