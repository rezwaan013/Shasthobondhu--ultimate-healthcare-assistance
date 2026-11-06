package com.shasthobondhu.app.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.shasthobondhu.app.BuildConfig;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.data.remote.PasswordResetApi;
import com.shasthobondhu.app.data.remote.model.ResetPasswordRequest;
import com.shasthobondhu.app.data.remote.model.SendCodeRequest;
import com.shasthobondhu.app.data.remote.model.SimpleResponse;
import com.shasthobondhu.app.util.ValidationUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles the "forgot password" flow.
 *
 * <p>Two modes are supported:</p>
 * <ul>
 *     <li><b>Server mode</b> – {@code PASSWORD_RESET_BASE_URL} is configured, so the
 *     trusted backend emails the 6-digit code and performs the password change with
 *     the Firebase Admin SDK.</li>
 *     <li><b>Demo mode</b> – no backend configured. A code is generated locally so the
 *     UI can be exercised; the code is surfaced to the caller and logged for debugging.
 *     A Firebase reset <i>link</i> is always available as a real, working fallback.</li>
 * </ul>
 */
@Singleton
public class PasswordResetRepository {

    private static final String TAG = "PasswordResetRepo";
    private static final long CODE_TTL_MS = 10 * 60 * 1000L;
    private static final int MAX_ATTEMPTS = 5;
    public static final String DEMO_CODE_PREFIX = "DEMO_CODE:";

    public interface ResultCallback {
        void onSuccess(String message);

        void onError(String message);
    }

    private final PasswordResetApi api;
    private final FirebaseAuth auth;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SecureRandom random = new SecureRandom();

    // Demo mode state only – never used when the backend is configured.
    private String pendingEmail;
    private String pendingHash;
    private long expiresAt;
    private int attempts;

    @Inject
    public PasswordResetRepository(PasswordResetApi api, FirebaseService firebaseService) {
        this.api = api;
        this.auth = firebaseService.getAuth();
    }

    public boolean isBackendEnabled() {
        String url = BuildConfig.PASSWORD_RESET_BASE_URL;
        return url != null && !url.trim().isEmpty();
    }

    public void sendCode(String rawEmail, @NonNull ResultCallback callback) {
        String email = normalize(rawEmail);
        if (!ValidationUtils.isValidEmail(email)) {
            callback.onError("INVALID_EMAIL");
            return;
        }

        if (isBackendEnabled()) {
            api.sendCode(new SendCodeRequest(email)).enqueue(new Callback<SimpleResponse>() {
                @Override
                public void onResponse(@NonNull Call<SimpleResponse> call,
                                       @NonNull Response<SimpleResponse> response) {
                    SimpleResponse body = response.body();
                    if (response.isSuccessful() && body != null && body.isSuccess()) {
                        callback.onSuccess(body.getMessage() != null
                                ? body.getMessage() : "Code sent");
                    } else {
                        callback.onError(body != null && body.getMessage() != null
                                ? body.getMessage() : "Could not send the code");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SimpleResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "sendCode failed", t);
                    callback.onError("Network error. Check your connection.");
                }
            });
            return;
        }

        // Demo mode: generate locally so the UX can be tested end to end.
        ioExecutor.execute(() -> {
            String code = generateCode();
            pendingEmail = email;
            pendingHash = sha256(email + ":" + code);
            expiresAt = System.currentTimeMillis() + CODE_TTL_MS;
            attempts = 0;
            Log.d(TAG, "Demo verification code for " + email + " is " + code);
            mainHandler.post(() -> callback.onSuccess(DEMO_CODE_PREFIX + code));
        });
    }

    public void resetPassword(String rawEmail, String rawCode, String newPassword,
                              @NonNull ResultCallback callback) {
        String email = normalize(rawEmail);
        String code = rawCode == null ? "" : rawCode.trim();

        if (!ValidationUtils.isValidEmail(email)) {
            callback.onError("INVALID_EMAIL");
            return;
        }
        if (!ValidationUtils.isSixDigitCode(code)) {
            callback.onError("INVALID_CODE");
            return;
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            callback.onError("WEAK_PASSWORD");
            return;
        }

        if (isBackendEnabled()) {
            api.resetPassword(new ResetPasswordRequest(email, code, newPassword))
                    .enqueue(new Callback<SimpleResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<SimpleResponse> call,
                                               @NonNull Response<SimpleResponse> response) {
                            SimpleResponse body = response.body();
                            if (response.isSuccessful() && body != null && body.isSuccess()) {
                                callback.onSuccess(body.getMessage() != null
                                        ? body.getMessage() : "Password updated");
                            } else {
                                callback.onError(body != null && body.getMessage() != null
                                        ? body.getMessage() : "Could not reset the password");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<SimpleResponse> call,
                                              @NonNull Throwable t) {
                            Log.e(TAG, "resetPassword failed", t);
                            callback.onError("Network error. Check your connection.");
                        }
                    });
            return;
        }

        ioExecutor.execute(() -> {
            String error = verifyDemoCode(email, code);
            if (error != null) {
                mainHandler.post(() -> callback.onError(error));
                return;
            }
            // Consume the code so it cannot be replayed.
            pendingEmail = null;
            pendingHash = null;
            expiresAt = 0L;
            // Code is valid. Without a backend we cannot change another user's password,
            // so fall back to Firebase's secure, verified reset link (a real email).
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mainHandler.post(() ->
                                    callback.onSuccess(DEMO_CODE_PREFIX + "LINK"));
                        } else {
                            mainHandler.post(() -> callback.onError(
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Could not send the reset link"));
                        }
                    });
        });
    }

    /** Always-available real fallback: Firebase sends a secure password reset link. */
    public void sendResetLink(String rawEmail, @NonNull ResultCallback callback) {
        String email = normalize(rawEmail);
        if (!ValidationUtils.isValidEmail(email)) {
            callback.onError("INVALID_EMAIL");
            return;
        }
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("LINK_SENT");
                    } else {
                        callback.onError(task.getException() != null
                                ? task.getException().getMessage()
                                : "Could not send the reset link");
                    }
                });
    }

    private String verifyDemoCode(String email, String code) {
        if (pendingEmail == null || !pendingEmail.equals(email)) {
            return "WRONG_CODE";
        }
        if (System.currentTimeMillis() > expiresAt) {
            return "CODE_EXPIRED";
        }
        if (attempts >= MAX_ATTEMPTS) {
            return "TOO_MANY_ATTEMPTS";
        }
        attempts++;
        String hash = sha256(email + ":" + code);
        if (pendingHash != null && pendingHash.equals(hash)) {
            return null;
        }
        return attempts >= MAX_ATTEMPTS ? "TOO_MANY_ATTEMPTS" : "WRONG_CODE";
    }

    private String generateCode() {
        int value = 100000 + random.nextInt(900000);
        return String.valueOf(value);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format(Locale.US, "%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }
}
