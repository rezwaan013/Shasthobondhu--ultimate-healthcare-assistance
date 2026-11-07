package com.shasthobondhu.app.ui.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentForgotPasswordBinding;
import com.shasthobondhu.app.util.ValidationUtils;
import com.shasthobondhu.app.viewmodel.ForgotPasswordViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/** Sends Firebase's secure, expiring password-reset email link. */
@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        binding.btnSendLink.setOnClickListener(v -> sendResetLink());
        binding.btnBack.setOnClickListener(v -> navigateBack());
        binding.btnBackToLogin.setOnClickListener(v -> navigateBack());

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = loading != null && loading;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.btnSendLink.setEnabled(!busy);
        });

        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null && !email.isEmpty()) {
                binding.tvDoneMessage.setText(getString(R.string.link_sent_message, email));
            }
        });

        viewModel.getLinkSent().observe(getViewLifecycleOwner(), sent -> {
            boolean complete = sent != null && sent;
            binding.groupEmail.setVisibility(complete ? View.GONE : View.VISIBLE);
            binding.groupDone.setVisibility(complete ? View.VISIBLE : View.GONE);
            binding.btnBack.setVisibility(complete ? View.GONE : View.VISIBLE);
            if (complete) {
                hideKeyboard();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) {
                binding.tvError.setVisibility(View.GONE);
                return;
            }
            binding.tvError.setVisibility(View.VISIBLE);
            binding.tvError.setText("INVALID_EMAIL".equals(error)
                    ? getString(R.string.err_invalid_email) : error);
        });
    }

    private void sendResetLink() {
        String email = text(binding.etEmail);
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.err_invalid_email));
            return;
        }
        binding.tilEmail.setError(null);
        viewModel.sendResetLink(email);
    }

    private void navigateBack() {
        if (!NavHostFragment.findNavController(this).popBackStack()) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_login);
        }
    }

    private void hideKeyboard() {
        View focused = requireActivity().getCurrentFocus();
        if (focused == null) {
            return;
        }
        InputMethodManager manager = requireContext().getSystemService(InputMethodManager.class);
        if (manager != null) {
            manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
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
