package com.shasthobondhu.app.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class BangladeshHealthStatsResponse {
    @SerializedName("country") public String country;
    @SerializedName("updated") public Long updated;
    @SerializedName("cases") public Long cases;
    @SerializedName("deaths") public Long deaths;
    @SerializedName("tests") public Long tests;
    @SerializedName("population") public Long population;
    @SerializedName("casesPerOneMillion") public Double casesPerMillion;
    @SerializedName("countryInfo") public CountryInfo countryInfo;

    public static class CountryInfo {
        @SerializedName("iso2") public String iso2;
        @SerializedName("iso3") public String iso3;
        @SerializedName("flag") public String flagUrl;
    }
}
