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

    public void addDonor(DonorEntity donor) {
        AppExecutors.io(() -> donorDao.insert(donor));
    }

    public void addDonorAndNotify(DonorEntity donor, Runnable onSaved) {
        AppExecutors.io(() -> {
            donorDao.insert(donor);
            AppExecutors.main(onSaved);
        });
    }
}
