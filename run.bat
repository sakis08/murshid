@echo off
cd /d "%~dp0"
title إدارة العمليات - بث الدرون

echo ==============================
echo   إدارة العمليات - بث الدرون
echo ==============================
echo.

echo [🚀] تشغيل برنامج إدارة العمليات...
start /B cmd /c "node server/index.js"

REM فتح المتصفح بعد تأخير بسيط
timeout /t 2 /nobreak >nul
start http://127.0.0.1:3000/

echo.
echo [✅] البرنامج شغال على: http://127.0.0.1:3000
echo.
echo [🔴] أضغط Ctrl+C أو أقفل النافذة لإيقاف البرنامج
echo.
pause
