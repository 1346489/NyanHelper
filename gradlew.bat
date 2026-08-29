@echo off
REM 替代标准 gradlew.bat：直接调用系统 gradle，绕过 gradle-wrapper.jar
setlocal
where gradle >nul 2>&1
if %ERRORLEVEL% neq 0 (
  if defined GRADLE_HOME (
    "%GRADLE_HOME%\bin\gradle" %*
  ) else (
    echo ERROR: 未找到 gradle，请安装 Gradle 7.6.4 或设置 GRADLE_HOME
    exit /b 127
  )
) else (
  gradle %*
)
