package com.shasthobondhu.app.data.repository;

import com.shasthobondhu.app.data.model.HealthStats;
import com.shasthobondhu.app.data.remote.model.BangladeshHealthStatsResponse;

/** Converts nullable API fields to a stable app model. */
public final class HealthStatsMapper {

    private HealthStatsMapper() {
    }

    public static HealthStats map(BangladeshHealthStatsResponse response) {
        if (response == null) {
            return null;
        }
        String country = response.country == null ? "" : response.country.trim();
        if (country.isEmpty()) {
            return null;
        }
        return new HealthStats(country,
                value(response.updated), value(response.cases), value(response.deaths),
                value(response.tests), value(response.population));
    }

    private static long value(Long value) {
        return value == null ? 0L : Math.max(0L, value);
    }
}
