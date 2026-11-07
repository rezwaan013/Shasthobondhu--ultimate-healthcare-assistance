package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.shasthobondhu.app.data.local.dao.MedicineDao;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.util.AppExecutors;

import java.util.List;

import javax.inject.Inject;

@SuppressWarnings("unchecked")
public class MedicineRepository {
    private final MedicineDao medicineDao;
    private final RealtimeDatabaseRepository realtimeRepository;

    @Inject
    public MedicineRepository(MedicineDao medicineDao,
                              RealtimeDatabaseRepository realtimeRepository) {
        this.medicineDao = medicineDao;
        this.realtimeRepository = realtimeRepository;
    }

    public LiveData<List<MedicineEntity>> searchMedicines(String query, String area) {
        String nameFilter = query == null ? "" : query.trim();
        String areaFilter = area == null ? "" : area.trim();
        LiveData<List<MedicineEntity>> local = medicineDao.searchMedicines(nameFilter, areaFilter);
        if (!realtimeRepository.isConfigured() || !realtimeRepository.isAuthenticated()) {
            return local;
        }
        LiveData<List<MedicineEntity>> remote = realtimeRepository.observeMedicines();
        MediatorLiveData<List<MedicineEntity>> result = new MediatorLiveData<>();
        final List<MedicineEntity>[] localValue = new List[]{null};
        final List<MedicineEntity>[] remoteValue = new List[]{null};
        result.addSource(local, value -> {
            localValue[0] = value;
            publish(result, remoteValue[0], localValue[0], nameFilter, areaFilter);
        });
        result.addSource(remote, value -> {
            remoteValue[0] = value;
            publish(result, remoteValue[0], localValue[0], nameFilter, areaFilter);
        });
        return result;
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

    private void publish(MediatorLiveData<List<MedicineEntity>> result,
                         List<MedicineEntity> remote, List<MedicineEntity> local,
                         String query, String area) {
        if (remote != null && !remote.isEmpty()) {
            String nameFilter = query.toLowerCase(java.util.Locale.ROOT);
            String areaFilter = area.toLowerCase(java.util.Locale.ROOT);
            List<MedicineEntity> matches = new java.util.ArrayList<>();
            for (MedicineEntity item : remote) {
                boolean nameMatches = nameFilter.isEmpty()
                        || safe(item.name).toLowerCase(java.util.Locale.ROOT).contains(nameFilter)
                        || safe(item.pharmacyName).toLowerCase(java.util.Locale.ROOT)
                        .contains(nameFilter);
                boolean areaMatches = areaFilter.isEmpty()
                        || safe(item.pharmacyArea).toLowerCase(java.util.Locale.ROOT)
                        .contains(areaFilter);
                if (nameMatches && areaMatches) matches.add(item);
            }
            result.setValue(matches);
        } else if (local != null) {
            result.setValue(local);
        } else if (remote != null) {
            result.setValue(remote);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
