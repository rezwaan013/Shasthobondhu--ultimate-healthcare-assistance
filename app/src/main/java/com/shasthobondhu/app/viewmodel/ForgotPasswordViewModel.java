package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.repository.PasswordResetRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Drives the three step forgot-password flow:
 * EMAIL → CODE → DONE.
 */
@HiltViewModel
public class ForgotPasswordViewModel extends ViewModel {

    public enum Step {EMAIL, CODE, DONE}

    private final PasswordResetRepository repository;

    private final MutableLiveData<Step> step = new MutableLiveData<>(Step.EMAIL);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorCode = new MutableLiveData<>();
    private final MutableLiveData<String> demoCode = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> linkSent = new MutableLiveData<>(false);

    @Inject
    public ForgotPasswordViewModel(PasswordResetRepository repository) {
        this.repository = repository;
    }

    public LiveData<Step> getStep() {
        return step;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorCode() {
        return errorCode;
    }

    public LiveData<String> getDemoCode() {
        return demoCode;
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<Boolean> getLinkSent() {
        return linkSent;
    }

    public boolean isBackendEnabled() {
        return repository.isBackendEnabled();
    }

    public void sendCode(String address) {
        loading.setValue(true);
        errorCode.setValue(null);
        demoCode.setValue(null);
        linkSent.setValue(false);
        repository.sendCode(address, new PasswordResetRepository.ResultCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                email.postValue(address.trim());
                if (message != null && message.startsWith(PasswordResetRepository.DEMO_CODE_PREFIX)) {
                    String payload = message.substring(PasswordResetRepository.DEMO_CODE_PREFIX.length());
                    if ("LINK".equals(payload)) {
                        linkSent.postValue(true);
                        step.postValue(Step.DONE);
                        return;
                    }
                    demoCode.postValue(payload);
                }
                step.postValue(Step.CODE);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                errorCode.postValue(message);
            }
        });
    }

    public void resendCode() {
        String address = email.getValue();
        if (address == null || address.isEmpty()) {
            step.setValue(Step.EMAIL);
            return;
        }
        sendCode(address);
    }

    public void resetPassword(String code, String newPassword) {
        String address = email.getValue();
        if (address == null || address.isEmpty()) {
            step.setValue(Step.EMAIL);
            return;
        }
        loading.setValue(true);
        errorCode.setValue(null);
        repository.resetPassword(address, code, newPassword,
                new PasswordResetRepository.ResultCallback() {
                    @Override
                    public void onSuccess(String message) {
                        loading.postValue(false);
                        if (message != null
                                && message.startsWith(PasswordResetRepository.DEMO_CODE_PREFIX)) {
                            linkSent.postValue(true);
                            step.postValue(Step.DONE);
                        } else {
                            step.postValue(Step.DONE);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
                        errorCode.postValue(message);
                    }
                });
    }

    public void sendResetLink(String address) {
        loading.setValue(true);
        errorCode.setValue(null);
        repository.sendResetLink(address, new PasswordResetRepository.ResultCallback() {
            @Override
            public void onSuccess(String message) {
                loading.postValue(false);
                email.postValue(address.trim());
                linkSent.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                errorCode.postValue(message);
            }
        });
    }

    public void backToEmailStep() {
        step.setValue(Step.EMAIL);
        demoCode.setValue(null);
        errorCode.setValue(null);
    }

    public void clearError() {
        errorCode.setValue(null);
    }
}
