package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;

import com.shasthobondhu.app.data.local.dao.MedicineDao;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.util.AppExecutors;

import java.util.List;

import javax.inject.Inject;

public class MedicineRepository {
    private final MedicineDao medicineDao;

    @Inject
    public MedicineRepository(MedicineDao medicineDao) {
        this.medicineDao = medicineDao;
    }

    public LiveData<List<MedicineEntity>> searchMedicines(String query, String area) {
        return medicineDao.searchMedicines(query == null ? "" : query,
                area == null ? "" : area);
    }

    public LiveData<List<MedicineEntity>> getByPharmacy(int pharmacyId) {
        return medicineDao.getByPharmacy(pharmacyId);
    }

    public void addMedicine(MedicineEntity medicine) {
        AppExecutors.io(() -> medicineDao.insert(medicine));
    }

    public void addMedicineAndNotify(MedicineEntity medicine, Runnable onSaved) {
        AppExecutors.io(() -> {
            medicineDao.insert(medicine);
            AppExecutors.main(onSaved);
        });
    }

    public void updateMedicine(MedicineEntity medicine) {
        AppExecutors.io(() -> medicineDao.update(medicine));
    }

    public void deleteMedicine(MedicineEntity medicine) {
        AppExecutors.io(() -> medicineDao.delete(medicine));
    }
}
