package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import com.shasthobondhu.app.data.local.dao.DonorDao;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import javax.inject.Inject;
import java.util.List;

public class DonorRepository {
    private final DonorDao donorDao;

    @Inject
    public DonorRepository(DonorDao donorDao) {
        this.donorDao = donorDao;
    }

    public LiveData<List<DonorEntity>> getDonors(String bloodGroup, String division, String area) {
        return donorDao.searchDonors(bloodGroup, division, area);
    }

    public LiveData<List<DonorEntity>> getDonorsByArea(String area) {
        return donorDao.searchDonorsByArea(area);
    }

    public void addDonor(DonorEntity donor) {
        donorDao.insert(donor);
    }
}
