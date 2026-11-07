# ShasthoBondhu — Ultimate Healthcare Assistance

A native Android app (Java) that improves access to essential healthcare services in Bangladesh:
blood donors, emergency blood requests, hospitals/clinics and medicine availability — with
Bangla + English support and area-based search.

---

## Tech stack

| Area | Choice |
|---|---|
| Language | Java 11 (no Kotlin) |
| UI | XML + Material Components 3, ViewBinding |
| Architecture | MVVM (ViewModel + LiveData) + Repository |
| DI | Hilt (`annotationProcessor`, no kapt) |
| Local cache | Room |
| Networking | Retrofit + OkHttp + Gson |
| Backend | Firebase Auth, Firestore, Realtime Database, Cloud Messaging |
| Build | AGP 7.3.1 · Gradle 7.4 · JDK 11 · Groovy DSL |

`minSdk 24` · `targetSdk 33` · `compileSdk 33`

---

## Project structure

```
app/src/main/java/com/shasthobondhu/app/
├── ShasthoBondhuApp.java          # @HiltAndroidApp
├── MainActivity.java              # primary activity + BottomNavigationView
├── LabComponentsActivity.java     # second activity + Android/Firebase live demos
├── data/
│   ├── local/                     # Room: AppDatabase, entities, dao
│   ├── remote/                    # FirebaseService, public health API, FCM service, DTOs
│   └── repository/                # Donor, Request, Hospital, Medicine, PasswordReset
├── di/                            # DatabaseModule, NetworkModule (Hilt)
├── ui/
│   ├── adapters/                  # ViewBinding RecyclerView adapters
│   └── fragments/
│       ├── auth/                  # Registration, ForgotPassword
│       ├── auth/login/            # Email/password login
│       ├── home/ donors/ requests/ hospitals/ medicines/
├── util/                          # AreaDataset, ValidationUtils, DataSeeder, constants
└── viewmodel/                     # Auth, ForgotPassword, Donor, Request, Hospital, Medicine

app/src/main/res/
├── layout/            # fragments, items, activity_main
├── navigation/nav_graph.xml
├── menu/bottom_nav_menu.xml
├── values/            # English strings, colors, themes (Material 3)
└── values-bn/         # Bangla strings
```

---

## Firebase setup

1. Create a project at <https://console.firebase.google.com>.
2. Add an Android app with package name **`com.shasthobondhu.app`**.
3. Download `google-services.json` and put it in **`app/google-services.json`**
   (it must be inside `app/`, not the project root).
4. Enable **Authentication → Sign-in method → Email/Password**. No SHA fingerprint is
   required because this app does not use Phone or Google Authentication.
5. Create a **Cloud Firestore** database.
6. Create a **Realtime Database** in the same Firebase project.
   If Firebase gives it a regional URL, place that URL in `gradle.properties`:
   `FIREBASE_DATABASE_URL=https://your-database-url/`.

Deploy the Firestore rules included in this repo:

```bash
firebase deploy --only firestore:rules,database
```

Alternatively, paste `database.rules.json` into **Realtime Database → Rules** in Firebase
Console and click **Publish**. The in-app cloud check identifies unpublished rules as a
permission error.

---

## Forgot password

The screen at **Login → Forgot password?** uses Firebase Authentication's hosted,
expiring reset link:

1. Enter the account email.
2. Firebase sends a secure password-reset email.
3. Open the link, choose a new password, and return to the app to sign in.

This requires no custom server, Gmail credential, Cloud Function, billing plan, SHA
fingerprint, or app secret. Firebase handles token generation, expiration and password update.

---

## Build & run

1. Open the project in **Android Studio Dolphin 2021.3.1** (or newer).
2. Make sure `app/google-services.json` exists.
3. Let Gradle sync (AGP 7.3.1 / Gradle 7.4 / JDK 11).
   If the wrapper is missing on your machine:
   ```bash
   gradle wrapper --gradle-version 7.4
   ```
4. Run on a device/emulator with **API 24+** and Google Play Services.

> Samsung devices may block sideloaded debug builds with
> `INSTALL_FAILED_VERIFICATION_FAILURE`; temporarily disable *Play Protect →
> Scan apps with Play Protect* or install via Android Studio's Run button.

---

## Features

* **Blood donors** — register with name, phone, blood group, division/district/upazila,
  last donation date and availability. Search **by blood group and by area**
  (any level of the hierarchy), then call the donor.
* **Emergency requests** — create a request (patient, group, units, hospital, location,
  urgency, contact), accept another user's request, see who accepted, and call the requester.
* **Hospitals & clinics** — search by name, area, test or service; call or launch
  turn-by-turn Google Maps navigation.
* **Medicines** — search a medicine and see participating medicine shops with
  **In Stock / Low / Out of Stock**; shop owners update stock and users can navigate
  to the pharmacy in Google Maps.
* **Auth-first role flow** — the app opens only to Login/Register. Registration chooses
  **General user / blood donor**, **Medicine shop owner**, or **Clinic owner**. General users
  complete and later edit availability/last-donation details; owners route directly to their
  management dashboard. A salted local fallback keeps
  the roles testable when Firebase Email/Password is not enabled.
* **Offline first** — Room caches everything; screens expose loading / empty / error states.
* **Localisation** — complete English (`values/`) and Bangla (`values-bn/`) string sets.
* **Accessible visual modes** — a persistent forest-green light/dark theme, boxed service
  selections and large touch targets tuned on a Samsung Android 14 device.
* **Lab components** — explicit and implicit Intents, system broadcasts, contact picker,
  runtime notification permission, updating foreground-service notification, public JSON API,
  and Auth + Firestore + Realtime Database cloud verification.

See **[LAB_DEMONSTRATION.md](LAB_DEMONSTRATION.md)** for the exact live-demo sequence,
Firebase prerequisites, and code references.

---

## Data notes

`util/AreaDataset` ships a starter division → district → upazila tree (Dhaka, Chattogram).
Extend it with the remaining divisions; the Room queries already match on all three levels.

`util/DataSeeder` inserts demo donors, requests, hospitals and medicines **once** on first
launch, on a background thread, so every screen has content immediately.
