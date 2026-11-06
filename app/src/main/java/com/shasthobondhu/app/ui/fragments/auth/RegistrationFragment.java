package com.shasthobondhu.app.ui.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentRegistrationBinding;
import com.shasthobondhu.app.util.ValidationUtils;
import com.shasthobondhu.app.util.constants.AppConstants;
import com.shasthobondhu.app.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private AuthViewModel viewModel;

    private final String[] roleLabels = new String[]{};
    private String selectedRole = AppConstants.ROLE_USER;

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
        binding.tvLogin.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = loading != null && loading;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!busy);
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
            if (registered != null && registered && isAdded()) {
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build();
                NavHostFragment.findNavController(this).navigate(R.id.nav_home, null, options);
            }
        });
    }

    private void setupRoleDropdown() {
        String[] labels = new String[]{
                getString(R.string.role_user),
                getString(R.string.role_pharmacy)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, labels);
        binding.spinnerRole.setAdapter(adapter);
        binding.spinnerRole.setText(labels[0], false);
        selectedRole = AppConstants.ROLE_USER;

        binding.spinnerRole.setOnItemClickListener((parent, view, position, id) ->
                selectedRole = position == 1 ? AppConstants.ROLE_PHARMACY : AppConstants.ROLE_USER);
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

    private void showError(String message) {
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(message);
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
