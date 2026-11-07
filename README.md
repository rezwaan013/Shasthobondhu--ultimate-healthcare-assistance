# ShasthoBondhu

**ShasthoBondhu** is a native Android health-support application for Bangladesh. It connects
people with blood donors, emergency blood requests, hospitals, medicine availability, private
notifications, and practical emergency tools in Bangla and English.

## Download

[**Download the latest debug APK**](releases/ShasthoBondhu-v1.0-debug.apk)

```text
SHA-256: F3D1D1CE237F398B9103922AABF6B43E0CD5BE5E0F5EBFBD200BF1E7C5C35D99
```

> The downloadable file is a debug build intended for coursework, demonstration, and testing.
> Android may ask you to allow installation from the browser or file manager used to open it.

## Highlights

- **Verified accounts** — Firebase Email/Password registration, verification email, resend flow,
  verified-login enforcement, and secure password reset.
- **Blood donors** — donor profile, blood group and exact-area search, donation eligibility,
  calling, map navigation, and live availability.
- **Genuine presence** — Firebase `.info/connected` and `onDisconnect()` maintain online status;
  the **Available now** badge requires both donor willingness and an active Firebase connection.
- **Emergency requests** — RTDB push IDs, request ownership, transactional acceptance, and secure
  Open → Accepted → Fulfilled/Cancelled transitions.
- **Hospitals and medicines** — searchable local/offline data with authenticated RTDB reads;
  sensitive cloud writes require a trusted administrator claim.
- **Medicine information** — keyless openFDA label lookup with typed JSON parsing and safe
  loading, empty, network, malformed-data, and server-error states.
- **Emergency Hub** — emergency contact import, call/SMS actions, public Bangladesh health data,
  system-condition warnings, live request monitoring, and donor availability controls.
- **Private notifications** — per-user RTDB notifications that clients may read and mark as read,
  but cannot create or rewrite.
- **Device integration** — runtime permissions, Contacts Content Provider, implicit intents,
  system broadcasts, foreground service, FCM, dark mode, and responsive landscape layouts.

## Verified requirements

| Requirement | Implementation |
|---|---|
| Multi-Activity navigation and explicit intents | `EmergencyHubActivity`, `DonorDetailActivity` |
| Implicit intents | Dialer, SMS, Google Maps, browser fallback |
| View Binding and UI event handling | Activities, Fragments, adapters, and row bindings |
| RecyclerView, adapters, and ViewHolders | Donors, requests, hospitals, medicines, contacts, notifications |
| Responsive layouts and rotation | Portrait plus two-column `layout-land` activity resources |
| Runtime permissions | Reusable notification/contact `PermissionHelper` |
| BroadcastReceiver | Airplane mode, battery low/okay, and power saver events |
| Content Provider | Off-main `ContactsContract` query with safely closed cursor |
| Foreground service | Ongoing emergency-request monitor notification |
| HTTP and JSON parsing | Retrofit, OkHttp, Gson, openFDA, disease.sh |
| Firebase services | Authentication, Firestore, RTDB, FCM |
| Realtime presence | `.info/connected`, `onDisconnect()`, server timestamp |
| Security rules | Owner checks, private notifications, validated transitions, admin claims |

## Architecture

The project follows the existing Java MVVM/repository architecture:

```text
View (Activity/Fragment + View Binding)
        ↓
Hilt ViewModel + LiveData
        ↓
Repository layer
   ├── Room local/offline cache
   ├── Firebase Authentication
   ├── Cloud Firestore
   ├── Firebase Realtime Database
   └── Retrofit public APIs
```

Firestore and Room remain available for durable/offline behavior. RTDB supplies live profiles,
donor discovery, request updates, presence, notifications, and administrator-managed reference
data without removing existing features.

## Technology

| Area | Choice |
|---|---|
| Language | Java 11 |
| UI | XML, Material 3, View Binding, ConstraintLayout |
| Architecture | MVVM, Repository, LiveData |
| Dependency injection | Hilt |
| Local database | Room |
| Cloud | Firebase Auth, Firestore, RTDB, FCM |
| Networking | Retrofit, OkHttp, Gson |
| Build | AGP 7.3.1, Gradle 7.4, Groovy DSL |

`minSdk 24` · `targetSdk 33` · `compileSdk 33`

