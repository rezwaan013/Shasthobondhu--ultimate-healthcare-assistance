package com.shasthobondhu.app.data.repository;

import com.shasthobondhu.app.data.model.HealthStats;
import com.shasthobondhu.app.data.remote.model.BangladeshHealthStatsResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HealthStatsMapperTest {

    @Test
    public void mapsMissingNumbersToSafeDefaults() {
        BangladeshHealthStatsResponse response = new BangladeshHealthStatsResponse();
        response.country = "Bangladesh";
        response.cases = 100L;
        response.deaths = null;
        response.tests = -4L;

        HealthStats stats = HealthStatsMapper.map(response);
        assertEquals("Bangladesh", stats.country);
        assertEquals(100L, stats.cases);
        assertEquals(0L, stats.deaths);
        assertEquals(0L, stats.tests);
    }

    @Test
    public void missingCountryIsMalformed() {
        assertNull(HealthStatsMapper.map(new BangladeshHealthStatsResponse()));
    }
}
