package com.shasthobondhu.app.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.local.dao.LocalUserDao;
import com.shasthobondhu.app.data.local.entities.LocalUserEntity;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.util.AppExecutors;
import com.shasthobondhu.app.util.PasswordHasher;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.constants.AppConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final LocalUserDao localUserDao;
    @SuppressLint("StaticFieldLeak") // Hilt supplies the process-wide application context.
    private final Context context;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoginSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRegistered = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> localMode = new MutableLiveData<>(false);

    @Inject
    public AuthViewModel(FirebaseService firebaseService, LocalUserDao localUserDao,
                         @ApplicationContext Context context) {
        this.auth = firebaseService.getAuth();
        this.db = firebaseService.getDb();
        this.localUserDao = localUserDao;
        this.context = context.getApplicationContext();
        this.localMode.setValue(SessionManager.isLocalMode(this.context));
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getAuthError() {
        return authError;
    }

    public LiveData<Boolean> isLoginSuccess() {
        return isLoginSuccess;
    }

    public LiveData<Boolean> isRegistered() {
        return isRegistered;
    }

    public LiveData<Boolean> isLocalMode() {
        return localMode;
    }

    public void loginWithEmail(String email, String password) {
        loading.setValue(true);
        authError.setValue(null);
        // Preserve seeded/offline accounts even after Firebase Email/Password is enabled.
        AppExecutors.io(() -> {
            LocalUserEntity localUser = localUserDao.findByEmail(email);
            if (localUser != null) {
                if (!PasswordHasher.matches(password, localUser.passwordSalt,
                        localUser.passwordHash)) {
                    AppExecutors.main(() -> {
                        loading.setValue(false);
                        authError.setValue(context.getString(R.string.error_wrong_password));
                    });
                    return;
                }
                AppExecutors.main(() -> {
                    SessionManager.signIn(context, localUser.uid, localUser.name,
                            localUser.email, localUser.phone, localUser.role, true);
                    localMode.setValue(true);
                    loading.setValue(false);
                    isLoginSuccess.setValue(true);
                });
                return;
            }
            AppExecutors.main(() -> loginWithFirebase(email, password));
        });
    }

    private void loginWithFirebase(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        completeFirebaseLogin(auth.getCurrentUser());
                    } else {
                        Exception error = task.getException();
                        if (isBackendUnavailable(error)) {
                            loginLocally(email, password);
                        } else {
                            loading.setValue(false);
                            authError.setValue(message(error, R.string.error_sign_in));
                        }
                    }
                });
    }

    public void register(String name, String email, String password, String phone, String role) {
        loading.setValue(true);
        authError.setValue(null);
        String safeRole = SessionManager.normalizeRole(role);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception error = task.getException();
                        if (isBackendUnavailable(error)) {
                            registerLocally(name, email, password, phone, safeRole);
                        } else {
                            loading.setValue(false);
                            authError.setValue(message(error, R.string.error_registration));
                        }
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        loading.setValue(false);
                        authError.setValue(context.getString(R.string.error_registration));
                        return;
                    }

                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    user.updateProfile(profile);

                    Map<String, Object> document = new HashMap<>();
                    document.put("uid", user.getUid());
                    document.put("name", name);
                    document.put("email", email);
                    document.put("phone", phone);
                    document.put("role", safeRole);
                    document.put("createdAt", System.currentTimeMillis());

                    SessionManager.cacheRole(context, email, safeRole);
                    db.collection(AppConstants.COLLECTION_USERS)
                            .document(user.getUid())
                            .set(document)
                            .addOnCompleteListener(saveTask -> {
                                // A failed profile save should not block a created account.
                                finishSignIn(user.getUid(), name, email, phone, safeRole, false);
                                isRegistered.setValue(true);
                            });
                });
    }

    public void logout() {
        auth.signOut();
        SessionManager.signOut(context);
        localMode.setValue(false);
    }

    public void clearError() {
        authError.setValue(null);
    }

    // ---------------------------------------------------------------------
    // Firebase completion
    // ---------------------------------------------------------------------

    private void completeFirebaseLogin(FirebaseUser user) {
        String email = user.getEmail() == null ? "" : user.getEmail();
        String cachedRole = SessionManager.getCachedRole(context, email);
        String cachedName = user.getDisplayName() == null || user.getDisplayName().isEmpty()
                ? email : user.getDisplayName();
        String cachedPhone = user.getPhoneNumber() == null ? "" : user.getPhoneNumber();

        db.collection(AppConstants.COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    String role = cachedRole;
                    String name = cachedName;
                    String phone = cachedPhone;
                    if (task.isSuccessful() && task.getResult() != null) {
                        String remoteRole = task.getResult().getString("role");
                        if (remoteRole != null && !remoteRole.isEmpty()) {
                            role = remoteRole;
                        }
                        String remoteName = task.getResult().getString("name");
                        if (remoteName != null && !remoteName.isEmpty()) {
                            name = remoteName;
                        }
                        String remotePhone = task.getResult().getString("phone");
                        if (remotePhone != null && !remotePhone.isEmpty()) {
                            phone = remotePhone;
                        }
                    }
                    finishSignIn(user.getUid(), name, email, phone, role, false);
                });
    }

    private void finishSignIn(String uid, String name, String email, String phone,
                              String role, boolean isLocal) {
        SessionManager.signIn(context, uid, name, email, phone, role, isLocal);
        SessionManager.cacheRole(context, email, role);
        localMode.setValue(isLocal);
        loading.setValue(false);
        isLoginSuccess.setValue(true);
    }

    // ---------------------------------------------------------------------
    // Offline fallback (Firebase Email/Password not enabled yet)
    // ---------------------------------------------------------------------

    private void registerLocally(String name, String email, String password,
                                 String phone, String role) {
        AppExecutors.io(() -> {
            LocalUserEntity existing = localUserDao.findByEmail(email);
            if (existing != null) {
                AppExecutors.main(() -> {
                    loading.setValue(false);
                    authError.setValue(context.getString(R.string.error_account_exists_local));
                });
                return;
            }

            String salt = PasswordHasher.newSalt();
            String hash = PasswordHasher.hash(password, salt);
            String uid = "local_" + UUID.randomUUID();
            localUserDao.insert(new LocalUserEntity(uid, name, email, phone, role, hash, salt));

            AppExecutors.main(() -> {
                SessionManager.signIn(context, uid, name, email, phone, role, true);
                SessionManager.cacheRole(context, email, role);
                localMode.setValue(true);
                loading.setValue(false);
                isRegistered.setValue(true);
                isLoginSuccess.setValue(true);
            });
        });
    }

    private void loginLocally(String email, String password) {
        AppExecutors.io(() -> {
            LocalUserEntity user = localUserDao.findByEmail(email);
            if (user == null) {
                AppExecutors.main(() -> {
                    loading.setValue(false);
                    authError.setValue(context.getString(R.string.error_no_local_account));
                });
                return;
            }
            if (!PasswordHasher.matches(password, user.passwordSalt, user.passwordHash)) {
                AppExecutors.main(() -> {
                    loading.setValue(false);
                    authError.setValue(context.getString(R.string.error_wrong_password));
                });
                return;
            }

            AppExecutors.main(() -> {
                SessionManager.signIn(context, user.uid, user.name, user.email, user.phone,
                        user.role, true);
                localMode.setValue(true);
                loading.setValue(false);
                isLoginSuccess.setValue(true);
            });
        });
    }

    private boolean isBackendUnavailable(Exception error) {
        if (error == null || error.getMessage() == null) {
            return false;
        }
        String message = error.getMessage();
        return message.contains("CONFIGURATION_NOT_FOUND")
                || message.contains("configuration not found")
                || message.contains("OPERATION_NOT_ALLOWED")
                || message.contains("API key not valid");
    }

    private String message(Exception error, int fallbackRes) {
        String text = error == null ? null : error.getMessage();
        if (text == null || text.trim().isEmpty()) {
            return context.getString(fallbackRes);
        }
        if (text.contains("CONFIGURATION_NOT_FOUND")) {
            return context.getString(R.string.error_firebase_not_configured);
        }
        return text;
    }
}
