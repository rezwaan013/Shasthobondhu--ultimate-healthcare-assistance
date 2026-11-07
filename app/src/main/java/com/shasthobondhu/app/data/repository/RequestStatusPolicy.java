package com.shasthobondhu.app.data.repository;

/** Shared client-side transition policy; RTDB security rules independently enforce the same set. */
public final class RequestStatusPolicy {
    private RequestStatusPolicy() {
    }

    public static boolean canOwnerTransition(String oldStatus, String newStatus) {
        return ("Open".equals(oldStatus) && "Cancelled".equals(newStatus))
                || ("Accepted".equals(oldStatus)
                && ("Fulfilled".equals(newStatus) || "Cancelled".equals(newStatus)));
    }

    public static boolean canDonorAccept(String oldStatus, String requesterUid,
                                         String actorUid, String newStatus) {
        return "Open".equals(oldStatus) && "Accepted".equals(newStatus)
                && actorUid != null && !actorUid.equals(requesterUid);
    }
}
