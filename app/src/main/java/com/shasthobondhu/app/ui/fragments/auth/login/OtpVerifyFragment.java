package com.shasthobondhu.app.ui.fragments.auth.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.shasthobondhu.app.databinding.FragmentOtpVerifyBinding;
import com.shasthobondhu.app.viewmodel.AuthViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpVerifyFragment extends Fragment {
    private FragmentOtpVerifyBinding binding;
    private AuthViewModel viewModel;
    private String verificationId; // Passed from LoginFragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOtpVerifyBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otp = binding.etOtp.getText().toString().trim();
            if (otp.length() < 6) {
                Toast.makeText(getContext(), "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.verifyOtp(otp, verificationId);
        });

        viewModel.isLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_LONG).show();
                // Navigate to MainActivity/Home
            }
        });

        viewModel.getAuthError().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
        });

        return binding.getRoot();
    }
}
