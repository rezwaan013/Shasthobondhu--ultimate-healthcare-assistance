package com.shasthobondhu.app.viewmodel;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.util.constants.AppConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoginSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRegistered = new MutableLiveData<>(false);

    @Inject
    public AuthViewModel(FirebaseService firebaseService) {
        this.auth = firebaseService.getAuth();
        this.db = firebaseService.getDb();
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

    /** Sends a phone OTP. An Activity is required by Firebase for reCAPTCHA fallback. */
    public void sendOtp(@NonNull Activity activity, String phone,
                        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        loading.setValue(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /** Used when Firebase verifies the number instantly (no code entry required). */
    public void signInWithCredential(PhoneAuthCredential credential) {
        loading.setValue(true);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    loading.setValue(false);
                    if (task.isSuccessful()) {
                        isLoginSuccess.setValue(true);
                    } else {
                        authError.setValue(task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign in failed");
                    }
                });
    }

    public void verifyOtp(String otp, String verificationId) {
        if (verificationId == null || verificationId.isEmpty()) {
            authError.setValue("Verification session expired. Please request a new code.");
            return;
        }
        loading.setValue(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    loading.setValue(false);
                    if (task.isSuccessful()) {
                        isLoginSuccess.setValue(true);
                    } else {
                        authError.setValue(task.getException() != null
                                ? task.getException().getMessage()
                                : "Invalid OTP");
                    }
                });
    }

    public void loginWithEmail(String email, String password) {
        loading.setValue(true);
        authError.setValue(null);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loading.setValue(false);
                    if (task.isSuccessful()) {
                        isLoginSuccess.setValue(true);
                    } else {
                        authError.setValue(task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign in failed");
                    }
                });
    }

    public void register(String name, String email, String password, String phone, String role) {
        loading.setValue(true);
        authError.setValue(null);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        loading.setValue(false);
                        authError.setValue(task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed");
                        return;
                    }
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        loading.setValue(false);
                        authError.setValue("Registration failed");
                        return;
                    }

                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    Map<String, Object> document = new HashMap<>();
                    document.put("uid", user.getUid());
                    document.put("name", name);
                    document.put("email", email);
                    document.put("phone", phone);
                    document.put("role", role);
                    document.put("createdAt", System.currentTimeMillis());

                    user.updateProfile(profile);
                    db.collection(AppConstants.COLLECTION_USERS)
                            .document(user.getUid())
                            .set(document)
                            .addOnCompleteListener(saveTask -> {
                                loading.setValue(false);
                                if (saveTask.isSuccessful()) {
                                    isRegistered.setValue(true);
                                    isLoginSuccess.setValue(true);
                                } else {
                                    authError.setValue(saveTask.getException() != null
                                            ? saveTask.getException().getMessage()
                                            : "Could not save your profile");
                                }
                            });
                });
    }

    public void clearError() {
        authError.setValue(null);
    }
}
