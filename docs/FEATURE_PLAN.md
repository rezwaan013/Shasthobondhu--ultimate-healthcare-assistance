# Course Requirements Feature Plan

## Baseline and architecture

- Java 11 Android app (`minSdk 24`, `targetSdk 33`, `compileSdk 33`) using one launcher
  `MainActivity` and Navigation Component fragments.
- UI uses Material 3, View Binding, English and Bangla string resources, day/night colors,
  LinearLayout/RelativeLayout, and RecyclerView adapters with nested ViewHolders.
- MVVM is implemented with Hilt `@HiltViewModel` classes, repositories, Room/LiveData, and
  constructor injection. `DatabaseModule` provides Room and DAOs; Firebase wrappers are
  constructor-injectable.
- Firebase Authentication and Firestore are active, FCM is handled independently by
  `ShasthoMessagingService`, and Firestore remains the durable source of truth.
- The working tree contained substantial pre-existing uncommitted feature work before this
  plan. Implementation must not reset or discard it. Feature commits will stage only files and
  hunks that can be isolated safely; any integration hunk that cannot be separated without
  capturing unrelated work will remain clearly reported rather than rewriting user work.

## UI direction

The Emergency Hub will extend the existing Bangladesh field-green system rather than introduce
a generic dashboard style. Emergency red is reserved for SOS actions, forest green indicates a
ready donor, amber communicates degraded device conditions, and the existing surface/outline
tokens carry all other structure. Portrait uses a vertical readiness sequence; landscape uses a
two-column split with device/contact controls on the left and live blood/API information on the
right. Donor details use a strong blood-group marker and quiet clinical typography. Layouts stay
left-aligned, use the app's existing sans-serif family, and avoid decorative cards that do not
encode a real action or status.

## Feature 1 — Activities, explicit intents, and landscape layouts

Create:

- `EmergencyHubActivity.java`, `EmergencyHubViewModel.java`
- `DonorDetailActivity.java`
- `layout/activity_emergency_hub.xml`, `layout-land/activity_emergency_hub.xml`
- `layout/activity_donor_detail.xml`, `layout-land/activity_donor_detail.xml`

Modify:

- `HomeFragment.java`, `fragment_home.xml` — useful Emergency Hub entry and user extras.
- `DonorAdapter.java`, `DonorFragment.java` — adapter callback, explicit donor detail launch,
  Activity Result API, and contacted feedback.
- `AndroidManifest.xml`, `app/build.gradle`, English/Bangla strings.

Add dependency: `androidx.constraintlayout:constraintlayout:2.1.4`.

## Feature 2 — Runtime permissions

Create `util/PermissionHelper.java`. It registers modern Activity Result permission launchers,
shows rationale dialogs, reports denial, and opens app settings after permanent denial. The Hub
requests notifications when emergency monitoring is enabled and contacts when contact import is
requested. All notification checks are guarded for API 33+.

Modify the Hub activity and English/Bangla strings. Add permission `READ_CONTACTS`.

## Feature 3 — Device/system events

Create:

- `system/SystemEvent.java`
- `system/SystemEventReceiver.java`
- `data/repository/SystemStatusRepository.java`
- pure mapping unit tests.

Modify `MainActivity`, `EmergencyHubActivity`, both activity layouts, and strings. Both activities
dynamically register with `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)` during
`onStart` and unregister during `onStop`. The repository exposes current status with LiveData;
airplane mode, battery low/okay, and power saver become actionable dismissible banners.

No new permission is required.

## Feature 4 — Emergency contacts Content Provider

Create:

- `data/model/EmergencyContact.java`
- `data/repository/EmergencyContactRepository.java`
- `ui/adapters/ContactAdapter.java`
- `viewmodel/EmergencyContactViewModel.java`
- `layout/item_contact.xml` and a reusable contact-selection layout.

Modify the Hub activity/layouts and strings. `ContentResolver.query()` reads
`ContactsContract.CommonDataKinds.Phone` on the IO executor with try-with-resources. A selected
contact is persisted in SharedPreferences. ACTION_DIAL calls the contact and ACTION_SENDTO opens
an SMS containing the user's emergency blood-request context. Empty and denied states remain
usable.

Permission: `READ_CONTACTS` (requested only before reading contacts).

## Feature 5 — Emergency Request Monitor foreground service

Create:

- `service/HealthMonitorService.java`
- `data/repository/MonitorPreferences.java`

Modify the Hub toggle, `MainActivity` deep-link handling, manifest, and strings. The service calls
`startForeground()` immediately, then attaches a Firestore listener and filters open requests by
blood group and district. Its ongoing notification shows the live urgent count and opens the
requests destination. Listener registration is removed in `onDestroy`; start/stop state is
persisted.

Permissions: `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_DATA_SYNC`. Manifest service type:
`dataSync`. The app keeps the repository's existing `targetSdk 33`; Android 14 type declarations
are included without forcing a risky AGP/SDK migration.

## Feature 6 — Public JSON APIs

Create:

- `di/NetworkModule.java`
- `data/remote/PublicHealthApi.java`
- typed DTOs under `data/remote/model/`
- `data/model/Resource.java`
- `data/repository/HealthInfoRepository.java`
- `viewmodel/HealthInfoViewModel.java`
- medicine-information bottom-sheet layout and mapper unit tests.

Modify `MedicineAdapter`, `MedicineFragment`, `MedicineViewModel`, the Hub, layouts, strings, and
`app/build.gradle`.

Dependencies: Retrofit 2.9.0, Gson converter 2.9.0, and OkHttp logging interceptor 3.14.9
(compatible with the repository's current Android/Java baseline). Logging is BODY only in debug
and disabled in release; no credentials are logged.

Verified on 2026-11-07:

- `https://disease.sh/v3/covid-19/countries/bangladesh` returned a typed Bangladesh statistics
  object.
- openFDA returned typed label data for generic `acetaminophen`. A query for `paracetamol`
  returned HTTP 404, so the repository treats 404 as an empty state and includes the common
  Bangladesh alias mapping `paracetamol -> acetaminophen` while still allowing arbitrary names.

## Feature 7 — Realtime Database donor presence

Create:

- `data/model/DonorPresence.java`
- `data/repository/PresenceRepository.java`
- mapping unit tests
- `database.rules.json`

Modify `FirebaseService`, `DonorViewModel`, `DonorFragment`, `DonorAdapter`, donor item and Hub
layouts, strings, `firebase.json`, `app/build.gradle`, and README.

Dependency: `com.google.firebase:firebase-database` through the existing Firebase BoM.

`google-services.json` currently has no `project_info.firebase_url`. Do not invent or hard-code a
URL. `FirebaseService` will catch missing-URL configuration and expose presence as unavailable;
the UI will hide live badges/toggles without crashing. Manual setup required:

1. Enable Realtime Database for the existing Firebase project in Firebase Console.
2. Publish `database.rules.json`.
3. Download a fresh `google-services.json` containing `firebase_url` and replace the local file.

Rules limit `/presence/{uid}` writes to the authenticated matching UID. Firestore remains the
source of truth; RTDB stores only ephemeral availability and `lastSeen`.

## Validation

After each feature: run `assembleDebug`; run unit tests and lint when practical. Final validation
includes all unit tests, `assembleDebug`, manifest/resource review, and a README requirements
coverage/manual-setup update. Device-only behaviors (runtime dialogs, system broadcasts,
foreground notification, rotation, contacts, and Firebase Console setup) will be called out if
they cannot be exercised in this environment.
