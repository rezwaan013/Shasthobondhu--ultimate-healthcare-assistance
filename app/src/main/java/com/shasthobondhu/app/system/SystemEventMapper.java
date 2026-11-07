package com.shasthobondhu.app.system;

import android.content.Intent;
import android.os.PowerManager;

/** Pure action-to-state mapping kept separate for unit testing. */
public final class SystemEventMapper {

    private SystemEventMapper() {
    }

    public static SystemEvent map(String action, boolean enabled) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            return SystemEvent.airplane(enabled);
        }
        if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            return SystemEvent.batteryLow(true);
        }
        if (Intent.ACTION_BATTERY_OKAY.equals(action)) {
            return SystemEvent.batteryLow(false);
        }
        if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
            return SystemEvent.powerSave(enabled);
        }
        return null;
    }
}
