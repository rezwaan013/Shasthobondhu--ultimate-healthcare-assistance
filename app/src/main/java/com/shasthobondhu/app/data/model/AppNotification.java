package com.shasthobondhu.app.data.model;

/** Private per-user notification loaded from Realtime Database. */
public final class AppNotification {
    public final String id;
    public final String title;
    public final String message;
    public final String type;
    public final String requestId;
    public final long createdAt;
    public final boolean read;

    public AppNotification(String id, String title, String message, String type,
                           String requestId, long createdAt, boolean read) {
        this.id = safe(id);
        this.title = safe(title);
        this.message = safe(message);
        this.type = safe(type);
        this.requestId = safe(requestId);
        this.createdAt = Math.max(0L, createdAt);
        this.read = read;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
