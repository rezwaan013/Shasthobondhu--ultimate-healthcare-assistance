package com.shasthobondhu.app.data.repository;

/** Keeps donor willingness independent from Firebase connection presence. */
public final class DonorAvailabilityPolicy {
    private DonorAvailabilityPolicy() {
    }

    public static boolean isAvailableNow(boolean willing, boolean online) {
        return willing && online;
    }
}
