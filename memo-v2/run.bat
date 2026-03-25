@echo off
REM MEMO_V2 - Activity Tracker Launcher
cd /d "%~dp0"
call mvn exec:java
pause
