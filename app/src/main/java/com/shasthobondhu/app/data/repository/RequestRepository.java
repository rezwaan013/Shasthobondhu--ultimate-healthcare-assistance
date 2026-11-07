package com.shasthobondhu.app.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shasthobondhu.app.data.local.dao.RequestDao;
import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.util.AppExecutors;
import com.shasthobondhu.app.util.constants.AppConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RequestRepository {
    private static final String TAG = "RequestRepository";

    private final RequestDao requestDao;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    @Inject
    public RequestRepository(RequestDao requestDao, FirebaseService firebaseService) {
        this.requestDao = requestDao;
        this.auth = firebaseService.getAuth();
        this.firestore = firebaseService.getDb();
        startRemoteSync();
    }

    public LiveData<List<RequestEntity>> getRequests(String area) {
        return requestDao.getOpenRequestsByArea(area == null ? "" : area);
    }

    public LiveData<List<RequestEntity>> getAllOpenRequests() {
        return requestDao.getOpenRequests();
    }

    public void createRequest(RequestEntity request) {
        AppExecutors.io(() -> requestDao.insert(request));
    }

    public void createRequestAndNotify(RequestEntity request, Runnable onSaved) {
        AppExecutors.io(() -> {
            if (request.remoteId == null && request.requesterUid != null) {
                request.remoteId = request.requesterUid + "_" + System.currentTimeMillis();
            }
            requestDao.insert(request);
            syncRequestToFirestore(request);
            AppExecutors.main(onSaved);
        });
    }

    public void acceptRequest(int requestId, String uid, String name,
                              AcceptCallback callback) {
        AppExecutors.io(() -> {
            RequestEntity request = findById(requestId);
            if (request == null || request.remoteId == null || auth.getCurrentUser() == null) {
                boolean accepted = requestDao.acceptRequest(requestId, uid, name) > 0;
                AppExecutors.main(() -> callback.onComplete(accepted));
                return;
            }
            DocumentReference reference = firestore.collection(AppConstants.COLLECTION_REQUESTS)
                    .document(request.remoteId);
            firestore.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(reference);
                if (!snapshot.exists() || !"Open".equals(snapshot.getString("status"))) {
                    return false;
                }
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "Accepted");
                updates.put("acceptedByUid", uid);
                updates.put("acceptedByName", name);
                updates.put("acceptedAt", FieldValue.serverTimestamp());
                transaction.update(reference, updates);
                return true;
            }).addOnSuccessListener(accepted -> AppExecutors.io(() -> {
                if (accepted) {
                    requestDao.acceptRequest(requestId, uid, name);
                }
                AppExecutors.main(() -> callback.onComplete(accepted));
            })).addOnFailureListener(error -> {
                Log.e(TAG, "Could not accept remote request", error);
                AppExecutors.main(() -> callback.onComplete(false));
            });
        });
    }

    private RequestEntity findById(int requestId) {
        return requestDao.getRequestById(requestId);
    }

    private void syncRequestToFirestore(RequestEntity request) {
        if (request.remoteId == null || request.requesterUid == null
                || auth.getCurrentUser() == null
                || !request.requesterUid.equals(auth.getCurrentUser().getUid())) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("requesterUid", request.requesterUid);
        data.put("patientName", request.patientName);
        data.put("patientDetails", request.patientDetails);
        data.put("bloodGroup", request.bloodGroup);
        data.put("units", request.units);
        data.put("hospitalName", request.hospitalName);
        data.put("location", request.location);
        data.put("neededDate", request.neededDate);
        data.put("neededTime", request.neededTime);
        data.put("urgency", request.urgency);
        data.put("contactPhone", request.contactPhone);
        data.put("status", request.status);
        data.put("acceptedByUid", request.acceptedByUid);
        data.put("acceptedByName", request.acceptedByName);
        data.put("createdAt", FieldValue.serverTimestamp());
        firestore.collection(AppConstants.COLLECTION_REQUESTS)
                .document(request.remoteId)
                .set(data)
                .addOnFailureListener(error ->
                        Log.e(TAG, "Could not sync blood request", error));
    }

    private void startRemoteSync() {
        firestore.collection(AppConstants.COLLECTION_REQUESTS)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Could not listen for blood requests", error);
                        return;
                    }
                    if (snapshot == null) {
                        return;
                    }
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        RequestEntity remote = toRequest(document);
                        if (remote == null) {
                            continue;
                        }
                        AppExecutors.io(() -> {
                            RequestEntity existing = requestDao.getByRemoteId(remote.remoteId);
                            if (existing != null) {
                                remote.id = existing.id;
                                requestDao.update(remote);
                            } else {
                                requestDao.insert(remote);
                            }
                        });
                    }
                });
    }

    private RequestEntity toRequest(DocumentSnapshot document) {
        String requesterUid = document.getString("requesterUid");
        String patientName = document.getString("patientName");
        String bloodGroup = document.getString("bloodGroup");
        Long units = document.getLong("units");
        String hospitalName = document.getString("hospitalName");
        String location = document.getString("location");
        String urgency = document.getString("urgency");
        String phone = document.getString("contactPhone");
        String status = document.getString("status");
        if (requesterUid == null || patientName == null || bloodGroup == null || units == null
                || hospitalName == null || location == null || urgency == null || phone == null) {
            return null;
        }
        RequestEntity request = new RequestEntity(patientName, bloodGroup, units.intValue(),
                hospitalName, location, urgency, phone,
                status == null ? "Open" : status, requesterUid);
        request.remoteId = document.getId();
        request.patientDetails = document.getString("patientDetails");
        request.neededDate = document.getString("neededDate");
        request.neededTime = document.getString("neededTime");
        request.acceptedByUid = document.getString("acceptedByUid");
        request.acceptedByName = document.getString("acceptedByName");
        return request;
    }

    public interface AcceptCallback {
        void onComplete(boolean accepted);
    }
}
