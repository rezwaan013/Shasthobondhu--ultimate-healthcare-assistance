package com.shasthobondhu.app.ui.fragments.auth.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.shasthobondhu.app.databinding.FragmentLoginBinding;
import com.shasthobondhu.app.viewmodel.AuthViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private String mVerificationId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnSendOtp.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(getContext(), "Please enter phone number", Toast.LENGTH_SHORT).show();
                return;
            }
            
            viewModel.sendOtp(phone, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // Auto-sign in
                    viewModel.verifyOtp("000000", mVerificationId); // Mock or real
                }

                @Override
                public void onVerificationFailed(com.google.firebase.auth.FirebaseException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    mVerificationId = verificationId;
                    Toast.makeText(getContext(), "OTP Sent!", Toast.LENGTH_SHORT).show();
                    // Navigate to OTP verify fragment here
                }
            });
        });

        return binding.getRoot();
    }
}
