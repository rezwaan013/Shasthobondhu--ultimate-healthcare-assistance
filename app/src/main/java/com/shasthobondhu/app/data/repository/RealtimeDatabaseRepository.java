package com.shasthobondhu.app.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.data.model.AppNotification;
import com.shasthobondhu.app.data.remote.FirebaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Typed RTDB gateway. Durable Room/Firestore repositories remain available for offline fallback;
 * this class owns lifecycle-aware RTDB reads and authenticated real-time writes.
 */
@Singleton
public class RealtimeDatabaseRepository {

    public static final String PATH_USERS = "users";
    public static final String PATH_DONORS = "blood_donors";
    public static final String PATH_REQUESTS = "blood_requests";
    public static final String PATH_REQUEST_STATUS = "blood_request_status";
    public static final String PATH_HOSPITALS = "hospitals";
    public static final String PATH_MEDICINES = "medicines";
    public static final String PATH_NOTIFICATIONS = "notifications";
    public static final String PATH_USER_STATUS = "user_status";

    private final FirebaseAuth auth;
    private final FirebaseDatabase database;

    @Inject
    public RealtimeDatabaseRepository(FirebaseService firebaseService) {
        auth = firebaseService.getAuth();
        database = firebaseService.getRealtimeDb();
    }

    public boolean isConfigured() {
        return database != null;
    }

    public boolean isAuthenticated() {
        return auth.getCurrentUser() != null;
    }

    public void saveUserProfile(String uid, String name, String email, String phone,
                                String role, CompletionCallback callback) {
        if (database == null) {
            complete(callback, false, Failure.NOT_CONFIGURED);
            return;
        }
        if (!owns(uid)) {
            complete(callback, false, Failure.UNAUTHENTICATED);
            return;
        }
        if (isBlank(name) || isBlank(email) || isBlank(phone) || isBlank(role)) {
            complete(callback, false, Failure.INVALID_INPUT);
            return;
        }
        Map<String, Object> value = new HashMap<>();
        value.put("uid", uid);
        value.put("name", name.trim());
        value.put("email", email.trim().toLowerCase(Locale.ROOT));
        value.put("phone", phone.trim());
        value.put("role", role);
        value.put("updatedAt", ServerValue.TIMESTAMP);
        reference(PATH_USERS).child(uid).updateChildren(value)
                .addOnCompleteListener(task -> complete(callback, task.isSuccessful(),
                        task.isSuccessful() ? null : failure(task.getException())));
    }

    public void saveDonorProfile(DonorEntity donor, CompletionCallback callback) {
        if (database == null) {
            complete(callback, false, Failure.NOT_CONFIGURED);
            return;
        }
        if (donor == null || !owns(donor.ownerUid)) {
            complete(callback, false, Failure.UNAUTHENTICATED);
            return;
        }
        if (isBlank(donor.name) || isBlank(donor.phone) || isBlank(donor.bloodGroup)
                || isBlank(donor.division) || isBlank(donor.district)
                || isBlank(donor.upazila)) {
            complete(callback, false, Failure.INVALID_INPUT);
            return;
        }
        Map<String, Object> value = new HashMap<>();
        value.put("uid", donor.ownerUid);
        value.put("name", donor.name.trim());
        value.put("bloodGroup", donor.bloodGroup.trim());
        value.put("phone", donor.phone.trim());
        value.put("division", donor.division.trim());
        value.put("district", donor.district.trim());
        value.put("upazila", donor.upazila.trim());
        value.put("location", donor.district.trim() + ", " + donor.upazila.trim());
        value.put("lastDonationDate", safe(donor.lastDonationDate));
        value.put("available", donor.isAvailable);
        value.put("availableAfterEpoch", donor.availableAfterEpoch);
        value.put("updatedAt", ServerValue.TIMESTAMP);
        reference(PATH_DONORS).child(donor.ownerUid).setValue(value)
                .addOnCompleteListener(task -> complete(callback, task.isSuccessful(),
                        task.isSuccessful() ? null : failure(task.getException())));
    }

