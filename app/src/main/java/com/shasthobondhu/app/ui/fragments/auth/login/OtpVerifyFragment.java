package com.shasthobondhu.app.ui.fragments.auth.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentOtpVerifyBinding;
import com.shasthobondhu.app.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpVerifyFragment extends Fragment {

    public static final String ARG_VERIFICATION_ID = "verificationId";
    public static final String ARG_PHONE = "phone";

    private FragmentOtpVerifyBinding binding;
    private AuthViewModel viewModel;
    private String verificationId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpVerifyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        if (getArguments() != null) {
            verificationId = getArguments().getString(ARG_VERIFICATION_ID);
            String phone = getArguments().getString(ARG_PHONE);
            if (phone != null) {
                binding.tvSubtitle.setText(getString(R.string.otp_subtitle) + "\n" + phone);
            }
        }

        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otp = binding.etOtp.getText() == null
                    ? "" : binding.etOtp.getText().toString().trim();
            if (otp.length() != 6) {
                showError(getString(R.string.err_invalid_code));
                return;
            }
            hideError();
            viewModel.verifyOtp(otp, verificationId);
        });

        viewModel.isLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success && isAdded()) {
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_login, true)
                        .setLaunchSingleTop(true)
                        .build();
                NavHostFragment.findNavController(this).navigate(R.id.nav_home, null, options);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progress.setVisibility(loading != null && loading
                        ? View.VISIBLE : View.GONE));

        viewModel.getAuthError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }

    private void showError(String message) {
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
    }

    private void hideError() {
        binding.tvError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
