package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.data.repository.DonorRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Combines the blood-group and area filters and re-queries Room whenever
 * either of them changes.
 */
@HiltViewModel
public class DonorViewModel extends ViewModel {

    private final DonorRepository repository;

    private final MutableLiveData<String> areaFilter = new MutableLiveData<>("");
    private final MutableLiveData<String> bloodGroupFilter = new MutableLiveData<>("");
    private final MediatorLiveData<List<DonorEntity>> donors = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);

    private LiveData<List<DonorEntity>> currentSource;

    @Inject
    public DonorViewModel(DonorRepository repository) {
        this.repository = repository;
        reload();
        donors.addSource(areaFilter, value -> reload());
        donors.addSource(bloodGroupFilter, value -> reload());
    }

    private void reload() {
        String bloodGroup = bloodGroupFilter.getValue() == null
                ? "" : bloodGroupFilter.getValue().trim();
        String area = areaFilter.getValue() == null
                ? "" : areaFilter.getValue().trim();

        LiveData<List<DonorEntity>> source = repository.getDonors(bloodGroup, area);
        if (currentSource != null) {
            donors.removeSource(currentSource);
        }
        currentSource = source;
        donors.addSource(source, donors::setValue);
    }

    public void setAreaFilter(String area) {
        areaFilter.setValue(area == null ? "" : area);
    }

    public void setBloodGroupFilter(String bloodGroup) {
        bloodGroupFilter.setValue(bloodGroup == null ? "" : bloodGroup);
    }

    public LiveData<List<DonorEntity>> getDonors() {
        return donors;
    }

    public LiveData<DonorEntity> getMyProfile(String ownerUid) {
        return repository.getMyProfile(ownerUid);
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public void registerDonor(DonorEntity donor) {
        repository.saveDonorAndNotify(donor, () -> saved.setValue(true));
    }
}
