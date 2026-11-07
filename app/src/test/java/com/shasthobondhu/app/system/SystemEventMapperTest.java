package com.shasthobondhu.app.system;

import android.content.Intent;
import android.os.PowerManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SystemEventMapperTest {

    @Test
    public void mapsAirplaneTransitions() {
        SystemEvent enabled = SystemEventMapper.map(Intent.ACTION_AIRPLANE_MODE_CHANGED, true);
        assertEquals(SystemEvent.Type.AIRPLANE_MODE, enabled.type);
        assertTrue(enabled.active);

        SystemEvent disabled = SystemEventMapper.map(Intent.ACTION_AIRPLANE_MODE_CHANGED, false);
        assertFalse(disabled.active);
    }

    @Test
    public void mapsBatteryAndPowerTransitions() {
        assertTrue(SystemEventMapper.map(Intent.ACTION_BATTERY_LOW, false).active);
        assertFalse(SystemEventMapper.map(Intent.ACTION_BATTERY_OKAY, true).active);
        assertTrue(SystemEventMapper.map(
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED, true).active);
    }

    @Test
    public void ignoresUnknownAction() {
        assertNull(SystemEventMapper.map("com.example.UNKNOWN", true));
    }
}
