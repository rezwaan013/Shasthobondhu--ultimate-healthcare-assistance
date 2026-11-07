package com.shasthobondhu.app.data.model;

/** Ephemeral Realtime Database state; donor profile data remains in Firestore/Room. */
public final class DonorPresence {
    public final boolean available;
    public final long lastSeen;

    public DonorPresence(boolean available, long lastSeen) {
        this.available = available;
        this.lastSeen = Math.max(0L, lastSeen);
    }
}
