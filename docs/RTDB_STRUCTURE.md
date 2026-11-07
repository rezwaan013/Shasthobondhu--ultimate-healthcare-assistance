# Expected Realtime Database Structure

The database starts empty. The Android client never inserts fake hospital, medicine,
notification, donor, request, or user records.

```text
/
├── users/{uid}
│   ├── uid, name, email, phone, role, updatedAt
├── blood_donors/{uid}
│   ├── uid, name, bloodGroup, phone
│   ├── division, district, upazila, location
│   ├── lastDonationDate, available, availableAfterEpoch, updatedAt
├── blood_requests/{pushId}
│   ├── requestId, requesterId, patientName, patientDetails
│   ├── bloodGroup, units, hospital, location, phone, urgency
│   ├── neededDate, neededTime, status, timestamp, updatedAt
│   └── acceptedByUid, acceptedByName (only after acceptance)
├── blood_request_status/{requestId}
│   └── status, updatedBy, timestamp
├── notifications/{uid}/{notificationId}
│   └── title, message, type, requestId, createdAt, read
├── user_status/{uid}
│   └── online, lastSeen
├── hospitals/{hospitalId}
│   └── hospitalId, name, address, phone, services, location, doctors?, updatedAt
└── medicines/{medicineId}
    └── medicineId, name, brand, pharmacy, location, availability
        price, phone, address, lastUpdated, updatedAt
```

## Automatic creation

- A verified Firebase user login/registration writes `users/{uid}`.
- Saving a donor profile writes `blood_donors/{uid}`.
- Posting a request generates a push ID and writes `blood_requests/{pushId}` plus its status.
- A verified signed-in Firebase connection writes `user_status/{uid}` and registers
  `onDisconnect()` to set `online=false` and a server timestamp.

## Trusted-backend-only creation

`notifications`, `hospitals`, and `medicines` must be populated with Firebase Admin SDK,
Cloud Functions, or a deliberately assigned Auth custom claim `admin: true`. The Android app's
PHARMACY/HOSPITAL role is display/routing data and never grants administrative permission.

The Firebase Console uses administrative credentials and can add initial real hospital/medicine
records, but an Admin SDK import script with validation and audit logging is safer for repeatable
production setup.

Example trusted Node.js Admin SDK claim assignment (run only in an administrator-controlled
environment, never in the Android client):

```js
const { getAuth } = require("firebase-admin/auth");
await getAuth().setCustomUserClaims(uid, { admin: true });
```

The administrator must sign in again or force-refresh their ID token before client-side rules see
the new claim. Prefer Admin SDK writes for production imports and notification creation.
