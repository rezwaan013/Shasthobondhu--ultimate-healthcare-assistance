package com.shasthobondhu.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.shasthobondhu.app.LabComponentsActivity;
import com.shasthobondhu.app.R;

/** Foreground service with a notification that refreshes its runtime and battery reading. */
public class HealthMonitorService extends Service {

    public static final String ACTION_START =
            "com.shasthobondhu.app.action.START_HEALTH_MONITOR";
    public static final String ACTION_STOP =
            "com.shasthobondhu.app.action.STOP_HEALTH_MONITOR";

    private static final String CHANNEL_ID = "health_monitor_channel";
    private static final int NOTIFICATION_ID = 2207;
    private static final long UPDATE_INTERVAL_MS = 15_000L;
    private static final String PREFS = "health_monitor_state";
    private static final String KEY_RUNNING = "running";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAt;

    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, buildNotification());
            }
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (startedAt == 0L) {
            startedAt = SystemClock.elapsedRealtime();
        }
        setRunning(true);
        startForeground(NOTIFICATION_ID, buildNotification());
        handler.removeCallbacks(updateTask);
        handler.postDelayed(updateTask, UPDATE_INTERVAL_MS);
        return START_STICKY;
    }

    private Notification buildNotification() {
        long elapsedSeconds = startedAt == 0L ? 0L
                : (SystemClock.elapsedRealtime() - startedAt) / 1000L;
        BatteryManager batteryManager =
                (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        int battery = batteryManager == null ? -1
                : batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        Intent openIntent = new Intent(this, LabComponentsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int elapsedQuantity = elapsedSeconds > Integer.MAX_VALUE
                ? Integer.MAX_VALUE : (int) elapsedSeconds;
        String detail = getResources().getQuantityString(
                R.plurals.monitor_notification_detail,
                elapsedQuantity,
                battery < 0 ? "--" : String.valueOf(battery),
                elapsedSeconds);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_logo)
                .setContentTitle(getString(R.string.monitor_notification_title))
                .setContentText(detail)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(detail))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.monitor_channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.monitor_channel_description));
        manager.createNotificationChannel(channel);
    }

    private void setRunning(boolean running) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_RUNNING, running)
                .apply();
    }

    public static boolean isRunning(Context context) {
        return context.getSharedPreferences(PREFS, MODE_PRIVATE)
                .getBoolean(KEY_RUNNING, false);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateTask);
        setRunning(false);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
