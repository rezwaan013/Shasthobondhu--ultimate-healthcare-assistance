package com.shasthobondhu.app.ui.fragments.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
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

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    private static final long RESEND_SECONDS = 60L;

    private FragmentForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;
    private CountDownTimer resendTimer;
    private String lastEmail = "";

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

        binding.btnSendCode.setOnClickListener(v -> submitEmail());
        binding.btnSendLink.setOnClickListener(v -> submitResetLink());
        binding.btnReset.setOnClickListener(v -> submitReset());
        binding.btnResend.setOnClickListener(v -> {
            viewModel.resendCode();
            startResendTimer();
        });
        binding.btnBack.setOnClickListener(v -> handleBack());
        binding.btnBackToLogin.setOnClickListener(v -> navigateToLogin());

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBack();
                    }
                });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getStep().observe(getViewLifecycleOwner(), step -> {
            if (step == null) {
                return;
            }
            binding.groupEmail.setVisibility(step == ForgotPasswordViewModel.Step.EMAIL
                    ? View.VISIBLE : View.GONE);
            binding.groupReset.setVisibility(step == ForgotPasswordViewModel.Step.CODE
                    ? View.VISIBLE : View.GONE);
            binding.groupDone.setVisibility(step == ForgotPasswordViewModel.Step.DONE
                    ? View.VISIBLE : View.GONE);
            binding.btnBack.setVisibility(step == ForgotPasswordViewModel.Step.DONE
                    ? View.GONE : View.VISIBLE);

            if (step == ForgotPasswordViewModel.Step.CODE) {
                startResendTimer();
                hideKeyboard();
            }
            if (step == ForgotPasswordViewModel.Step.DONE) {
                cancelResendTimer();
                Boolean linkSent = viewModel.getLinkSent().getValue();
                binding.tvDoneMessage.setText(linkSent != null && linkSent
                        ? getString(R.string.link_sent_message, lastEmail)
                        : getString(R.string.reset_success_message));
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = loading != null && loading;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.btnSendCode.setEnabled(!busy);
            binding.btnSendLink.setEnabled(!busy);
            binding.btnReset.setEnabled(!busy);
        });

        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email == null || email.isEmpty()) {
                return;
            }
            lastEmail = email;
            binding.tvCodeSent.setText(getString(R.string.code_sent_message, email));
        });

        viewModel.getDemoCode().observe(getViewLifecycleOwner(), code -> {
            if (code == null || code.isEmpty()) {
                binding.tvDemoCode.setVisibility(View.GONE);
                return;
            }
            binding.tvDemoCode.setVisibility(View.VISIBLE);
            binding.tvDemoCode.setText(getString(R.string.demo_code_label) + "\n" + code);
        });

        viewModel.getErrorCode().observe(getViewLifecycleOwner(), this::showError);
    }

    private void submitEmail() {
        String email = text(binding.etEmail);
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.err_invalid_email));
            return;
        }
        binding.tilEmail.setError(null);
        viewModel.sendCode(email);
    }

    private void submitResetLink() {
        String email = text(binding.etEmail);
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.err_invalid_email));
            return;
        }
        binding.tilEmail.setError(null);
        viewModel.sendResetLink(email);
    }

    private void submitReset() {
        String code = text(binding.etCode);
        String newPassword = text(binding.etNewPassword);
        String confirmPassword = text(binding.etConfirmPassword);

        binding.tilCode.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        if (!ValidationUtils.isSixDigitCode(code)) {
            binding.tilCode.setError(getString(R.string.err_invalid_code));
            return;
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.err_invalid_password));
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.err_password_mismatch));
            return;
        }
        hideKeyboard();
        viewModel.resetPassword(code, newPassword);
    }

    private void showError(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            binding.tvError.setVisibility(View.GONE);
            return;
        }
        binding.tvError.setVisibility(View.VISIBLE);
        binding.tvError.setText(mapError(errorCode));
    }

    private String mapError(String errorCode) {
        switch (errorCode) {
            case "INVALID_EMAIL":
                return getString(R.string.err_invalid_email);
            case "WEAK_PASSWORD":
                return getString(R.string.err_invalid_password);
            case "INVALID_CODE":
                return getString(R.string.err_invalid_code);
            case "WRONG_CODE":
            case "CODE_EXPIRED":
                return getString(R.string.err_wrong_code);
            case "TOO_MANY_ATTEMPTS":
                return getString(R.string.err_too_many_attempts);
            default:
                return errorCode;
        }
    }

    private void handleBack() {
        ForgotPasswordViewModel.Step step = viewModel.getStep().getValue();
        if (step == ForgotPasswordViewModel.Step.CODE) {
            viewModel.backToEmailStep();
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        if (!isAdded()) {
            return;
        }
        androidx.navigation.NavController controller = NavHostFragment.findNavController(this);
        if (!controller.popBackStack()) {
            controller.navigate(R.id.nav_login);
        }
    }

    private void startResendTimer() {
        cancelResendTimer();
        binding.btnResend.setEnabled(false);
        resendTimer = new CountDownTimer(RESEND_SECONDS * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (binding == null) {
                    return;
                }
                binding.btnResend.setText(
                        getString(R.string.resend_in, millisUntilFinished / 1000L));
            }

            @Override
            public void onFinish() {
                if (binding == null) {
                    return;
                }
                binding.btnResend.setEnabled(true);
                binding.btnResend.setText(R.string.resend_code);
            }
        }.start();
    }

    private void cancelResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
            resendTimer = null;
        }
    }

    private void hideKeyboard() {
        View view = getActivity() != null ? getActivity().getCurrentFocus() : null;
        if (view != null) {
            InputMethodManager imm = requireContext()
                    .getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private String text(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        cancelResendTimer();
        super.onDestroyView();
        binding = null;
    }
}
