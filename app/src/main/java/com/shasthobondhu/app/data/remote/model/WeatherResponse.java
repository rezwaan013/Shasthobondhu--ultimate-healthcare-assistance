package com.shasthobondhu.app.data.remote.model;

import com.google.gson.annotations.SerializedName;

/** Minimal Open-Meteo response model, parsed from JSON by Gson. */
public class WeatherResponse {

    @SerializedName("current")
    private CurrentConditions current;

    public CurrentConditions getCurrent() {
        return current;
    }

    public static class CurrentConditions {
        @SerializedName("time")
        private String time;

        @SerializedName("temperature_2m")
        private Double temperature;

        @SerializedName("relative_humidity_2m")
        private Integer humidity;

        @SerializedName("wind_speed_10m")
        private Double windSpeed;

        public String getTime() {
            return time;
        }

        public Double getTemperature() {
            return temperature;
        }

        public Integer getHumidity() {
            return humidity;
        }

        public Double getWindSpeed() {
            return windSpeed;
        }
    }
}
