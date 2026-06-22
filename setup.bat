@echo off
cd /d "%~dp0"
title إعداد إدارة العمليات - بث الدرون

echo =======================================
echo   إعداد برنامج إدارة العمليات
echo =======================================
echo.

REM التحقق من وجود Node.js
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [⚠] Node.js غير موجود!
    echo.
    echo تحميل Node.js...
    echo.
    REM تحميل Node.js (نسخة ويندوز 64 بت)
    echo افتح الرابط التالي في المتصفح واضغط Next Next Install:
    echo https://nodejs.org/dist/v22.22.3/node-v22.22.3-x64.msi
    echo.
    echo بعد التثبيت، أعد تشغيل هذا الملف.
    echo.
    start https://nodejs.org/dist/v22.22.3/node-v22.22.3-x64.msi
    pause
    exit /b
)

echo [✅] Node.js موجود: 
node --version
echo.

REM تثبيت الاعتماديات
echo [📦] جاري تثبيت الاعتماديات...
call npm install
if %errorlevel% neq 0 (
    echo [!!] خطأ في التثبيت. حاول تشغيل: npm install
    pause
    exit /b
)

echo.
echo [✅] تم الإعداد بنجاح!
echo.
echo شغّل البرنامج عن طريق فتح run.bat
echo.
pause
