@echo off
cd /d "%~dp0"
title إدارة العمليات - بث الدرون

echo ==============================
echo   إدارة العمليات - بث الدرون
echo ==============================
echo.

REM تأكد من وجود node_modules
if not exist "node_modules" (
    echo [!!] الرجاء تشغيل setup.bat أولاً
    echo.
    pause
    exit /b
)

REM تشغيل WebSocket relay للمرشد (للاتصال من APK)
start /B "مرشد 6789" cmd /c "node server/ws-relay.js"
echo [🎙] WebSocket relay على port 6789 (لـ APK)

REM تشغيل السيرفر الموحد (HTTP + WebSocket)
echo [🚀] تشغيل برنامج إدارة العمليات...
start /B "سيرفر 3000" cmd /c "node server/index.js"

REM فتح المتصفح بعد تأخير بسيط
timeout /t 2 /nobreak >nul
start http://127.0.0.1:3000/

echo.
echo [✅] البرنامج شغال على: http://127.0.0.1:3000
echo.
echo [🔴] أضغط Ctrl+C أو أقفل النافذة لإيقاف البرنامج
echo.
pause
