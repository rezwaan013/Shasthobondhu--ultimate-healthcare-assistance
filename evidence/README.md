# Emulator Screenshot Evidence

These screenshots were captured from a Pixel 5 Android 11 emulator using the verified debug APK.
Only local demo/test data is shown.

| Screenshot | Feature evidence |
|---|---|
| `01-login-green-logo.png` | Login screen and green branding |
| `02-registration-email-verification.png` | Account registration flow |
| `03-forgot-password.png` | Firebase password-reset screen |
| `04-home-dashboard.png` | General-user Home dashboard |
| `05-dark-mode.png` | Persistent dark theme |
| `06-donor-registration.png` | Donor profile form (post-crash fix) |
| `07-donor-search-and-list.png` | RecyclerView donor search/results |
| `08-donor-detail-explicit-activity.png` | Explicit-intent donor detail Activity |
| `09-blood-requests-list.png` | Emergency request RecyclerView |
| `10-create-emergency-request.png` | Blood-request creation form |
| `11-hospitals-search-list.png` | Hospital search/results |
| `12-medicine-availability.png` | Medicine and pharmacy availability |
| `13-openfda-api-response.png` | Typed openFDA API response |
| `14-home-emergency-tools.png` | Emergency Hub and notification entry points |
| `15-private-notifications.png` | Private notification screen/empty state |
| `16-emergency-hub-health-api.png` | Emergency Hub portrait screen |
| `17-health-api-and-presence.png` | disease.sh API result and presence controls |
| `18-emergency-hub-landscape.png` | Responsive two-column landscape layout |
| `19-system-broadcast-low-battery.png` | Low-battery system broadcast warning |
| `20-contact-runtime-permission.png` | Android Contacts runtime permission |
| `21-contact-permission-denied-retry.png` | Permission denied/retry handling |
| `22-foreground-service-notification.png` | Ongoing Emergency Request Monitor notification |
| `23-pharmacy-owner-dashboard.png` | Pharmacy owner dashboard |

Firebase Auth/RTDB rule authorization is additionally verified by
`scripts/rtdb-rules-smoke.ps1`. Production Firebase Console screenshots are intentionally not
stored here because they can expose account/project administration details.
