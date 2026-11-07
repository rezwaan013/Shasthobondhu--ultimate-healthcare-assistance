package com.shasthobondhu.app.data.model;

/** App-facing Bangladesh public-health statistics with non-null numeric defaults. */
public final class HealthStats {
    public final String country;
    public final long updatedAt;
    public final long cases;
    public final long deaths;
    public final long tests;
    public final long population;

    public HealthStats(String country, long updatedAt, long cases, long deaths,
                       long tests, long population) {
        this.country = country;
        this.updatedAt = updatedAt;
        this.cases = cases;
        this.deaths = deaths;
        this.tests = tests;
        this.population = population;
    }
}
