#!/bin/bash
# Run this locally on your machine with Android SDK & NDK installed.
# Usage: ./build_and_sign.sh
set -e
# 1) Build release AAB/APK
./gradlew clean assembleRelease

# 2) Find the unsigned APK (or use bundle)
APK_PATH=$(find app/build/outputs -name "*release*.apk" | head -n1)
echo "Unsigned APK: $APK_PATH"

# 3) Create keystore (if you don't have one)
echo "If you don't have a keystore, run: ./generate_keystore.sh"

# 4) Sign using apksigner (part of Android SDK build-tools)
# Update the keystore vars below or set env variables
KEYSTORE_PATH=keystore/release.keystore
KEY_ALIAS=releasekey
KEY_PASSWORD=changeit
if [ -f "$APK_PATH" ]; then
  apksigner sign --ks "$KEYSTORE_PATH" --ks-key-alias "$KEY_ALIAS" --ks-pass pass:$KEY_PASSWORD "$APK_PATH"
  echo "Signed APK at: $APK_PATH"
else
  echo "No APK found. Build may have failed."
fi
