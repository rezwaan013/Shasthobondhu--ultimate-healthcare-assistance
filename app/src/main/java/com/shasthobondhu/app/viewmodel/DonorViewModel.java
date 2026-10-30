package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.data.repository.DonorRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DonorViewModel extends ViewModel {
    private final DonorRepository repository;
    private final MutableLiveData<String> queryBloodGroup = new MutableLiveData<>("");
    private final MutableLiveData<String> queryDivision = new MutableLiveData<>("");
    private final MutableLiveData<String> queryDistrict = new MutableLiveData<>("");

    @Inject
    public DonorViewModel(DonorRepository repository) {
        this.repository = repository;
    }

    public void setFilters(String bloodGroup, String division, String district) {
        queryBloodGroup.setValue(bloodGroup);
        queryDivision.setValue(division);
        queryDistrict.setValue(district);
    }

    public LiveData<List<DonorEntity>> getDonors() {
        // Simplified for mock; in real app, use SwitchMap
        return repository.getDonors("A+", "Dhaka", "Dhaka");
    }
}
