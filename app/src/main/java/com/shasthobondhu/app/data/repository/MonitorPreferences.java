package com.shasthobondhu.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

/** Small persisted state used to restore the foreground monitor after process recreation. */
public final class MonitorPreferences {

    private static final String PREFS = "emergency_monitor";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_BLOOD_GROUP = "blood_group";
    private static final String KEY_DISTRICT = "district";

    private MonitorPreferences() {
    }

    public static void setRunning(Context context, boolean running,
                                  String bloodGroup, String district) {
        preferences(context).edit()
                .putBoolean(KEY_RUNNING, running)
                .putString(KEY_BLOOD_GROUP, safe(bloodGroup))
                .putString(KEY_DISTRICT, safe(district))
                .apply();
    }

    public static boolean isRunning(Context context) {
        return preferences(context).getBoolean(KEY_RUNNING, false);
    }

    public static String getBloodGroup(Context context) {
        return preferences(context).getString(KEY_BLOOD_GROUP, "");
    }

    public static String getDistrict(Context context) {
        return preferences(context).getString(KEY_DISTRICT, "");
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