    public void updateDonorAvailability(String uid, boolean available,
                                        CompletionCallback callback) {
        if (database == null) {
            complete(callback, false, Failure.NOT_CONFIGURED);
            return;
        }
        if (!owns(uid)) {
            complete(callback, false, Failure.UNAUTHENTICATED);
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("available", available);
        updates.put("updatedAt", ServerValue.TIMESTAMP);
        reference(PATH_DONORS).child(uid).updateChildren(updates)
                .addOnCompleteListener(task -> complete(callback, task.isSuccessful(),
                        task.isSuccessful() ? null : failure(task.getException())));
    }

    public LiveData<List<DonorEntity>> observeDonors(String bloodGroup, String division,
                                                      String area) {
        if (!canRead()) return emptyList();
        Query query = reference(PATH_DONORS);
        String normalizedBlood = safe(bloodGroup).trim();
        if (!normalizedBlood.isEmpty()) {
            query = query.orderByChild("bloodGroup").equalTo(normalizedBlood);
        }
        String normalizedDivision = safe(division).trim();
        String normalizedArea = safe(area).trim();
        return new SnapshotListLiveData<>(query, snapshot -> {
            List<DonorEntity> donors = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (DataSnapshot child : snapshot.getChildren()) {
                DonorEntity donor = donorFrom(child);
                if (donor == null || !donor.isAvailable || donor.availableAfterEpoch > now) continue;
                if (!normalizedDivision.isEmpty()
                        && !equalsIgnoreCase(donor.division, normalizedDivision)) continue;
                if (!normalizedArea.isEmpty()
                        && !equalsIgnoreCase(donor.district, normalizedArea)
                        && !equalsIgnoreCase(donor.upazila, normalizedArea)) continue;
                donors.add(donor);
            }
            donors.sort(Comparator.comparing(donor -> safe(donor.name),
                    String.CASE_INSENSITIVE_ORDER));
            return donors;
        });
    }

    public String newBloodRequestId() {
        if (!canRead()) return null;
        return reference(PATH_REQUESTS).push().getKey();
    }

    public void saveBloodRequest(RequestEntity request, CompletionCallback callback) {
        if (database == null) {
            complete(callback, false, Failure.NOT_CONFIGURED);
            return;
        }
        if (request == null || !owns(request.requesterUid)) {
            complete(callback, false, Failure.UNAUTHENTICATED);
            return;
        }
        if (isBlank(request.remoteId) || isBlank(request.patientName)
                || isBlank(request.bloodGroup) || isBlank(request.hospitalName)
                || isBlank(request.location) || isBlank(request.contactPhone)) {
            complete(callback, false, Failure.INVALID_INPUT);
            return;
        }
        Map<String, Object> value = requestValue(request);
        reference(PATH_REQUESTS).child(request.remoteId).setValue(value)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        writeRequestStatus(request.remoteId, "Open", request.requesterUid, null);
                    }
                    complete(callback, task.isSuccessful(),
                            task.isSuccessful() ? null : failure(task.getException()));
                });
    }

    public LiveData<List<RequestEntity>> observeBloodRequests() {
        if (!canRead()) return emptyList();
        return new SnapshotListLiveData<>(reference(PATH_REQUESTS), snapshot -> {
            List<RequestEntity> requests = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                RequestEntity request = requestFrom(child);
                if (request != null && !"Fulfilled".equals(request.status)
                        && !"Cancelled".equals(request.status)) {
                    requests.add(request);
                }
            }
            Collections.reverse(requests);
            return requests;
        });
    }

    public void acceptBloodRequest(String requestId, String accepterName,
                                   CompletionCallback callback) {
        if (database == null) {
            complete(callback, false, Failure.NOT_CONFIGURED);
            return;
        }
        String uid = currentUid();
        if (uid == null || isBlank(requestId)) {
            complete(callback, false, Failure.UNAUTHENTICATED);
            return;
        }
        DatabaseReference requestRef = reference(PATH_REQUESTS).child(requestId);
        requestRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                String status = stringValue(currentData.child("status").getValue());
                String requesterId = stringValue(currentData.child("requesterId").getValue());
                if (!RequestStatusPolicy.canDonorAccept(
                        status, requesterId, uid, "Accepted")) {
                    return Transaction.abort();
                }
                currentData.child("status").setValue("Accepted");
                currentData.child("acceptedByUid").setValue(uid);
                currentData.child("acceptedByName").setValue(safe(accepterName));
                currentData.child("updatedAt").setValue(ServerValue.TIMESTAMP);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed,
                                   DataSnapshot snapshot) {
                if (committed) {
                    writeRequestStatus(requestId, "Accepted", uid, null);
                }
                Failure failure = null;
                if (!committed) {
                    failure = error != null ? failure(error)
                            : snapshot == null || !snapshot.exists()
                            ? Failure.NOT_FOUND : Failure.CONFLICT;
                }
                complete(callback, committed, failure);
            }
        });
    }

    public void updateOwnedRequestStatus(String requestId, String newStatus,
                                         CompletionCallback callback) {
        if (database == null) {
            complete(callback, false, Failure.NOT_CONFIGURED);
            return;
        }
        String uid = currentUid();
        if (uid == null || isBlank(requestId) || !isTerminalStatus(newStatus)) {
            complete(callback, false, Failure.INVALID_INPUT);
            return;
        }
        DatabaseReference requestRef = reference(PATH_REQUESTS).child(requestId);
        requestRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                String requesterId = stringValue(currentData.child("requesterId").getValue());
                String oldStatus = stringValue(currentData.child("status").getValue());
                if (!uid.equals(requesterId)
                        || !RequestStatusPolicy.canOwnerTransition(oldStatus, newStatus)) {
                    return Transaction.abort();
                }
                currentData.child("status").setValue(newStatus);
                currentData.child("updatedAt").setValue(ServerValue.TIMESTAMP);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed,
                                   DataSnapshot snapshot) {
                if (committed) writeRequestStatus(requestId, newStatus, uid, null);
                complete(callback, committed, committed ? null
                        : error == null ? Failure.CONFLICT : failure(error));
            }
        });
    }

    public LiveData<List<HospitalEntity>> observeHospitals() {
        if (!canRead()) return emptyList();
        return new SnapshotListLiveData<>(reference(PATH_HOSPITALS), snapshot -> {
            List<HospitalEntity> hospitals = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                String name = childString(child, "name");
                String address = childString(child, "address");
                String phone = childString(child, "phone");
                String location = childString(child, "location");
                String services = childString(child, "services");
                if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) continue;
                HospitalEntity hospital = new HospitalEntity(name, address, phone, location,
                        services, null);
                hospital.doctors = childString(child, "doctors");
                hospitals.add(hospital);
            }
            hospitals.sort(Comparator.comparing(item -> safe(item.name),
                    String.CASE_INSENSITIVE_ORDER));
            return hospitals;
        });
    }

    public LiveData<List<MedicineEntity>> observeMedicines() {
        if (!canRead()) return emptyList();
        return new SnapshotListLiveData<>(reference(PATH_MEDICINES), snapshot -> {
            List<MedicineEntity> medicines = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                String name = childString(child, "name");
                String pharmacy = childString(child, "pharmacy");
                String location = childString(child, "location");
                String availability = childString(child, "availability");
                if (name.isEmpty() || pharmacy.isEmpty()) continue;
                medicines.add(new MedicineEntity(name, pharmacy,
                        availability.isEmpty() ? "In Stock" : availability,
                        childString(child, "lastUpdated"), childString(child, "phone"),
                        0, location, childString(child, "address")));
            }
            medicines.sort(Comparator.comparing(item -> safe(item.name),
                    String.CASE_INSENSITIVE_ORDER));
            return medicines;
        });
    }

    public LiveData<List<AppNotification>> observeNotifications(String uid) {
        if (!owns(uid)) return emptyList();
        return new SnapshotListLiveData<>(reference(PATH_NOTIFICATIONS).child(uid), snapshot -> {
            List<AppNotification> notifications = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                notifications.add(new AppNotification(child.getKey(),
                        childString(child, "title"), childString(child, "message"),
                        childString(child, "type"), childString(child, "requestId"),
                        childLong(child, "createdAt"), childBoolean(child, "read", false)));
            }
            notifications.sort((left, right) -> Long.compare(right.createdAt, left.createdAt));
            return notifications;
        });
    }

    public void markNotificationRead(String uid, String notificationId,
                                     CompletionCallback callback) {
        if (database == null) {
            complete(callback, false, Failure.NOT_CONFIGURED);
            return;
        }
        if (!owns(uid) || isBlank(notificationId)) {
            complete(callback, false, Failure.UNAUTHENTICATED);
            return;
        }
        reference(PATH_NOTIFICATIONS).child(uid).child(notificationId).child("read")
                .setValue(true).addOnCompleteListener(task -> complete(callback,
                        task.isSuccessful(), task.isSuccessful()
                                ? null : failure(task.getException())));
    }

    private void writeRequestStatus(String requestId, String status, String updatedBy,
                                    CompletionCallback callback) {
        Map<String, Object> value = new HashMap<>();
        value.put("status", status);
        value.put("updatedBy", updatedBy);
        value.put("timestamp", ServerValue.TIMESTAMP);
        reference(PATH_REQUEST_STATUS).child(requestId).setValue(value)
                .addOnCompleteListener(task -> complete(callback, task.isSuccessful(),
                        task.isSuccessful() ? null : failure(task.getException())));
    }

    private Map<String, Object> requestValue(RequestEntity request) {
        Map<String, Object> value = new HashMap<>();
        value.put("requestId", request.remoteId);
        value.put("requesterId", request.requesterUid);
        value.put("patientName", request.patientName);
        value.put("patientDetails", safe(request.patientDetails));
        value.put("bloodGroup", request.bloodGroup);
        value.put("units", request.units);
        value.put("hospital", request.hospitalName);
        value.put("location", request.location);
        value.put("phone", request.contactPhone);
        value.put("urgency", safe(request.urgency));
        value.put("neededDate", safe(request.neededDate));
        value.put("neededTime", safe(request.neededTime));
        value.put("status", "Open");
        value.put("timestamp", ServerValue.TIMESTAMP);
        value.put("updatedAt", ServerValue.TIMESTAMP);
        return value;
    }

    private DonorEntity donorFrom(DataSnapshot child) {
        String uid = childString(child, "uid");
        String name = childString(child, "name");
        String phone = childString(child, "phone");
        String bloodGroup = childString(child, "bloodGroup");
        String division = childString(child, "division");
        String district = childString(child, "district");
        String upazila = childString(child, "upazila");
        if (uid.isEmpty() || name.isEmpty() || phone.isEmpty() || bloodGroup.isEmpty()) return null;
        DonorEntity donor = new DonorEntity(name, phone, bloodGroup, division, district, upazila,
                childString(child, "lastDonationDate"),
                childBoolean(child, "available", false), uid);
        donor.availableAfterEpoch = childLong(child, "availableAfterEpoch");
        return donor;
    }

    private RequestEntity requestFrom(DataSnapshot child) {
        String requesterId = childString(child, "requesterId");
        String patientName = childString(child, "patientName");
        String bloodGroup = childString(child, "bloodGroup");
        String hospital = childString(child, "hospital");
        String location = childString(child, "location");
        String phone = childString(child, "phone");
        if (requesterId.isEmpty() || patientName.isEmpty() || bloodGroup.isEmpty()
                || hospital.isEmpty() || location.isEmpty() || phone.isEmpty()) return null;
        RequestEntity request = new RequestEntity(patientName, bloodGroup,
                (int) childLong(child, "units"), hospital, location,
                childString(child, "urgency"), phone, childString(child, "status"), requesterId);
        request.remoteId = child.getKey();
        request.patientDetails = childString(child, "patientDetails");
        request.neededDate = childString(child, "neededDate");
        request.neededTime = childString(child, "neededTime");
        request.acceptedByUid = childString(child, "acceptedByUid");
        request.acceptedByName = childString(child, "acceptedByName");
        return request;
    }

    private boolean owns(String uid) {
        return database != null && uid != null && auth.getCurrentUser() != null
                && uid.equals(auth.getCurrentUser().getUid());
    }

    private boolean canRead() {
        return database != null && auth.getCurrentUser() != null;
    }

    private String currentUid() {
        return auth.getCurrentUser() == null ? null : auth.getCurrentUser().getUid();
    }

    private DatabaseReference reference(String path) {
        return database.getReference(path);
    }

    private static boolean isTerminalStatus(String status) {
        return "Fulfilled".equals(status) || "Cancelled".equals(status);
    }

    private static Failure failure(DatabaseError error) {
        if (error == null) return Failure.WRITE_FAILED;
        if (error.getCode() == DatabaseError.PERMISSION_DENIED) return Failure.PERMISSION_DENIED;
        if (error.getCode() == DatabaseError.DISCONNECTED
                || error.getCode() == DatabaseError.NETWORK_ERROR) return Failure.NETWORK;
        return Failure.WRITE_FAILED;
    }

    private static Failure failure(Exception error) {
        if (error == null || error.getMessage() == null) return Failure.WRITE_FAILED;
        String message = error.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("permission") || message.contains("denied")) {
            return Failure.PERMISSION_DENIED;
        }
        if (message.contains("network") || message.contains("disconnect")
                || message.contains("unavailable")) {
            return Failure.NETWORK;
        }
        return Failure.WRITE_FAILED;
    }

    private static String childString(DataSnapshot snapshot, String child) {
        return stringValue(snapshot.child(child).getValue());
    }

    private static long childLong(DataSnapshot snapshot, String child) {
        Object value = snapshot.child(child).getValue();
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private static boolean childBoolean(DataSnapshot snapshot, String child,
                                        boolean fallback) {
        Boolean value = snapshot.child(child).getValue(Boolean.class);
        return value == null ? fallback : value;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        return safe(left).equalsIgnoreCase(safe(right));
    }

    private static <T> LiveData<List<T>> emptyList() {
        return new MutableLiveData<>(Collections.emptyList());
    }

    private static void complete(CompletionCallback callback, boolean success, Failure failure) {
        if (callback != null) callback.onComplete(success, failure);
    }

    public enum Failure {
        NOT_CONFIGURED,
        UNAUTHENTICATED,
        INVALID_INPUT,
        PERMISSION_DENIED,
        NETWORK,
        NOT_FOUND,
        CONFLICT,
        WRITE_FAILED
    }

    public interface CompletionCallback {
        void onComplete(boolean success, Failure failure);
    }

    private interface SnapshotMapper<T> {
        List<T> map(DataSnapshot snapshot);
    }

    /** Attaches exactly one listener while observed and removes it when the UI becomes inactive. */
    private static final class SnapshotListLiveData<T> extends LiveData<List<T>> {
        private final Query query;
        private final ValueEventListener listener;

        SnapshotListLiveData(Query query, SnapshotMapper<T> mapper) {
            this.query = query;
            this.listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    setValue(Collections.unmodifiableList(mapper.map(snapshot)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    setValue(Collections.emptyList());
                }
            };
        }

        @Override
        protected void onActive() {
            query.addValueEventListener(listener);
        }

        @Override
        protected void onInactive() {
            query.removeEventListener(listener);
        }
    }
}
