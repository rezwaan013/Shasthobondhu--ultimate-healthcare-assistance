package com.shasthobondhu.app.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/** Dynamic receiver used by the lab screen to display live Android system broadcasts. */
public class SystemEventReceiver extends BroadcastReceiver {

    private final Listener listener;

    public SystemEventReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || listener == null) {
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            listener.onSystemEvent(action, intent.getBooleanExtra("state", false), -1);
            return;
        }
        if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
            PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            listener.onSystemEvent(action, manager != null && manager.isPowerSaveMode(), -1);
            return;
        }
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            int level = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", 100);
            int percentage = level < 0 || scale <= 0 ? -1 : Math.round(level * 100f / scale);
            listener.onSystemEvent(action, false, percentage);
            return;
        }
        listener.onSystemEvent(action, Intent.ACTION_BATTERY_OKAY.equals(action), -1);
    }

    public interface Listener {
        void onSystemEvent(String action, boolean enabled, int batteryPercentage);
    }
}
