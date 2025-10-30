# Deployment Guide

This document provides instructions for building, signing, and deploying the Xamu Wetlands Android application.

---

## 1. Prerequisites

- **Android Studio**: Ensure you have the latest stable version of Android Studio installed.
- **Java Development Kit (JDK)**: JDK 17 is required.
- **Google Services**: You must have a `google-services.json` file from your Firebase project.
- **Keystore**: You will need a signing keystore to sign the release build of the application.

## 2. Building the Application

### Debug Build

To create a debug build for testing, you can use the following Gradle task:

```bash
./gradlew assembleDebug
```

This will generate an unsigned APK in the `app/build/outputs/apk/debug` directory.

### Release Build

To create a release build, you will need to configure the signing information in your `local.properties` and `build.gradle.kts` files.

1. **Create or update your `keystore.properties` file** in the root of the project with the following information:

```properties
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
storeFile=path/to/your/keystore.jks
```

2. **Run the `assembleRelease` Gradle task**:

```bash
./gradlew assembleRelease
```

This will generate a signed APK in the `app/build/outputs/apk/release` directory, which can then be uploaded to the Google Play Store.

## 3. Deployment to Google Play Store

1. **Log in to the Google Play Console**.
2. **Select your application**.
3. **Navigate to the "Production" track** and create a new release.
4. **Upload the signed release APK** generated in the previous step.
5. **Enter the release notes** and submit the release for review.
