package com.shasthobondhu.app.data.repository;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DonorAvailabilityPolicyTest {
    @Test
    public void badgeRequiresWillingAndOnline() {
        assertTrue(DonorAvailabilityPolicy.isAvailableNow(true, true));
        assertFalse(DonorAvailabilityPolicy.isAvailableNow(true, false));
        assertFalse(DonorAvailabilityPolicy.isAvailableNow(false, true));
        assertFalse(DonorAvailabilityPolicy.isAvailableNow(false, false));
    }
}
