package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import com.shasthobondhu.app.data.local.dao.MedicineDao;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import javax.inject.Inject;
import java.util.List;

public class MedicineRepository {
    private final MedicineDao medicineDao;

    @Inject
    public MedicineRepository(MedicineDao medicineDao) {
        this.medicineDao = medicineDao;
    }

    public LiveData<List<MedicineEntity>> searchMedicines(String query) {
        return medicineDao.searchMedicines(query);
    }
}
