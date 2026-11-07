package com.shasthobondhu.app.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shasthobondhu.app.data.local.dao.DonorDao;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.util.AppExecutors;
import com.shasthobondhu.app.util.constants.AppConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SuppressWarnings("unchecked")
public class DonorRepository {
    private static final String TAG = "DonorRepository";

    private final DonorDao donorDao;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final RealtimeDatabaseRepository realtimeRepository;

    @Inject
    public DonorRepository(DonorDao donorDao, FirebaseService firebaseService,
                           RealtimeDatabaseRepository realtimeRepository) {
        this.donorDao = donorDao;
        this.auth = firebaseService.getAuth();
        this.firestore = firebaseService.getDb();
        this.realtimeRepository = realtimeRepository;
        startRemoteSync();
        syncExistingLocalProfile();
    }

    public LiveData<List<DonorEntity>> getDonors(String bloodGroup, String division, String area) {
        LiveData<List<DonorEntity>> local = donorDao.searchDonors(bloodGroup == null ? "" : bloodGroup,
                division == null ? "" : division,
                area == null ? "" : area,
                System.currentTimeMillis());
        if (!realtimeRepository.isConfigured() || !realtimeRepository.isAuthenticated()) {
            return local;
        }
        LiveData<List<DonorEntity>> remote = realtimeRepository.observeDonors(
                bloodGroup, division, area);
        MediatorLiveData<List<DonorEntity>> result = new MediatorLiveData<>();
        final List<DonorEntity>[] localValue = new List[]{null};
        final List<DonorEntity>[] remoteValue = new List[]{null};
        result.addSource(local, value -> {
            localValue[0] = value;
            publishPreferred(result, remoteValue[0], localValue[0]);
        });
        result.addSource(remote, value -> {
            remoteValue[0] = value;
            publishPreferred(result, remoteValue[0], localValue[0]);
        });
        return result;
    }

    public LiveData<List<DonorEntity>> getAllDonors() {
        return donorDao.getAllDonors(System.currentTimeMillis());
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
            syncDonorToFirestore(donor);
            realtimeRepository.saveDonorProfile(donor, null);
            AppExecutors.main(onSaved);
        });
    }

    public void updateAvailability(String ownerUid, boolean available,
                                   RealtimeDatabaseRepository.CompletionCallback callback) {
        if (ownerUid == null || ownerUid.trim().isEmpty()) {
            if (callback != null) {
                callback.onComplete(false,
                        RealtimeDatabaseRepository.Failure.UNAUTHENTICATED);
            }
            return;
        }
        AppExecutors.io(() -> {
            donorDao.updateAvailability(ownerUid, available);
            if (auth.getCurrentUser() != null
                    && ownerUid.equals(auth.getCurrentUser().getUid())) {
                firestore.collection(AppConstants.COLLECTION_DONORS).document(ownerUid)
                        .update("available", available,
                                "updatedAt", FieldValue.serverTimestamp());
            }
            realtimeRepository.updateDonorAvailability(ownerUid, available, callback);
        });
    }

    private void syncDonorToFirestore(DonorEntity donor) {
        if (donor.ownerUid == null || auth.getCurrentUser() == null
                || !donor.ownerUid.equals(auth.getCurrentUser().getUid())) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("ownerUid", donor.ownerUid);
        data.put("name", donor.name);
        data.put("phone", donor.phone);
        data.put("bloodGroup", donor.bloodGroup);
        data.put("division", donor.division);
        data.put("district", donor.district);
        data.put("upazila", donor.upazila);
        data.put("lastDonationDate", donor.lastDonationDate);
        data.put("available", donor.isAvailable);
        data.put("availableAfterEpoch", donor.availableAfterEpoch);
        data.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection(AppConstants.COLLECTION_DONORS)
                .document(donor.ownerUid)
                .set(data)
                .addOnFailureListener(error ->
                        Log.e(TAG, "Could not sync donor profile", error));
    }

    private void startRemoteSync() {
        firestore.collection(AppConstants.COLLECTION_DONORS)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Could not listen for donor updates", error);
                        return;
                    }
                    if (snapshot == null) {
                        return;
                    }
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        DonorEntity remote = toDonor(document);
                        if (remote == null) {
                            continue;
                        }
                        AppExecutors.io(() -> {
                            DonorEntity existing = donorDao.getByOwnerUid(remote.ownerUid);
                            if (existing != null) {
                                remote.id = existing.id;
                                donorDao.update(remote);
                            } else {
                                donorDao.insert(remote);
                            }
                        });
                    }
                });
    }

    private void syncExistingLocalProfile() {
        if (auth.getCurrentUser() == null) {
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        AppExecutors.io(() -> {
            DonorEntity existing = donorDao.getByOwnerUid(uid);
            if (existing != null) {
                syncDonorToFirestore(existing);
            }
        });
    }

    private DonorEntity toDonor(DocumentSnapshot document) {
        String ownerUid = document.getString("ownerUid");
        String name = document.getString("name");
        String phone = document.getString("phone");
        String bloodGroup = document.getString("bloodGroup");
        String division = document.getString("division");
        String district = document.getString("district");
        String area = document.getString("upazila");
        Boolean available = document.getBoolean("available");
        Long availableAfter = document.getLong("availableAfterEpoch");
        if (ownerUid == null || name == null || phone == null || bloodGroup == null
                || division == null || district == null || area == null) {
            return null;
        }
        String lastDonation = document.getString("lastDonationDate");
        DonorEntity donor = new DonorEntity(name, phone, bloodGroup, division, district, area,
                lastDonation == null ? "Not shared" : lastDonation,
                available == null || available, ownerUid);
        donor.availableAfterEpoch = availableAfter == null ? 0L : availableAfter;
        return donor;
    }

    private static void publishPreferred(MediatorLiveData<List<DonorEntity>> result,
                                         List<DonorEntity> remote,
                                         List<DonorEntity> local) {
        if (remote != null && !remote.isEmpty()) {
            result.setValue(remote);
        } else if (local != null) {
            result.setValue(local);
        } else if (remote != null) {
            result.setValue(remote);
        }
    }
}
