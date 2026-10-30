package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import com.shasthobondhu.app.data.local.dao.HospitalDao;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import javax.inject.Inject;
import java.util.List;

public class HospitalRepository {
    private final HospitalDao hospitalDao;

    @Inject
    public HospitalRepository(HospitalDao hospitalDao) {
        this.hospitalDao = hospitalDao;
    }

    public LiveData<List<HospitalEntity>> searchHospitals(String query) {
        return hospitalDao.searchHospitals(query);
    }
}
