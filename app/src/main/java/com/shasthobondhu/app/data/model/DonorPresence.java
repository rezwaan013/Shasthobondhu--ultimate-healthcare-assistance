package com.shasthobondhu.app.data.model;

/** Ephemeral Firebase connection state; willingness remains on the donor profile. */
public final class DonorPresence {
    public final boolean online;
    public final long lastSeen;

    public DonorPresence(boolean online, long lastSeen) {
        this.online = online;
        this.lastSeen = Math.max(0L, lastSeen);
    }
}
