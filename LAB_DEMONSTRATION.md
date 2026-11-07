# ShasthoBondhu — Lab Demonstration Guide

This guide maps every instructor requirement to a visible app action and its source code.

## Before the live demonstration

1. In Firebase Console, enable **Email/Password** Authentication. Phone and Anonymous
   providers are intentionally not used, so SHA fingerprints are not required.
2. Create both **Cloud Firestore** and **Realtime Database** in project
   `shasthobondhu-d5bdf`.
   If the Realtime Database uses a regional URL, set
   `FIREBASE_DATABASE_URL=https://your-database-url/` in `gradle.properties`.
3. Deploy the included restricted rules:

   ```bash
   firebase deploy --only firestore:rules,database
   ```

   Without Firebase CLI, open **Realtime Database → Rules**, replace the editor with the
   complete contents of `database.rules.json`, and press **Publish**. A cloud-check result that
   says `Permission denied` means these Realtime Database rules are not yet published.

4. Build and install the debug APK.
5. On Android 13+, grant notification permission when the lab screen requests it.

No user picture or media is stored by this app, so Firebase Storage is intentionally not used.

## 1. Overview and features

Demonstrate the three roles:

- **General user:** register as a blood donor, post a blood request, search hospitals by
  available test/service, search medicines by nearby pharmacy, call contacts, and navigate.
- **Pharmacy owner:** create a pharmacy and add, remove, or change medicine stock.
- **Hospital owner:** create a hospital and add or remove tests and services.

## 2. Architecture and navigation

### Multi-Activity and explicit Intent

From Home, press **Open lab demonstration**. `HomeFragment` creates an explicit Intent from
the main Activity flow to `LabComponentsActivity`.

Source:

- `MainActivity.java`
- `LabComponentsActivity.java`
- `HomeFragment.java`

### Implicit Intents

- Press **Navigate with Google Maps** on the lab screen.
- Or press **Navigate** on a hospital or medicine/pharmacy result.
- Press **Call** on a donor/request/hospital/pharmacy result.

These use `ACTION_VIEW`/Google Navigation and `ACTION_DIAL`. Google Maps falls back to another
maps app or a browser, so no Maps API key is required.

### View Binding and events

All Activities, Fragments, and RecyclerView item rows use generated View Binding classes.
Buttons, text changes, dropdowns, Activity Result callbacks, and LiveData observers demonstrate
event handling.

## 3. UI and dynamic lists

- `activity_lab_components.xml` uses **ConstraintLayout** and LinearLayout.
- Existing list rows use **RelativeLayout** and LinearLayout.
- Donors, requests, hospitals, and medicines use **RecyclerView**, custom **Adapters**, and
  **ViewHolders**.
- Room + LiveData refresh lists after owner/user changes.

## 4. Android components and permissions

Open **Android components lab** from Home.

### BroadcastReceiver

The lab Activity dynamically registers `SystemEventReceiver` for:

- airplane-mode changes,
- battery-saver changes,
- battery percentage,
- low-battery and battery-okay events.

Press **Airplane mode** or **Battery saver**, change the setting, and return. The latest broadcast
appears on screen.

### Content Provider

Press **Choose from device contacts** and select a phone contact. The app receives the contact URI
and queries Android's `ContactsContract` using `ContentResolver`. The picker grants access only to
the chosen record and does not require broad contact permission.

### Runtime permission and foreground service

Press **Request notification permission**, then **Start foreground monitor**. The service creates
an ongoing notification showing battery percentage and elapsed runtime and refreshes every
15 seconds. Press **Stop monitor** to stop it.

Source:

- `system/SystemEventReceiver.java`
- `service/HealthMonitorService.java`
- `LabComponentsActivity.java`

## 5. Networking and Firebase

### JSON API and HTTPS

Press **Load Dhaka health conditions**. Retrofit performs an HTTPS `GET` request to Open-Meteo.
Gson parses temperature, humidity, wind speed, and API time from JSON into `WeatherResponse`.

Source:

- `remote/PublicHealthApi.java`
- `remote/model/WeatherResponse.java`
- `repository/PublicHealthRepository.java`
- `di/NetworkModule.java`

### Authentication, Firestore and Realtime Database

Press **Run Firebase cloud check**. The app:

1. reuses the currently signed-in Firebase Email/Password user;
2. writes an authenticated `lastLabCheck` event to its own Firestore `users/{uid}` document;
3. writes the same event to Realtime Database path `lab_demo_status/{uid}`;
4. displays success only when both writes complete.

Register a fresh account in the app after enabling Firebase Email/Password, or sign in with an
existing Firebase email account, before running this check. Offline demo-owner accounts remain
local and cannot write to Firebase.

Source:

- `repository/FirebaseDemoRepository.java`
- `remote/FirebaseService.java`
- `firestore.rules`
- `database.rules.json`

## 6. Responsive layout and rotation

Rotate the lab screen to landscape. Android switches from
`res/layout/activity_lab_components.xml` to
`res/layout-land/activity_lab_components.xml`, changing the demonstrations from one column to
two columns. API and Firebase result text is saved through `onSaveInstanceState`. Other app layouts
use `dp`, `sp`, weights, ScrollView, and RecyclerView for dynamic scaling.

## Recommended video order

1. General, pharmacy-owner, and hospital-owner feature walkthrough.
2. RecyclerView list and owner update reflected in public search.
3. Open the second Activity and explain explicit Intent + View Binding.
4. Google Maps and dialer implicit Intents.
5. Toggle airplane mode or battery saver and show the broadcast result.
6. Pick one contact through the Content Provider.
7. Grant notification permission; start and stop the foreground service.
8. Run the public JSON API request.
9. Run the Firebase cloud check and show both Firebase Console records.
10. Rotate to landscape and show the responsive two-column layout.
