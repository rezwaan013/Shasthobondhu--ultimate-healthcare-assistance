package com.shasthobondhu.app.ui.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.shasthobondhu.app.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shasthobondhu.app.databinding.FragmentRegistrationBinding;
import com.shasthobondhu.app.ui.fragments.donors.DonorRegisterFragment;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.ValidationUtils;
import com.shasthobondhu.app.util.constants.AppConstants;
import com.shasthobondhu.app.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private AuthViewModel viewModel;
    private AlertDialog verificationDialog;

    private String selectedRole = AppConstants.ROLE_USER;

    private final String[] roleLabels = new String[3];
    private final String[] roleValues = new String[]{
            AppConstants.ROLE_USER,
            AppConstants.ROLE_PHARMACY,
            AppConstants.ROLE_HOSPITAL
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupRoleDropdown();

        binding.btnRegister.setOnClickListener(v -> submit());
        binding.btnResendVerification.setOnClickListener(v ->
                viewModel.resendVerificationEmail());
        binding.tvLogin.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = loading != null && loading;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!busy);
            binding.btnResendVerification.setEnabled(!busy);
        });

        viewModel.getAuthError().observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) {
                binding.tvError.setVisibility(View.GONE);
            } else {
                binding.tvError.setVisibility(View.VISIBLE);
                binding.tvError.setText(error);
            }
        });

        viewModel.isRegistered().observe(getViewLifecycleOwner(), registered -> {
            if (registered != null && registered && isAdded()
                    && SessionManager.isLocalMode(requireContext())) {
                navigateAfterRegistration();
            }
        });

        viewModel.getCanResendVerification().observe(getViewLifecycleOwner(), canResend ->
                binding.btnResendVerification.setVisibility(
                        Boolean.TRUE.equals(canResend) ? View.VISIBLE : View.GONE));

        viewModel.getVerificationEmailSent().observe(getViewLifecycleOwner(), email -> {
            if (email != null && isAdded()) {
                showVerificationSent(email);
            }
        });
    }

    private void showVerificationSent(String email) {
        if (verificationDialog != null && verificationDialog.isShowing()) {
            return;
        }
        verificationDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.verification_email_sent_title)
                .setMessage(getString(R.string.verification_email_sent_message, email))
                .setCancelable(false)
                .setPositiveButton(R.string.action_back_to_login, (dialog, which) -> {
                    viewModel.consumeVerificationNotice();
                    if (!NavHostFragment.findNavController(this)
                            .popBackStack(R.id.nav_login, false)) {
                        NavHostFragment.findNavController(this).navigate(R.id.nav_login);
                    }
                })
                .create();
        verificationDialog.setOnDismissListener(dialog -> verificationDialog = null);
        verificationDialog.show();
    }

    private void setupRoleDropdown() {
        roleLabels[0] = getString(R.string.role_user);
        roleLabels[1] = getString(R.string.role_pharmacy);
        roleLabels[2] = getString(R.string.role_hospital);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, roleLabels);
        binding.spinnerRole.setAdapter(adapter);

        String requestedRole = AppConstants.ROLE_USER;
        int selectedIndex = 0;
        for (int i = 0; i < roleValues.length; i++) {
            if (roleValues[i].equals(requestedRole)) {
                selectedIndex = i;
                break;
            }
        }
        selectedRole = roleValues[selectedIndex];
        binding.spinnerRole.setText(roleLabels[selectedIndex], false);

        binding.spinnerRole.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < roleValues.length) {
                selectedRole = roleValues[position];
            }
        });
    }

    private void submit() {
        String name = text(binding.etFullName);
        String phone = text(binding.etPhone);
        String email = text(binding.etEmail);
        String password = text(binding.etPassword);

        binding.tvError.setVisibility(View.GONE);

        if (name.isEmpty()) {
            showError(getString(R.string.hint_full_name));
            return;
        }
        if (phone.length() < 11) {
            showError(getString(R.string.hint_phone));
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showError(getString(R.string.err_invalid_email));
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            showError(getString(R.string.err_invalid_password));
            return;
        }

        viewModel.register(name, email, password, phone, selectedRole);
    }

    private void navigateAfterRegistration() {
        String role = SessionManager.getRole(requireContext());
        if (AppConstants.ROLE_PHARMACY.equals(role)) {
            navigateAfterAuth(R.id.nav_pharmacy_dashboard, null);
        } else if (AppConstants.ROLE_HOSPITAL.equals(role)) {
            navigateAfterAuth(R.id.nav_hospital_dashboard, null);
        } else {
            Bundle args = new Bundle();
            args.putBoolean(DonorRegisterFragment.ARG_ONBOARDING, true);
            navigateAfterAuth(R.id.nav_donor_register, args);
        }
    }

    private void navigateAfterAuth(int destination, Bundle args) {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_register, true)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(destination, args, options);
    }

    private void showError(String message) {
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
    }

    private String text(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        if (verificationDialog != null) {
            verificationDialog.dismiss();
            verificationDialog = null;
        }
        super.onDestroyView();
        binding = null;
    }
}
