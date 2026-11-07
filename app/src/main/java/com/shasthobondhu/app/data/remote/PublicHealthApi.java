package com.shasthobondhu.app.data.remote;

import com.shasthobondhu.app.data.remote.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Public JSON API used in the lab demonstration. */
public interface PublicHealthApi {

    @GET("v1/forecast")
    Call<WeatherResponse> getCurrentConditions(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String currentFields,
            @Query("timezone") String timezone
    );
}
