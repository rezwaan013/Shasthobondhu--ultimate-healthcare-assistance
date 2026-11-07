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

    public void setAvailability(String uid, boolean available, Callback callback) {
        if (database == null) {
            callback.onComplete(false, Failure.NOT_CONFIGURED);
            return;
        }
        if (auth.getCurrentUser() == null || uid == null
                || !uid.equals(auth.getCurrentUser().getUid())) {
            callback.onComplete(false, Failure.NOT_AUTHENTICATED);
            return;
        }
        DatabaseReference reference = database.getReference("presence").child(uid);
        Map<String, Object> disconnected = presenceValue(false);
        reference.onDisconnect().setValue(disconnected).addOnCompleteListener(disconnectTask -> {
            if (!disconnectTask.isSuccessful()) {
                callback.onComplete(false, Failure.WRITE_FAILED);
                return;
            }
            reference.setValue(presenceValue(available)).addOnCompleteListener(writeTask ->
                    callback.onComplete(writeTask.isSuccessful(), writeTask.isSuccessful()
                            ? null : Failure.WRITE_FAILED));
        });
    }

    private Map<String, Object> presenceValue(boolean available) {
        Map<String, Object> value = new HashMap<>();
        value.put("available", available);
        value.put("lastSeen", ServerValue.TIMESTAMP);
        return value;
    }

    public enum Failure { NOT_CONFIGURED, NOT_AUTHENTICATED, WRITE_FAILED }

    public interface Callback {
        void onComplete(boolean success, Failure failure);
    }

    private static final class PresenceLiveData
            extends LiveData<Map<String, DonorPresence>> {

        private final DatabaseReference reference;
        private final ValueEventListener listener;

        PresenceLiveData(FirebaseDatabase database) {
            reference = database == null ? null : database.getReference("presence");
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
