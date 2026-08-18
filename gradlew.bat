@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0gradle\wrapper\gradlew.ps1" %*
exit /b %ERRORLEVEL%
