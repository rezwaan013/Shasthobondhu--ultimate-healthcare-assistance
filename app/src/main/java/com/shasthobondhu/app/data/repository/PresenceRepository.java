package com.shasthobondhu.app.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.shasthobondhu.app.data.model.DonorPresence;
import com.shasthobondhu.app.data.remote.FirebaseService;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PresenceRepository {

    private final FirebaseAuth auth;
    private final FirebaseDatabase database;
    private final PresenceLiveData presence;
    private DatabaseReference connectedReference;
    private DatabaseReference activeStatusReference;
    private ValueEventListener connectedListener;
    private String activeUid;

    @Inject
    public PresenceRepository(FirebaseService firebaseService) {
        auth = firebaseService.getAuth();
        database = firebaseService.getRealtimeDb();
        presence = new PresenceLiveData(database);
    }

    public boolean isConfigured() {
        return database != null;
    }

    public LiveData<Map<String, DonorPresence>> observePresence() {
        return presence;
    }

    /** Uses Firebase's connection sentinel; opening an Activity alone never implies online. */
    public synchronized void startPresenceForCurrentUser() {
        if (database == null || auth.getCurrentUser() == null
                || !auth.getCurrentUser().isEmailVerified()) {
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        if (uid.equals(activeUid) && connectedListener != null) {
            return;
        }
        removeConnectionListener();
        activeUid = uid;
        connectedReference = database.getReference(".info/connected");
        activeStatusReference = database.getReference(
                RealtimeDatabaseRepository.PATH_USER_STATUS).child(uid);
        connectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Boolean.TRUE.equals(snapshot.getValue(Boolean.class))
                        || activeStatusReference == null) {
                    return;
                }
                activeStatusReference.onDisconnect().setValue(statusValue(false));
                activeStatusReference.setValue(statusValue(true));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // A denied status write must not affect authentication or other app features.
            }
        };
        connectedReference.addValueEventListener(connectedListener);
    }

    public synchronized void stopPresence(Callback callback) {
        if (database == null) {
            complete(callback, true, null);
            return;
        }
        DatabaseReference status = activeStatusReference;
        removeConnectionListener();
        if (status == null) {
            complete(callback, true, null);
            return;
        }
        status.setValue(statusValue(false)).addOnCompleteListener(task -> {
            complete(callback, task.isSuccessful(), task.isSuccessful()
                    ? null : Failure.WRITE_FAILED);
        });
    }

    private synchronized void removeConnectionListener() {
        if (connectedReference != null && connectedListener != null) {
            connectedReference.removeEventListener(connectedListener);
        }
        connectedReference = null;
        connectedListener = null;
        activeStatusReference = null;
        activeUid = null;
    }

    private Map<String, Object> statusValue(boolean online) {
        Map<String, Object> value = new HashMap<>();
        value.put("online", online);
        value.put("lastSeen", ServerValue.TIMESTAMP);
        return value;
    }

    public enum Failure { NOT_CONFIGURED, NOT_AUTHENTICATED, WRITE_FAILED }

    public interface Callback {
        void onComplete(boolean success, Failure failure);
    }

    private static void complete(Callback callback, boolean success, Failure failure) {
        if (callback != null) callback.onComplete(success, failure);
    }

    private static final class PresenceLiveData
            extends LiveData<Map<String, DonorPresence>> {

        private final DatabaseReference reference;
        private final ValueEventListener listener;

        PresenceLiveData(FirebaseDatabase database) {
            reference = database == null ? null : database.getReference(
                    RealtimeDatabaseRepository.PATH_USER_STATUS);
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String, DonorPresence> values = new LinkedHashMap<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Object raw = child.getValue();
                        if (!(raw instanceof Map)) {
                            continue;
                        }
                        @SuppressWarnings("unchecked")
                        DonorPresence mapped = PresenceMapper.fromMap((Map<String, Object>) raw);
                        if (mapped != null && child.getKey() != null) {
                            values.put(child.getKey(), mapped);
                        }
                    }
                    setValue(Collections.unmodifiableMap(values));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    setValue(Collections.emptyMap());
                }
            };
            if (reference == null) {
                setValue(Collections.emptyMap());
            }
        }

        @Override
        protected void onActive() {
            if (reference != null) {
                reference.addValueEventListener(listener);
            }
        }

        @Override
        protected void onInactive() {
            if (reference != null) {
                reference.removeEventListener(listener);
            }
        }
    }
}
