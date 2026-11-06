package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;

import com.shasthobondhu.app.data.local.dao.HospitalDao;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.util.AppExecutors;

import java.util.List;

import javax.inject.Inject;

public class HospitalRepository {
    private final HospitalDao hospitalDao;

    @Inject
    public HospitalRepository(HospitalDao hospitalDao) {
        this.hospitalDao = hospitalDao;
    }

    public LiveData<List<HospitalEntity>> searchHospitals(String query) {
        return hospitalDao.searchHospitals(query == null ? "" : query);
    }

    public LiveData<HospitalEntity> getByOwner(String ownerUid) {
        return hospitalDao.getByOwner(ownerUid == null ? "" : ownerUid);
    }

    public void createHospital(HospitalEntity hospital) {
        AppExecutors.io(() -> hospitalDao.insert(hospital));
    }

    public void createHospitalAndNotify(HospitalEntity hospital, Runnable onSaved) {
        AppExecutors.io(() -> {
            hospitalDao.insert(hospital);
            AppExecutors.main(onSaved);
        });
    }

    public void updateHospital(HospitalEntity hospital) {
        AppExecutors.io(() -> hospitalDao.update(hospital));
    }

    public void updateHospitalAndNotify(HospitalEntity hospital, Runnable onSaved) {
        AppExecutors.io(() -> {
            hospitalDao.update(hospital);
            AppExecutors.main(onSaved);
        });
    }
}
