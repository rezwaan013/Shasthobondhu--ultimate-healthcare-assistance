package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SwitchMap;
import androidx.lifecycle.ViewModel;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.data.repository.DonorRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DonorViewModel extends ViewModel {
    private final DonorRepository repository;
    private final MutableLiveData<String> queryArea = new MutableLiveData<>("");
    private final MutableLiveData<String> queryBloodGroup = new MutableLiveData<>("");

    @Inject
    public DonorViewModel(DonorRepository repository) {
        this.repository = repository;
    }

    public void setAreaFilter(String area) {
        queryArea.setValue(area);
    }

    public void setBloodGroupFilter(String bloodGroup) {
        queryBloodGroup.setValue(bloodGroup);
    }

    public LiveData<List<DonorEntity>> getDonorsByArea() {
        return androidx.lifecycle.Transformations.switchMap(queryArea, area -> {
            if (area == null || area.isEmpty()) {
                return repository.getDonorsByArea(""); // Or return all
            }
            return repository.getDonorsByArea(area);
        });
    }

    public LiveData<List<DonorEntity>> getDonorsByBloodAndArea() {
        return androidx.lifecycle.Transformations.switchMap(queryArea, area -> {
            String bg = queryBloodGroup.getValue();
            return repository.getDonors(bg != null ? bg : "Any", "Any", area);
        });
    }
}
