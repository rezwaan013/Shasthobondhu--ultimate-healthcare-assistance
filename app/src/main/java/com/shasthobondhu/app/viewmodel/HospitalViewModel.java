package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.data.repository.HospitalRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HospitalViewModel extends ViewModel {
    private final HospitalRepository repository;

    @Inject
    public HospitalViewModel(HospitalRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<HospitalEntity>> searchHospitals(String query) {
        return repository.searchHospitals(query);
    }
}
