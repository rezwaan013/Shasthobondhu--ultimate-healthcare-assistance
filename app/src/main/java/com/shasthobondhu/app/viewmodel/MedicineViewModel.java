package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.data.repository.MedicineRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MedicineViewModel extends ViewModel {
    private final MedicineRepository repository;

    @Inject
    public MedicineViewModel(MedicineRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<MedicineEntity>> searchMedicines(String query) {
        return repository.searchMedicines(query);
    }
}
