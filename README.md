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
| Cloud data | Firebase Authentication, Firestore, Realtime Database presence, FCM |
| Build | AGP 7.3.1 · Gradle 7.4 · JDK 11 · Groovy DSL |

`minSdk 24` · `targetSdk 33` · `compileSdk 33`

---

## Project structure

```
app/src/main/java/com/shasthobondhu/app/
├── ShasthoBondhuApp.java          # @HiltAndroidApp
├── MainActivity.java              # primary activity + BottomNavigationView
├── EmergencyHubActivity.java      # emergency tools and foreground monitor controls
├── DonorDetailActivity.java       # explicit-intent donor details and result flow
├── data/
│   ├── local/                     # Room: AppDatabase, entities, dao
│   ├── remote/                    # Firebase, FCM, openFDA and public-health APIs
│   └── repository/                # Room, cloud, contacts, presence and network data
├── di/                            # Room and network Hilt modules
├── service/                       # Emergency Request Monitor foreground service
├── system/                        # dynamic Android system-event receiver
├── ui/
│   ├── adapters/                  # ViewBinding RecyclerView adapters
│   └── fragments/
│       ├── auth/                  # Registration, ForgotPassword
│       ├── auth/login/            # Email/password login
│       ├── home/ donors/ requests/ hospitals/ medicines/
├── util/                          # AreaDataset, ValidationUtils, DataSeeder, constants
└── viewmodel/                     # Auth, ForgotPassword, Donor, Request, Hospital, Medicine

app/src/main/res/
├── layout/            # fragments, rows and portrait activity layouts
├── layout-land/       # two-column Emergency Hub and donor-detail layouts
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
6. Create a **Realtime Database** for live donor presence.
7. Re-download `google-services.json` after creating Realtime Database. The file must contain
   `project_info.firebase_url`; the checked-in file currently does not, so presence intentionally
   fails soft until it is replaced.

Deploy the restricted Firestore and Realtime Database rules included in this repo:

```bash
firebase deploy --only firestore:rules,database
```

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
  calendar-selected last donation date and availability. Donor profiles synchronize through
  Firestore so updates appear on other phones. Search by selected division plus an exact,
  case-insensitive district/area match, optionally filtered by blood group.
* **Emergency requests** — create a request (patient, group, units, hospital, location,
  patient details, required date/time, urgency and contact), synchronize through Firestore,
  accept another user's request, see who accepted, and call the requester.
* **Hospitals & clinics** — search by name, area, test or service; call or launch
  turn-by-turn Google Maps navigation. Clinic owners also publish doctor speciality,
  available days and times.
* **Medicines** — search a medicine and see participating medicine shops with
  **In Stock / Low / Out of Stock**; shop owners update stock and users can navigate
  to the pharmacy in Google Maps. “More info” loads nullable-safe indications and warnings from
  openFDA and clearly distinguishes loading, empty, malformed, network and server states.
* **Emergency Hub** — a separate, rotation-safe Activity for emergency contacts, live blood
  requests, donor presence, Bangladesh public-health statistics, system readiness and the
  Emergency Request Monitor.
* **Live donor availability** — authenticated donors can publish ephemeral “Available now” state
  to Realtime Database. Firestore remains the durable donor/profile source of truth.
* **Device-aware emergency delivery** — live airplane-mode, low-battery and power-saver warnings;
  runtime notification/contact permissions; contact-provider import; call and SMS intents.
* **Auth-first role flow** — the app opens only to Login/Register. Registration chooses
  **General user / blood donor**, **Medicine shop owner**, or **Clinic owner**. General users
  complete and later edit availability/last-donation details; owners route directly to their
  management dashboard. A salted local fallback keeps
  the roles testable when Firebase Email/Password is not enabled.
* **Offline first** — Room caches everything; screens expose loading / empty / error states.
* **Localisation** — complete English (`values/`) and Bangla (`values-bn/`) string sets.
* **Accessible visual modes** — a persistent forest-green light/dark theme, boxed service
  selections and large touch targets tuned on a Samsung Android 14 device.

---

## Data notes

`util/AreaDataset` contains all eight Bangladesh divisions: Dhaka, Chattogram, Rajshahi,
Khulna, Barishal, Sylhet, Rangpur and Mymensingh. District and area are intentionally entered
manually, and donor search performs exact district/area matching within the selected division.

`util/DataSeeder` inserts demo donors, requests, hospitals and medicines **once** on first
launch, on a background thread, so every screen has content immediately.

---

## Requirements coverage

| Requirement | Implementation |
|---|---|
| Multi-Activity navigation and explicit intents | `EmergencyHubActivity`, `DonorDetailActivity`, `HomeFragment`, `DonorFragment`, `DonorAdapter` |
| Implicit intents | `MapNavigationUtils`, donor/contact dial actions, emergency SMS `ACTION_SENDTO` |
| View Binding and event handling | All Activities/Fragments/adapters and generated `*Binding` classes |
| Responsive ConstraintLayout and rotation | `layout/activity_emergency_hub.xml`, `layout-land/activity_emergency_hub.xml`, and donor-detail equivalents |
| RecyclerView, adapters and ViewHolders | Donors, requests, hospitals, medicines, and contact picker |
| Runtime permissions | `PermissionHelper`, `EmergencyHubActivity` |
| System broadcasts | `SystemEventReceiver`, `SystemStatusRepository`, status banners in both activities |
| Content Provider | `EmergencyContactRepository` queries `ContactsContract` off-main with a closed cursor |
| Foreground service | `HealthMonitorService`, Hub monitor toggle, ongoing live Firestore notification |
| HTTP and typed JSON parsing | `PublicHealthApi`, `NetworkModule`, `HealthInfoRepository`, API DTOs and mappers |
| Firebase Auth, Firestore, RTDB and FCM | `FirebaseService`, existing repositories/Auth, `PresenceRepository`, `ShasthoMessagingService` |

## Manual setup and API notes

### Realtime Database

1. In Firebase Console, open **Build → Realtime Database → Create Database**.
2. Choose the region appropriate for the existing project.
3. Deploy `database.rules.json` with `firebase deploy --only database`.
4. Download a fresh Android `google-services.json` and replace `app/google-services.json`.
5. Confirm the new file has `project_info.firebase_url`; never add or invent a URL/key in source.

Without these steps, Auth, Firestore and FCM continue to work. Presence controls are disabled and
donor “Available now” badges stay hidden instead of crashing.

### Public APIs

* Medicine labels use the key-less openFDA drug-label endpoint. A 404 is an expected empty result;
  common local names containing “Napa” or “paracetamol” are queried as `acetaminophen`.
* The Hub's Bangladesh card uses the key-less disease.sh country endpoint. Values are public
  reported COVID-19 totals and are informational, not medical advice.
* Both endpoints were reachable during implementation. They can change independently of the app,
  so every request has timeout, retry, empty, malformed-data and failure handling.
