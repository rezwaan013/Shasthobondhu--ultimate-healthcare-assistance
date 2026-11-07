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

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentLoginBinding;
import com.shasthobondhu.app.ui.fragments.home.HomeFragment;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.ValidationUtils;
import com.shasthobondhu.app.util.constants.AppConstants;
import com.shasthobondhu.app.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private String expectedRole;

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

        expectedRole = getArguments() == null
                ? null : getArguments().getString(HomeFragment.ARG_OWNER_ROLE);

        configureOwnerMode();

        binding.btnEmailLogin.setOnClickListener(v -> loginWithEmail());
        binding.tvForgotPassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_forgot_password));
        binding.tvRegister.setOnClickListener(v -> openRegister());

        observeViewModel();
    }

    private void configureOwnerMode() {
        if (expectedRole == null) {
            return;
        }
        String label = SessionManager.roleLabel(requireContext(), expectedRole);
        binding.tvOwnerNotice.setVisibility(View.VISIBLE);
        binding.tvOwnerNotice.setText(getString(R.string.owner_role_notice, label));
        binding.tvDemoAccounts.setVisibility(View.VISIBLE);
        binding.tvDemoAccounts.setText(getString(R.string.demo_accounts_title)
                + "\n" + getString(R.string.demo_accounts_note));
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = loading != null && loading;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
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
                navigateAfterLogin();
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

    private void navigateAfterLogin() {
        if (!isAdded()) {
            return;
        }
        String role = SessionManager.getRole(requireContext());
        if (expectedRole != null && !expectedRole.equals(role)) {
            if (AppConstants.ROLE_PHARMACY.equals(role)
                    || AppConstants.ROLE_HOSPITAL.equals(role)) {
                Toast.makeText(requireContext(),
                        getString(R.string.owner_role_changed,
                                SessionManager.roleLabel(requireContext(), role)),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), R.string.error_owner_only,
                        Toast.LENGTH_LONG).show();
            }
        }

        int destination;
        if (AppConstants.ROLE_PHARMACY.equals(role)) {
            destination = R.id.nav_pharmacy_dashboard;
        } else if (AppConstants.ROLE_HOSPITAL.equals(role)) {
            destination = R.id.nav_hospital_dashboard;
        } else {
            destination = R.id.nav_home;
        }

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_login, true)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(destination, null, options);
    }

    private void openRegister() {
        Bundle args = new Bundle();
        if (expectedRole != null) {
            args.putString(HomeFragment.ARG_OWNER_ROLE, expectedRole);
        }
        NavHostFragment.findNavController(this).navigate(R.id.nav_register, args);
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
