package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.model.HealthStats;
import com.shasthobondhu.app.data.model.Resource;
import com.shasthobondhu.app.data.repository.HealthInfoRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HealthInfoViewModel extends ViewModel {

    private final HealthInfoRepository repository;
    private final MediatorLiveData<Resource<HealthStats>> stats = new MediatorLiveData<>();
    private LiveData<Resource<HealthStats>> source;

    @Inject
    public HealthInfoViewModel(HealthInfoRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<HealthStats>> getStats() {
        return stats;
    }

    public void loadStats() {
        if (source != null) {
            stats.removeSource(source);
        }
        source = repository.loadBangladeshHealthStats();
        stats.addSource(source, stats::setValue);
    }
}
