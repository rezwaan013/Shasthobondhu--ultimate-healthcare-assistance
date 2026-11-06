package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;

import com.shasthobondhu.app.data.local.dao.PharmacyDao;
import com.shasthobondhu.app.data.local.entities.PharmacyEntity;
import com.shasthobondhu.app.util.AppExecutors;

import javax.inject.Inject;

public class PharmacyRepository {
    private final PharmacyDao pharmacyDao;

    @Inject
    public PharmacyRepository(PharmacyDao pharmacyDao) {
        this.pharmacyDao = pharmacyDao;
    }

    public LiveData<PharmacyEntity> getByOwner(String ownerUid) {
        return pharmacyDao.getByOwner(ownerUid == null ? "" : ownerUid);
    }

    public void createPharmacy(PharmacyEntity pharmacy) {
        AppExecutors.io(() -> pharmacyDao.insert(pharmacy));
    }

    public void createPharmacyAndNotify(PharmacyEntity pharmacy, Runnable onSaved) {
        AppExecutors.io(() -> {
            pharmacyDao.insert(pharmacy);
            AppExecutors.main(onSaved);
        });
    }
}
