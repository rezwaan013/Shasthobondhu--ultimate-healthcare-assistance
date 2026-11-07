package com.shasthobondhu.app.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.shasthobondhu.app.data.remote.FirebaseService;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Locale;

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
        if (currentUser == null || currentUser.isAnonymous()) {
            callback.onError("Sign in with an Email/Password Firebase account first. "
                    + "Local demo accounts cannot write to the cloud.");
            return;
        }
        writeToBothDatabases(currentUser, callback);
    }

    private void writeToBothDatabases(FirebaseUser user, ResultCallback callback) {
        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("uid", user.getUid());
        event.put("source", "android_lab_demo");
        event.put("message", "ShasthoBondhu cloud components are connected");
        event.put("timestamp", now);

        Task<Void> firestoreWrite = firestore.collection("users")
                .document(user.getUid())
                .set(Collections.singletonMap("lastLabCheck", event), SetOptions.merge());
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
                    : "Realtime Database failed: "
                    + realtimeErrorMessage(realtimeWrite.getException()));
            callback.onError(message.toString());
        });
    }

    private String realtimeErrorMessage(Exception exception) {
        String message = errorMessage(exception);
        if (message.toLowerCase(Locale.US).contains("permission denied")) {
            return "Permission denied. Publish database.rules.json in Firebase Console.";
        }
        return message;
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
