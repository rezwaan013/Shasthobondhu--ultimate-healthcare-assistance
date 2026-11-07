package com.shasthobondhu.app.data.repository;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RequestStatusPolicyTest {
    @Test
    public void ownerTransitionsAreRestricted() {
        assertTrue(RequestStatusPolicy.canOwnerTransition("Open", "Cancelled"));
        assertTrue(RequestStatusPolicy.canOwnerTransition("Accepted", "Fulfilled"));
        assertTrue(RequestStatusPolicy.canOwnerTransition("Accepted", "Cancelled"));
        assertFalse(RequestStatusPolicy.canOwnerTransition("Open", "Fulfilled"));
        assertFalse(RequestStatusPolicy.canOwnerTransition("Fulfilled", "Open"));
    }

    @Test
    public void donorCannotAcceptOwnOrClosedRequest() {
        assertTrue(RequestStatusPolicy.canDonorAccept("Open", "owner", "donor", "Accepted"));
        assertFalse(RequestStatusPolicy.canDonorAccept("Open", "owner", "owner", "Accepted"));
        assertFalse(RequestStatusPolicy.canDonorAccept(
                "Accepted", "owner", "donor", "Accepted"));
    }
}
