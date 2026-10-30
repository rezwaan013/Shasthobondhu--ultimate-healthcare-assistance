package com.shasthobondhu.app.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.shasthobondhu.app.data.remote.FirebaseService;
import java.util.concurrent.Tasks;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private final MutableLiveData<Boolean> isOtpSent = new MutableLiveData<>(false);
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoginSuccess = new MutableLiveData<>(false);
    private String verificationId;

    @Inject
    public AuthViewModel(FirebaseService firebaseService) {
        this.auth = firebaseService.getAuth();
    }

    public LiveData<Boolean> isOtpSent() { return isOtpSent; }
    public LiveData<String> getAuthError() { return authError; }
    public LiveData<Boolean> isLoginSuccess() { return isLoginSuccess; }

    public void sendOtp(String phone, PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60000L, java.util.concurrent.TimeUnit.SECONDS)
                .setActivity(null) // Will be set in Fragment
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verifyOtp(String otp, String verificationId) {
        this.verificationId = verificationId;
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isLoginSuccess.setValue(true);
                    } else {
                        authError.setValue(task.getException().getMessage());
                    }
                });
    }
}