## Project structure

```text
app/src/main/java/com/shasthobondhu/app/
├── MainActivity.java
├── EmergencyHubActivity.java
├── DonorDetailActivity.java
├── data/
│   ├── local/              # Room entities and DAOs
│   ├── model/              # UI/domain models
│   ├── remote/             # Firebase, FCM, Retrofit APIs and DTOs
│   └── repository/         # Local, cloud, presence, contacts, and API repositories
├── di/                     # Hilt modules and entry points
├── service/                # Emergency foreground monitor
├── system/                 # Android system-event receiver and state
├── ui/
│   ├── adapters/
│   └── fragments/
├── util/
└── viewmodel/

docs/RTDB_STRUCTURE.md      # Expected cloud paths and trusted-admin setup
scripts/rtdb-rules-smoke.ps1
database.rules.json
firestore.rules
firebase.json
```

## Build and test

### Requirements

- JDK 11
- Android SDK 33
- Android Studio or command-line Gradle
- `app/google-services.json` for package `com.shasthobondhu.app`

### Build the APK

```powershell
.\gradlew.bat clean assembleDebug
```

Generated APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run all local checks

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug lintDebug
```

The latest verification completed with a successful build, passing unit tests, and **0 lint
errors**. The app was also installed and launched on a Pixel 5 Android 11 emulator without an
`AndroidRuntime` crash.

## Firebase configuration

Firebase project:

```text
shasthobondhu-d5bdf
```

Realtime Database endpoint:

```text
https://shasthobondhu-d5bdf-default-rtdb.asia-southeast1.firebasedatabase.app
```

Enable Email/Password Authentication and create Firestore plus Realtime Database. The app's
`google-services.json` belongs in `app/`.

### Deploy rules

Review the rules before deployment, then run:

```powershell
firebase.cmd deploy --only database --project shasthobondhu-d5bdf
firebase.cmd deploy --only firestore:rules --project shasthobondhu-d5bdf
```

No production rule deployment is performed automatically by this repository.

### Test RTDB rules locally

```powershell
firebase.cmd emulators:exec --only auth,database --project shasthobondhu-d5bdf `
  "powershell -NoProfile -ExecutionPolicy Bypass -File scripts\rtdb-rules-smoke.ps1"
```

The smoke suite verifies authentication, ownership, donor discovery, presence, request
transitions, notification privacy, and administrator-only hospital/medicine writes.

## Realtime Database security

| Path | Read | Write |
|---|---|---|
| `/users/{uid}` | Owner | Owner |
| `/blood_donors` | Authenticated users | Donor owner |
| `/blood_requests` | Authenticated users | Requester or controlled donor acceptance |
| `/blood_request_status` | Authenticated users | Authorized requester/accepting donor |
| `/notifications/{uid}` | Owner | Owner can only mark read; trusted backend creates |
| `/user_status` | Authenticated users | Status owner |
| `/hospitals` | Authenticated users | `admin: true` only |
| `/medicines` | Authenticated users | `admin: true` only |

Root read/write access is denied. Android account roles do not grant administration. Production
hospital, medicine, and notification creation must use Firebase Admin SDK, Cloud Functions, or a
server-issued Auth custom claim:

```json
{ "admin": true }
```

See [RTDB_STRUCTURE.md](docs/RTDB_STRUCTURE.md) for the complete expected data structure and
automatic first-write behavior.

## Permissions

- `INTERNET`, `ACCESS_NETWORK_STATE`
- `POST_NOTIFICATIONS` on Android 13+
- `READ_CONTACTS`, requested only when selecting an emergency contact
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`

No location permission is requested.

## Data and privacy notes

- User profiles are private to their owners.
- Donor phone numbers are available only to authenticated users because calling donors is a core
  feature.
- Notification creation is denied to ordinary clients.
- Presence contains only `online` and `lastSeen`; donor willingness remains separate.
- Room demo records are local development data and are never uploaded as production RTDB data.
- Public medicine and health endpoints are informational and not medical advice.

## Localisation

UI strings are provided in:

- `res/values/strings.xml` — English
- `res/values-bn/strings.xml` — Bangla

The green day/night theme, large touch targets, and scrollable forms support phones in portrait
and landscape orientations.
