package com.shasthobondhu.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.shasthobondhu.app.MainActivity;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.data.repository.MonitorPreferences;
import com.shasthobondhu.app.data.repository.RealtimeDatabaseRepository;
import com.shasthobondhu.app.util.constants.AppConstants;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Foreground monitor for open cloud blood requests matching the donor's alert profile. */
@AndroidEntryPoint
public class HealthMonitorService extends Service {

    public static final String ACTION_START =
            "com.shasthobondhu.app.action.START_EMERGENCY_MONITOR";
    public static final String ACTION_STOP =
            "com.shasthobondhu.app.action.STOP_EMERGENCY_MONITOR";
    public static final String EXTRA_BLOOD_GROUP = "monitor_blood_group";
    public static final String EXTRA_DISTRICT = "monitor_district";

    private static final String CHANNEL_ID = "emergency_request_monitor";
    private static final int NOTIFICATION_ID = 2401;

    @Inject
    FirebaseService firebaseService;

    private ListenerRegistration firestoreRequestListener;
    private DatabaseReference realtimeRequestReference;
    private ValueEventListener realtimeRequestListener;
    private String bloodGroup = "";
    private String district = "";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        bloodGroup = intent == null ? MonitorPreferences.getBloodGroup(this)
                : safe(intent.getStringExtra(EXTRA_BLOOD_GROUP));
        district = intent == null ? MonitorPreferences.getDistrict(this)
                : safe(intent.getStringExtra(EXTRA_DISTRICT));
        MonitorPreferences.setRunning(this, true, bloodGroup, district);

        startForeground(NOTIFICATION_ID, buildNotification(-1, 0));
        attachRequestListener();
        return START_STICKY;
    }

    private void attachRequestListener() {
        removeRequestListeners();
        if (firebaseService.getRealtimeDb() != null
                && firebaseService.getAuth().getCurrentUser() != null) {
            attachRealtimeRequestListener();
            return;
        }
        firestoreRequestListener = firebaseService.getDb()
                .collection(AppConstants.COLLECTION_REQUESTS)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        updateNotification(-1, 0);
                        return;
                    }
                    int matching = 0;
                    int urgent = 0;
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        if (!"Open".equalsIgnoreCase(safe(document.getString("status")))) {
                            continue;
                        }
                        String requestBlood = safe(document.getString("bloodGroup"));
                        String requestLocation = safe(document.getString("location"));
                        if (!matches(bloodGroup, requestBlood)
                                || !containsIgnoreCase(requestLocation, district)) {
                            continue;
                        }
                        matching++;
                        String urgency = safe(document.getString("urgency"));
                        if (AppConstants.URGENCY_CRITICAL.equalsIgnoreCase(urgency)
                                || AppConstants.URGENCY_URGENT.equalsIgnoreCase(urgency)) {
                            urgent++;
                        }
                    }
                    updateNotification(matching, urgent);
                });
    }

    private void attachRealtimeRequestListener() {
        realtimeRequestReference = firebaseService.getRealtimeDb()
                .getReference(RealtimeDatabaseRepository.PATH_REQUESTS);
        realtimeRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int matching = 0;
                int urgent = 0;
                for (DataSnapshot request : snapshot.getChildren()) {
                    if (!"Open".equalsIgnoreCase(childString(request, "status"))) continue;
                    if (!matches(bloodGroup, childString(request, "bloodGroup"))
                            || !containsIgnoreCase(childString(request, "location"), district)) {
                        continue;
                    }
                    matching++;
                    String urgency = childString(request, "urgency");
                    if (AppConstants.URGENCY_CRITICAL.equalsIgnoreCase(urgency)
                            || AppConstants.URGENCY_URGENT.equalsIgnoreCase(urgency)) {
                        urgent++;
                    }
                }
                updateNotification(matching, urgent);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updateNotification(-1, 0);
            }
        };
        realtimeRequestReference.addValueEventListener(realtimeRequestListener);
    }

    private String childString(DataSnapshot snapshot, String child) {
        Object value = snapshot.child(child).getValue();
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void removeRequestListeners() {
        if (firestoreRequestListener != null) {
            firestoreRequestListener.remove();
            firestoreRequestListener = null;
        }
        if (realtimeRequestReference != null && realtimeRequestListener != null) {
            realtimeRequestReference.removeEventListener(realtimeRequestListener);
        }
        realtimeRequestReference = null;
        realtimeRequestListener = null;
    }

    private boolean matches(String expected, String actual) {
        return expected.isEmpty() || expected.equalsIgnoreCase(actual);
    }

    private boolean containsIgnoreCase(String value, String filter) {
        return filter.isEmpty() || value.toLowerCase(Locale.ROOT)
                .contains(filter.toLowerCase(Locale.ROOT));
    }

    private void updateNotification(int count, int urgent) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(count, urgent));
        }
    }

    private Notification buildNotification(int count, int urgent) {
        Intent openRequests = new Intent(this, MainActivity.class)
                .putExtra(MainActivity.EXTRA_DESTINATION, R.id.nav_requests)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openRequests,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String body;
        if (count < 0) {
            body = getString(R.string.monitor_notification_connecting);
        } else {
            body = getResources().getQuantityString(R.plurals.monitor_notification_count,
                    count, count, bloodGroup.isEmpty() ? getString(R.string.any_blood_group)
                            : bloodGroup, district.isEmpty()
                            ? getString(R.string.any_district) : district, urgent);
        }
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_logo)
                .setContentTitle(getString(R.string.monitor_notification_title))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                getString(R.string.monitor_channel_name), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.monitor_channel_description));
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        removeRequestListeners();
        MonitorPreferences.setRunning(this, false, bloodGroup, district);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
