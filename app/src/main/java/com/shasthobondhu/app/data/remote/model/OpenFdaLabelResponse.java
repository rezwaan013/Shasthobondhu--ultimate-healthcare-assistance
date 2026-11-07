package com.shasthobondhu.app.data.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenFdaLabelResponse {
    @SerializedName("results") public List<LabelResult> results;

    public static class LabelResult {
        @SerializedName("indications_and_usage") public List<String> indicationsAndUsage;
        @SerializedName("warnings") public List<String> warnings;
        @SerializedName("openfda") public OpenFda openFda;
    }

    public static class OpenFda {
        @SerializedName("brand_name") public List<String> brandName;
        @SerializedName("generic_name") public List<String> genericName;
        @SerializedName("manufacturer_name") public List<String> manufacturerName;
    }
}
