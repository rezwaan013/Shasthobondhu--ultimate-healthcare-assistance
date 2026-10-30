# ShasthoBondhu - Healthcare Access Platform

An Android application to improve access to essential healthcare services in Bangladesh.

## Tech Stack
- **Language:** Java 11
- **UI:** XML, Material 3, ViewBinding
- **Architecture:** MVVM (ViewModel, LiveData)
- **DI:** Hilt
- **Database:** Room (Local caching)
- **Networking:** Retrofit
- **Backend:** Firebase (Auth, Firestore, FCM)

## Environment Constraints
- Android Studio Dolphin 2021.3.1
- AGP 7.3.x, Gradle 7.4
- JDK 11
- minSdk 24, targetSdk 33, compileSdk 33

## Firebase Setup
1. Create a new project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package name `com.shasthobondhu.app`.
3. Download the `google-services.json` file.
4. Place `google-services.json` in the `app/` directory.
5. Enable **Phone Authentication** and **Cloud Firestore** in the Firebase console.

## Run Steps
1. Open the project in Android Studio Dolphin.
2. Sync Gradle.
3. Build and Run on an emulator or physical device (API 24+).
