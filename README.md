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
│   ├── remote/                    # FirebaseService, PasswordResetApi, FCM service, DTOs
│   └── repository/                # Donor, Request, Hospital, Medicine, PasswordReset
├── di/                            # DatabaseModule, NetworkModule (Hilt)
├── ui/
│   ├── adapters/                  # ViewBinding RecyclerView adapters
│   └── fragments/
│       ├── auth/                  # Registration, ForgotPassword
│       ├── auth/login/            # Login, OtpVerify
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
4. Enable **Authentication → Sign-in method**:
   * **Phone** — used by the OTP login screen.
   * **Email/Password** — used by registration and the forgot-password flow.
   * **Anonymous** — used by the one-button Firebase lab verification.
5. Create a **Cloud Firestore** database.
6. Create a **Realtime Database** in the same Firebase project.
   If Firebase gives it a regional URL, place that URL in `gradle.properties`:
   `FIREBASE_DATABASE_URL=https://your-database-url/`.
7. Add your debug **SHA-1 / SHA-256** fingerprints (required for Phone Auth):
   ```
   ./gradlew signingReport
   ```
   then paste the values into *Project settings → Your apps*.

Deploy the Firestore rules included in this repo:

```bash
firebase deploy --only firestore:rules,database
```

---

## Forgot password (6-digit code by Gmail)

The screen is at **Login → Forgot password?** and runs in three steps:

1. Enter the account email.
2. Receive a **6-digit code** (valid 10 minutes, max 5 attempts).
3. Enter the code + a new password.

Because Firebase's client SDK cannot change another user's password, the code is
issued and verified by a small trusted backend. A ready-to-deploy implementation
lives in `functions/index.js` (Node 16, Firebase Admin + Nodemailer over Gmail SMTP).

### 1. Configure Gmail

Create a Google **App Password** (<https://myaccount.google.com/apppasswords>;
requires 2-Step Verification):

```bash
firebase functions:config:set \
  gmail.user="youraccount@gmail.com" \
  gmail.app_password="your-16-char-app-password"
```

### 2. Deploy

```bash
cd functions && npm install && cd ..
firebase deploy --only functions
```

Functions are deployed to `asia-south1`. Your endpoint base URL is:

```
https://asia-south1-<project-id>.cloudfunctions.net/
```

### 3. Point the app at it

Add this line to `gradle.properties` (or pass `-PSHASSTHO_RESET_URL=...`):

```properties
SHASSTHO_RESET_URL=https://asia-south1-<project-id>.cloudfunctions.net/
```

Rebuild. The app now calls `POST /sendCode` and `POST /resetPassword`, and the
6-digit code arrives in the user's Gmail inbox.

### Demo mode (no backend configured)

If `SHASSTHO_RESET_URL` is empty the app still runs the full UI:

* the code is generated locally and displayed on the screen (and in Logcat),
* after the code verifies, the app calls Firebase's `sendPasswordResetEmail`
  so you receive a real, secure reset link.

**Send a secure reset link instead** on the first step does exactly that at any time.
Nothing secret is stored in the app: Gmail credentials live only in the Cloud Function.

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
  urgency, contact), see the open-request feed and call the requester.
* **Hospitals & clinics** — search by name, area, test or service; call or launch
  turn-by-turn Google Maps navigation.
* **Medicines** — search a medicine and see participating pharmacies with
  **In Stock / Low / Out of Stock**; pharmacy owners update stock and users can navigate
  to the pharmacy in Google Maps.
* **Auth and roles** — phone OTP plus email/password, with **General user**,
  **Pharmacy owner**, and **Hospital owner** dashboards. A salted local fallback keeps
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
