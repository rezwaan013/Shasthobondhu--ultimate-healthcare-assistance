# Realtime Database Integration Plan

## Existing architecture discovered

- Firebase Authentication is the identity source. Email verification is required for Firebase
  accounts; local demo accounts remain Room-only and cannot access protected cloud data.
- Firestore currently mirrors authenticated users, donor profiles, and blood requests. Room is the
  offline/UI cache and must remain operational.
- RTDB currently contains only a lifecycle-aware `/presence` listener. It stores `available`, which
  incorrectly combines donor willingness with connection status.
- Hospitals, pharmacies, and medicines are currently Room-backed. Owner dashboards are app roles,
  not trusted administrator identities.
- There is no notification list screen or RTDB notification repository.

## Compatibility decisions

1. Firestore and Room remain in place. RTDB is added as a real-time mirror/fallback rather than a
   destructive migration.
2. Existing Java entities and UI are reused. Only the notification record needs a new app model.
3. `/blood_donors/{uid}/available` stores donor willingness. `/user_status/{uid}/online` stores
   actual Firebase connection state from `.info/connected`; the badge requires both values.
4. Donor phone numbers and emergency-request phone numbers are readable only by authenticated
   users, never publicly.
5. `/hospitals` and `/medicines` are readable by authenticated users but writable only when the
   Firebase Auth token has the server-issued custom claim `admin: true`. Existing PHARMACY and
   HOSPITAL app roles do not grant admin access.
6. `/notifications/{uid}` is private. Clients may only change `read` from false to true; creation
   requires `admin: true` and therefore a trusted backend/Admin SDK.
7. Request creation uses RTDB push IDs. Existing Firestore IDs use the same generated ID when RTDB
   is configured.

## Rule-impact warning

Deploying the final rules changes the old `/presence` path to `/user_status` and denies the old
path. Install the updated app before or with the rule deployment. Existing `/presence` records can
be deleted later; the app will not read them. Root access stays denied by default.

Because the checked-in `google-services.json` currently lacks `firebase_url`, the app will use the
explicit non-secret project endpoint supplied for this repository:

`https://shasthobondhu-d5bdf-default-rtdb.asia-southeast1.firebasedatabase.app`

A freshly downloaded Firebase configuration file is still preferred and documented as manual
follow-up.

## Automatic first-write paths

- Registration: `/users/{uid}`
- Donor save: `/blood_donors/{uid}`
- Emergency request: `/blood_requests/{pushId}` and `/blood_request_status/{pushId}`
- Authenticated app connection: `/user_status/{uid}`

`/notifications`, `/hospitals`, and `/medicines` stay absent until a trusted backend or Firebase
Console/Admin SDK writes valid records. No fake production data will be inserted.

## Validation plan

- Unit tests for mapping/filtering/status-transition logic.
- Clean unit-test, assemble, and lint runs.
- Firebase Emulator/CLI syntax validation where the installed tooling permits it.
- No production rule deployment from the coding session. Final deployment command:
  `firebase.cmd deploy --only database`.
