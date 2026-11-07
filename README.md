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
6. Create the **Realtime Database** in `asia-southeast1`. The configured endpoint is
   `https://shasthobondhu-d5bdf-default-rtdb.asia-southeast1.firebasedatabase.app`.
7. Keep `app/google-services.json` downloaded from that Firebase project. The build also accepts
   a `FIREBASE_DATABASE_URL` Gradle property for separate environments.

Deploy the restricted Firestore and Realtime Database rules included in this repo:

```bash
firebase deploy --only firestore:rules
firebase.cmd deploy --only database
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
  calendar-selected last donation date and availability. Profiles synchronize through Firestore
  and RTDB. Search supports division, exact district/area and optional blood group.
* **Emergency requests** — create push-ID requests in RTDB, mirror them to Firestore, accept an
  open request, and let its owner mark accepted requests Fulfilled or Cancelled. Security rules
  enforce ownership and valid transitions.
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
* **Live donor availability** — willingness is stored in `/blood_donors`; genuine connection
  state comes from `.info/connected` and `/user_status`. “Available now” requires both.
* **Private notifications** — `/notifications/{uid}` is visible only to that user. Clients can
  mark records read but only a trusted backend can create them.
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
| Foreground service | `HealthMonitorService`, Hub monitor toggle, ongoing live RTDB/Firestore notification |
| HTTP and typed JSON parsing | `PublicHealthApi`, `NetworkModule`, `HealthInfoRepository`, API DTOs and mappers |
| Firebase Auth, Firestore, RTDB and FCM | `FirebaseService`, `RealtimeDatabaseRepository`, `PresenceRepository`, `NotificationRepository`, `ShasthoMessagingService` |

## Manual setup and API notes

### Realtime Database

The final rules are intentionally restrictive. Review `database.rules.json` and
`docs/RTDB_INTEGRATION_PLAN.md`, then deploy manually:

```powershell
firebase.cmd deploy --only database
```

Run the local Auth + Database emulator security smoke suite before deployment:

```powershell
firebase.cmd emulators:exec --only auth,database --project shasthobondhu-d5bdf `
  "powershell -NoProfile -ExecutionPolicy Bypass -File scripts\rtdb-rules-smoke.ps1"
```

Paths created automatically by app use are `/users`, `/blood_donors`, `/blood_requests`,
`/blood_request_status`, and `/user_status`. `/notifications`, `/hospitals`, and `/medicines`
remain empty until a trusted backend or Firebase Admin SDK writes real records.

Hospital, medicine, and notification creation requires either the Admin SDK (which bypasses
client rules) or an authenticated administrator with a server-issued custom claim:
`{ "admin": true }`. Never derive this permission from `/users/{uid}/role`.

### Public APIs

* Medicine labels use the key-less openFDA drug-label endpoint. A 404 is an expected empty result;
  common local names containing “Napa” or “paracetamol” are queried as `acetaminophen`.
* The Hub's Bangladesh card uses the key-less disease.sh country endpoint. Values are public
  reported COVID-19 totals and are informational, not medical advice.
* Both endpoints were reachable during implementation. They can change independently of the app,
  so every request has timeout, retry, empty, malformed-data and failure handling.
