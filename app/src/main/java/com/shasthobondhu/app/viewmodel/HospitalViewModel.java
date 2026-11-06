package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.data.repository.HospitalRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HospitalViewModel extends ViewModel {

    private final HospitalRepository repository;

    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<HospitalEntity>> results = new MediatorLiveData<>();
    private LiveData<List<HospitalEntity>> currentSource;

    @Inject
    public HospitalViewModel(HospitalRepository repository) {
        this.repository = repository;
        reload();
        results.addSource(query, value -> reload());
    }

    private void reload() {
        LiveData<List<HospitalEntity>> source = repository.searchHospitals(query.getValue());
        if (currentSource != null) {
            results.removeSource(currentSource);
        }
        currentSource = source;
        results.addSource(source, results::setValue);
    }

    public void setQuery(String value) {
        query.setValue(value == null ? "" : value.trim());
    }

    public LiveData<List<HospitalEntity>> getHospitals() {
        return results;
    }
}
