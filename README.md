# GreenClicker - Final NDK Enabled Project

This repository contains an Android prototype that:
- Captures the screen (MediaProjection)
- Detects green pixels using HSV thresholds
- Issues tap gestures via AccessibilityService
- Uses a native (C++) detection routine (NDK/CMake) for faster processing

What I provide here:
- Full Android Studio project (app module)
- Native code (src/main/cpp) with CMakeLists.txt
- Scripts to build and sign the APK locally
- GitHub skeleton (LICENSE, issue templates)

**Important**
- I cannot build or sign the APK on your behalf in this environment.
- Follow `build_and_sign.sh` locally on a machine with Android SDK & NDK installed.
