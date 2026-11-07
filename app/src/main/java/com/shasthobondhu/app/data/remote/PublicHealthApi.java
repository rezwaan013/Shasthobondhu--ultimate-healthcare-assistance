package com.shasthobondhu.app.data.remote;

import com.shasthobondhu.app.data.remote.model.BangladeshHealthStatsResponse;
import com.shasthobondhu.app.data.remote.model.OpenFdaLabelResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PublicHealthApi {

    @GET("drug/label.json")
    Call<OpenFdaLabelResponse> getDrugLabel(
            @Query("search") String search,
            @Query("limit") int limit);

    @GET("https://disease.sh/v3/covid-19/countries/bangladesh")
    Call<BangladeshHealthStatsResponse> getBangladeshHealthStats();
}
