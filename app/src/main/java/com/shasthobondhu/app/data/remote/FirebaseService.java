package com.shasthobondhu.app.data.remote;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shasthobondhu.app.BuildConfig;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseService {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseDatabase realtimeDb;

    @Inject
    public FirebaseService() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        FirebaseDatabase configuredDatabase = null;
        try {
            String databaseUrl = FirebaseApp.getInstance().getOptions().getDatabaseUrl();
            if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
                databaseUrl = BuildConfig.FIREBASE_DATABASE_URL;
            }
            configuredDatabase = FirebaseDatabase.getInstance(databaseUrl);
        } catch (RuntimeException ignored) {
            // Invalid/missing Firebase configuration keeps RTDB features disabled without a crash.
        }
        this.realtimeDb = configuredDatabase;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getDb() { return db; }
    public FirebaseDatabase getRealtimeDb() { return realtimeDb; }
}
