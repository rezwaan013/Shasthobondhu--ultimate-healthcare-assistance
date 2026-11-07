package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shasthobondhu.app.data.model.DrugLabel;
import com.shasthobondhu.app.data.model.HealthStats;
import com.shasthobondhu.app.data.model.Resource;
import com.shasthobondhu.app.data.remote.PublicHealthApi;
import com.shasthobondhu.app.data.remote.model.BangladeshHealthStatsResponse;
import com.shasthobondhu.app.data.remote.model.OpenFdaLabelResponse;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class HealthInfoRepository {

    private final PublicHealthApi api;

    @Inject
    public HealthInfoRepository(PublicHealthApi api) {
        this.api = api;
    }

    public LiveData<Resource<DrugLabel>> loadDrugLabel(String medicineName) {
        MutableLiveData<Resource<DrugLabel>> result =
                new MutableLiveData<>(Resource.loading());
        String normalized = DrugLabelMapper.normalizeSearchName(medicineName);
        if (normalized.isEmpty()) {
            result.setValue(Resource.empty());
            return result;
        }
        String search = "openfda.generic_name:\"" + normalized.replace("\"", "") + "\"";
        api.getDrugLabel(search, 1).enqueue(new Callback<OpenFdaLabelResponse>() {
            @Override
            public void onResponse(Call<OpenFdaLabelResponse> call,
                                   Response<OpenFdaLabelResponse> response) {
                if (response.code() == 404) {
                    result.setValue(Resource.empty());
                    return;
                }
                if (!response.isSuccessful()) {
                    result.setValue(Resource.error(Resource.ErrorType.SERVER));
                    return;
                }
                DrugLabel label = DrugLabelMapper.map(response.body(), medicineName);
                result.setValue(label == null ? Resource.empty() : Resource.success(label));
            }

            @Override
            public void onFailure(Call<OpenFdaLabelResponse> call, Throwable error) {
                result.setValue(Resource.error(error instanceof IOException
                        ? Resource.ErrorType.NETWORK : Resource.ErrorType.MALFORMED));
            }
        });
        return result;
    }

    public LiveData<Resource<HealthStats>> loadBangladeshHealthStats() {
        MutableLiveData<Resource<HealthStats>> result =
                new MutableLiveData<>(Resource.loading());
        api.getBangladeshHealthStats().enqueue(new Callback<BangladeshHealthStatsResponse>() {
            @Override
            public void onResponse(Call<BangladeshHealthStatsResponse> call,
                                   Response<BangladeshHealthStatsResponse> response) {
                if (!response.isSuccessful()) {
                    result.setValue(response.code() == 404
                            ? Resource.empty() : Resource.error(Resource.ErrorType.SERVER));
                    return;
                }
                HealthStats stats = HealthStatsMapper.map(response.body());
                result.setValue(stats == null ? Resource.empty() : Resource.success(stats));
            }

            @Override
            public void onFailure(Call<BangladeshHealthStatsResponse> call, Throwable error) {
                result.setValue(Resource.error(error instanceof IOException
                        ? Resource.ErrorType.NETWORK : Resource.ErrorType.MALFORMED));
            }
        });
        return result;
    }
}
