package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.data.repository.MedicineRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MedicineViewModel extends ViewModel {

    private final MedicineRepository repository;

    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> area = new MutableLiveData<>("");
    private final MediatorLiveData<List<MedicineEntity>> results = new MediatorLiveData<>();
    private LiveData<List<MedicineEntity>> currentSource;

    @Inject
    public MedicineViewModel(MedicineRepository repository) {
        this.repository = repository;
        reload();
        results.addSource(query, value -> reload());
        results.addSource(area, value -> reload());
    }

    private void reload() {
        LiveData<List<MedicineEntity>> source = repository.searchMedicines(
                query.getValue(), area.getValue());
        if (currentSource != null) {
            results.removeSource(currentSource);
        }
        currentSource = source;
        results.addSource(source, results::setValue);
    }

    public void setQuery(String value) {
        query.setValue(value == null ? "" : value.trim());
    }

    public void setArea(String value) {
        area.setValue(value == null ? "" : value.trim());
    }

    public LiveData<List<MedicineEntity>> getMedicines() {
        return results;
    }
}
