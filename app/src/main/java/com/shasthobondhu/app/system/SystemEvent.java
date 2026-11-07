package com.shasthobondhu.app.system;

import androidx.annotation.StringRes;

import com.shasthobondhu.app.R;

import java.util.Objects;

/** Immutable, UI-ready state for a relevant Android system event. */
public final class SystemEvent {

    public enum Type {
        AIRPLANE_MODE,
        BATTERY_LOW,
        POWER_SAVE
    }

    public final Type type;
    public final boolean active;
    @StringRes public final int messageRes;

    public SystemEvent(Type type, boolean active, @StringRes int messageRes) {
        this.type = type;
        this.active = active;
        this.messageRes = messageRes;
    }

    public static SystemEvent airplane(boolean active) {
        return new SystemEvent(Type.AIRPLANE_MODE, active,
                active ? R.string.system_airplane_on : R.string.system_airplane_off);
    }

    public static SystemEvent batteryLow(boolean active) {
        return new SystemEvent(Type.BATTERY_LOW, active,
                active ? R.string.system_battery_low : R.string.system_battery_okay);
    }

    public static SystemEvent powerSave(boolean active) {
        return new SystemEvent(Type.POWER_SAVE, active,
                active ? R.string.system_power_save_on : R.string.system_power_save_off);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SystemEvent)) return false;
        SystemEvent event = (SystemEvent) other;
        return active == event.active && type == event.type && messageRes == event.messageRes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, active, messageRes);
    }
}
