package com.shasthobondhu.app.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.provider.Settings;

/** Dynamic-only receiver for conditions that can disrupt emergency request delivery. */
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
        boolean enabled = false;
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            enabled = intent.getBooleanExtra("state", false);
        } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
            PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            enabled = manager != null && manager.isPowerSaveMode();
        }
        SystemEvent event = SystemEventMapper.map(action, enabled);
        if (event != null) {
            listener.onSystemEvent(event);
        }
    }

    public void publishCurrentWarnings(Context context) {
        if (listener == null) {
            return;
        }
        boolean airplane = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        if (airplane) {
            listener.onSystemEvent(SystemEvent.airplane(true));
            return;
        }
        PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (power != null && power.isPowerSaveMode()) {
            listener.onSystemEvent(SystemEvent.powerSave(true));
            return;
        }
        BatteryManager battery = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int level = battery == null ? -1
                : battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if (level >= 0 && level <= 15) {
            listener.onSystemEvent(SystemEvent.batteryLow(true));
            return;
        }
        // Clear a stale warning when the app returns after the condition ended off-screen.
        listener.onSystemEvent(SystemEvent.airplane(false));
    }

    public static IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        return filter;
    }

    public interface Listener {
        void onSystemEvent(SystemEvent event);
    }
}
