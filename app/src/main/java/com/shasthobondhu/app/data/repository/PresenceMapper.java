package com.shasthobondhu.app.data.repository;

import com.shasthobondhu.app.data.model.DonorPresence;

import java.util.Map;

/** Pure, defensive mapper for loosely typed RTDB presence values. */
public final class PresenceMapper {

    private PresenceMapper() {
    }

    public static DonorPresence fromMap(Map<String, Object> values) {
        if (values == null) {
            return null;
        }
        Object onlineValue = values.get("online");
        Object lastSeenValue = values.get("lastSeen");
        boolean online = onlineValue instanceof Boolean && (Boolean) onlineValue;
        long lastSeen = lastSeenValue instanceof Number
                ? ((Number) lastSeenValue).longValue() : 0L;
        return new DonorPresence(online, lastSeen);
    }
}
