package com.shasthobondhu.app.data.repository;

import androidx.annotation.NonNull;

import com.shasthobondhu.app.data.remote.PublicHealthApi;
import com.shasthobondhu.app.data.remote.model.WeatherResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class PublicHealthRepository {

    private static final double DHAKA_LATITUDE = 23.8103;
    private static final double DHAKA_LONGITUDE = 90.4125;
    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,wind_speed_10m";

    private final PublicHealthApi api;

    @Inject
    public PublicHealthRepository(PublicHealthApi api) {
        this.api = api;
    }

    public void loadDhakaConditions(ResultCallback callback) {
        api.getCurrentConditions(DHAKA_LATITUDE, DHAKA_LONGITUDE,
                        CURRENT_FIELDS, "Asia/Dhaka")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call,
                                           @NonNull Response<WeatherResponse> response) {
                        WeatherResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.getCurrent() != null) {
                            callback.onSuccess(body.getCurrent());
                        } else {
                            callback.onError("HTTP " + response.code()
                                    + ": the weather service returned no current data.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call,
                                          @NonNull Throwable error) {
                        callback.onError(error.getMessage() == null
                                ? "Network request failed." : error.getMessage());
                    }
                });
    }

    public interface ResultCallback {
        void onSuccess(WeatherResponse.CurrentConditions conditions);

        void onError(String message);
    }
}
