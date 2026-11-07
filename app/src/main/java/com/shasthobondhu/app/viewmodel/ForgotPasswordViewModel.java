package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.repository.PasswordResetRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ForgotPasswordViewModel extends ViewModel {

    private final PasswordResetRepository repository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> linkSent = new MutableLiveData<>(false);

    @Inject
    public ForgotPasswordViewModel(PasswordResetRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public LiveData<Boolean> getLinkSent() {
        return linkSent;
    }

    public void sendResetLink(String address) {
        loading.setValue(true);
        error.setValue(null);
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
                error.postValue(message);
            }
        });
    }
}
