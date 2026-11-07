package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;

import com.shasthobondhu.app.data.local.dao.DonorDao;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.util.AppExecutors;

import java.util.List;

import javax.inject.Inject;

public class DonorRepository {
    private final DonorDao donorDao;

    @Inject
    public DonorRepository(DonorDao donorDao) {
        this.donorDao = donorDao;
    }

    public LiveData<List<DonorEntity>> getDonors(String bloodGroup, String area) {
        return donorDao.searchDonors(bloodGroup == null ? "" : bloodGroup,
                area == null ? "" : area);
    }

    public LiveData<List<DonorEntity>> getAllDonors() {
        return donorDao.getAllDonors();
    }

    public LiveData<DonorEntity> getMyProfile(String ownerUid) {
        return donorDao.observeByOwnerUid(ownerUid == null ? "" : ownerUid);
    }

    public void addDonor(DonorEntity donor) {
        AppExecutors.io(() -> donorDao.insert(donor));
    }

    public void addDonorAndNotify(DonorEntity donor, Runnable onSaved) {
        AppExecutors.io(() -> {
            donorDao.insert(donor);
            AppExecutors.main(onSaved);
        });
    }

    public void saveDonorAndNotify(DonorEntity donor, Runnable onSaved) {
        AppExecutors.io(() -> {
            if (donor.id == 0 && donor.ownerUid != null) {
                DonorEntity existing = donorDao.getByOwnerUid(donor.ownerUid);
                if (existing != null) {
                    donor.id = existing.id;
                }
            }
            if (donor.id > 0) {
                donorDao.update(donor);
            } else {
                donorDao.insert(donor);
            }
            AppExecutors.main(onSaved);
        });
    }
}
