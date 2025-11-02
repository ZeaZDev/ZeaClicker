#!/bin/bash
# Generates a release keystore locally using keytool.
# Update values as needed.
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/release.keystore -alias releasekey -keyalg RSA -keysize 2048 -validity 10000       -storepass changeit -keypass changeit -dname "CN=Your Name, OU=Dev, O=YourOrg, L=City, S=State, C=US"
echo "Keystore generated at keystore/release.keystore (password: changeit)"
