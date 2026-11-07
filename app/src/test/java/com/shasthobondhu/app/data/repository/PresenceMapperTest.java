package com.shasthobondhu.app.data.repository;

import com.shasthobondhu.app.data.model.DonorPresence;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PresenceMapperTest {

    @Test
    public void mapsRealtimeValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("available", true);
        values.put("lastSeen", 1234L);
        DonorPresence presence = PresenceMapper.fromMap(values);
        assertTrue(presence.available);
        assertEquals(1234L, presence.lastSeen);
    }

    @Test
    public void malformedValuesFailSoft() {
        Map<String, Object> values = new HashMap<>();
        values.put("available", "yes");
        values.put("lastSeen", "now");
        DonorPresence presence = PresenceMapper.fromMap(values);
        assertFalse(presence.available);
        assertEquals(0L, presence.lastSeen);
        assertNull(PresenceMapper.fromMap(null));
    }
}
