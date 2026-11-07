package com.shasthobondhu.app.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shasthobondhu.app.data.remote.FirebaseService;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Performs a small, visible cloud round-trip for the lab demonstration:
 * Firebase Authentication, Firestore and Realtime Database in one action.
 */
@Singleton
public class FirebaseDemoRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseDatabase realtimeDatabase;

    @Inject
    public FirebaseDemoRepository(FirebaseService service) {
        auth = service.getAuth();
        firestore = service.getDb();
        realtimeDatabase = service.getRealtimeDb();
    }

    public void runCloudCheck(ResultCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            writeToBothDatabases(currentUser, callback);
            return;
        }

        auth.signInAnonymously().addOnCompleteListener(task -> {
            FirebaseUser user = auth.getCurrentUser();
            if (!task.isSuccessful() || user == null) {
                callback.onError("Firebase Authentication failed. Enable Anonymous sign-in "
                        + "in Firebase Console. " + errorMessage(task.getException()));
                return;
            }
            writeToBothDatabases(user, callback);
        });
    }

    private void writeToBothDatabases(FirebaseUser user, ResultCallback callback) {
        long now = System.currentTimeMillis();
        String eventId = user.getUid() + "_" + now;

        Map<String, Object> event = new HashMap<>();
        event.put("uid", user.getUid());
        event.put("source", "android_lab_demo");
        event.put("message", "ShasthoBondhu cloud components are connected");
        event.put("timestamp", now);

        Task<Void> firestoreWrite = firestore.collection("lab_demo_events")
                .document(eventId)
                .set(event);
        Task<Void> realtimeWrite = realtimeDatabase.getReference("lab_demo_status")
                .child(user.getUid())
                .setValue(event);

        Tasks.whenAllComplete(firestoreWrite, realtimeWrite).addOnCompleteListener(ignored -> {
            boolean firestoreOk = firestoreWrite.isSuccessful();
            boolean realtimeOk = realtimeWrite.isSuccessful();
            if (firestoreOk && realtimeOk) {
                callback.onSuccess(user.getUid());
                return;
            }
            StringBuilder message = new StringBuilder("Auth succeeded. ");
            message.append(firestoreOk ? "Firestore succeeded. "
                    : "Firestore failed: " + errorMessage(firestoreWrite.getException()) + " ");
            message.append(realtimeOk ? "Realtime Database succeeded."
                    : "Realtime Database failed: " + errorMessage(realtimeWrite.getException()));
            callback.onError(message.toString());
        });
    }

    private String errorMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return "unknown configuration or rules error.";
        }
        return exception.getMessage();
    }

    public interface ResultCallback {
        void onSuccess(String firebaseUid);

        void onError(String message);
    }
}
