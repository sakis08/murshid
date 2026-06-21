# مرشد — Murshid

تطبيق البث الصوتي من غرفة العمليات لجهاز التحكم (DJI RC Pro / RC Plus).

## بناء APK

### الطريقة 1: GitHub Actions (تلقائي)

1. ارفع المشروع لمخزن GitHub
2. GitHub Actions يبني APK تلقائياً
3. نزل APK من `Actions` → `Build APK` → `murshid-apk`

### الطريقة 2: Android Studio (يدوي)

```
File → Open → android/Murshid
Build → Build Bundle(s) / APK(s) → APK
```

### الطريقة 3: سطر أوامر

```
cd android/Murshid
./gradlew assembleDebug
```

الـ APK موجود في:
`app/build/outputs/apk/debug/app-debug.apk`
