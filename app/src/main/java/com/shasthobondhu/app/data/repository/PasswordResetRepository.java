package com.shasthobondhu.app.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.util.ValidationUtils;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

/** Uses Firebase Authentication's secure, expiring hosted reset link. */
@Singleton
public class PasswordResetRepository {

    private final FirebaseAuth auth;

    @Inject
    public PasswordResetRepository(FirebaseService firebaseService) {
        auth = firebaseService.getAuth();
    }

    public void sendResetLink(String rawEmail, @NonNull ResultCallback callback) {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!ValidationUtils.isValidEmail(email)) {
            callback.onError("INVALID_EMAIL");
            return;
        }
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess("LINK_SENT");
            } else {
                callback.onError(task.getException() != null
                        && task.getException().getMessage() != null
                        ? task.getException().getMessage()
                        : "Could not send the password reset email.");
            }
        });
    }

    public interface ResultCallback {
        void onSuccess(String message);

        void onError(String message);
    }
}
