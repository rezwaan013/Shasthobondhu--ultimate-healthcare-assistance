package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.shasthobondhu.app.data.local.dao.HospitalDao;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.util.AppExecutors;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

@SuppressWarnings("unchecked")
public class HospitalRepository {
    private final HospitalDao hospitalDao;
    private final RealtimeDatabaseRepository realtimeRepository;

    @Inject
    public HospitalRepository(HospitalDao hospitalDao,
                              RealtimeDatabaseRepository realtimeRepository) {
        this.hospitalDao = hospitalDao;
        this.realtimeRepository = realtimeRepository;
    }

    public LiveData<List<HospitalEntity>> searchHospitals(String query) {
        String filter = query == null ? "" : query.trim();
        LiveData<List<HospitalEntity>> local = hospitalDao.searchHospitals(filter);
        if (!realtimeRepository.isConfigured() || !realtimeRepository.isAuthenticated()) {
            return local;
        }
        LiveData<List<HospitalEntity>> remote = realtimeRepository.observeHospitals();
        MediatorLiveData<List<HospitalEntity>> result = new MediatorLiveData<>();
        final List<HospitalEntity>[] localValue = new List[]{null};
        final List<HospitalEntity>[] remoteValue = new List[]{null};
        result.addSource(local, value -> {
            localValue[0] = value;
            publish(result, remoteValue[0], localValue[0], filter);
        });
        result.addSource(remote, value -> {
            remoteValue[0] = value;
            publish(result, remoteValue[0], localValue[0], filter);
        });
        return result;
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

    private void publish(MediatorLiveData<List<HospitalEntity>> result,
                         List<HospitalEntity> remote, List<HospitalEntity> local, String query) {
        if (remote == null && local == null) {
            return;
        }
        Map<String, HospitalEntity> combined = new LinkedHashMap<>();
        if (remote != null) {
            for (HospitalEntity item : remote) {
                combined.put(hospitalKey(item), item);
            }
        }
        // Local owner changes override an equivalent cloud entry on this device.
        if (local != null) {
            for (HospitalEntity item : local) {
                combined.put(hospitalKey(item), item);
            }
        }
        String filter = query.toLowerCase(java.util.Locale.ROOT);
        List<HospitalEntity> matches = new java.util.ArrayList<>();
        for (HospitalEntity item : combined.values()) {
            String searchable = safe(item.name) + " " + safe(item.address) + " "
                    + safe(item.area) + " " + safe(item.services);
            if (filter.isEmpty()
                    || searchable.toLowerCase(java.util.Locale.ROOT).contains(filter)) {
                matches.add(item);
            }
        }
        matches.sort(java.util.Comparator.comparing(item -> safe(item.name),
                String.CASE_INSENSITIVE_ORDER));
        result.setValue(matches);
    }

    private String hospitalKey(HospitalEntity item) {
        return (safe(item.name) + "|" + safe(item.address) + "|" + safe(item.phone))
                .toLowerCase(java.util.Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
