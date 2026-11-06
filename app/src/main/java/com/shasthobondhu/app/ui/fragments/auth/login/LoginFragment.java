package com.shasthobondhu.app.ui.fragments.auth.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentLoginBinding;
import com.shasthobondhu.app.util.ValidationUtils;
import com.shasthobondhu.app.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            boolean phoneMode = checkedId == R.id.btnPhoneTab;
            binding.groupPhone.setVisibility(phoneMode ? View.VISIBLE : View.GONE);
            binding.groupEmail.setVisibility(phoneMode ? View.GONE : View.VISIBLE);
            hideError();
        });

        binding.btnSendOtp.setOnClickListener(v -> sendOtp());
        binding.btnEmailLogin.setOnClickListener(v -> loginWithEmail());
        binding.tvForgotPassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_forgot_password));
        binding.tvRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_register));

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = loading != null && loading;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.btnSendOtp.setEnabled(!busy);
            binding.btnEmailLogin.setEnabled(!busy);
        });

        viewModel.getAuthError().observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) {
                hideError();
            } else {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(error);
            }
        });

        viewModel.isLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                navigateHome();
            }
        });
    }

    private void sendOtp() {
        String phone = text(binding.etPhone);
        if (phone.length() < 11) {
            showError(getString(R.string.hint_phone));
            return;
        }
        hideError();
        viewModel.sendOtp(requireActivity(), phone,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        viewModel.signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.auth.FirebaseException e) {
                        showError(e.getMessage() != null ? e.getMessage() : getString(R.string.error_generic));
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(requireContext(), R.string.otp_sent, Toast.LENGTH_SHORT).show();
                        Bundle args = new Bundle();
                        args.putString(OtpVerifyFragment.ARG_VERIFICATION_ID, verificationId);
                        args.putString(OtpVerifyFragment.ARG_PHONE, phone);
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.nav_otp_verify, args);
                    }
                });
    }

    private void loginWithEmail() {
        String email = text(binding.etEmail);
        String password = text(binding.etPassword);
        if (!ValidationUtils.isValidEmail(email)) {
            showError(getString(R.string.err_invalid_email));
            return;
        }
        if (password.isEmpty()) {
            showError(getString(R.string.err_invalid_password));
            return;
        }
        hideError();
        viewModel.loginWithEmail(email, password);
    }

    private void navigateHome() {
        if (!isAdded()) {
            return;
        }
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_login, true)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.nav_home, null, options);
    }

    private void showError(String message) {
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
    }

    private void hideError() {
        binding.tvError.setVisibility(View.GONE);
    }

    private String text(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
