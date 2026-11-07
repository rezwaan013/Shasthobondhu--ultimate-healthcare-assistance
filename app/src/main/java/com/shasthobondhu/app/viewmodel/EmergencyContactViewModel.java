package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.model.EmergencyContact;
import com.shasthobondhu.app.data.repository.EmergencyContactRepository;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EmergencyContactViewModel extends ViewModel {

    private final EmergencyContactRepository repository;
    private final MutableLiveData<EmergencyContact> selectedContact = new MutableLiveData<>();
    private final MutableLiveData<List<EmergencyContact>> contacts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loadFailed = new MutableLiveData<>(false);

    @Inject
    public EmergencyContactViewModel(EmergencyContactRepository repository) {
        this.repository = repository;
        selectedContact.setValue(repository.getSavedContact());
    }

    public LiveData<EmergencyContact> getSelectedContact() {
        return selectedContact;
    }

    public LiveData<List<EmergencyContact>> getContacts() {
        return contacts;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoadFailed() {
        return loadFailed;
    }

    public void loadContacts() {
        loading.setValue(true);
        loadFailed.setValue(false);
        repository.loadPhoneContacts(new EmergencyContactRepository.Callback() {
            @Override
            public void onLoaded(List<EmergencyContact> result) {
                loading.setValue(false);
                contacts.setValue(result == null ? Collections.emptyList() : result);
            }

            @Override
            public void onError() {
                loading.setValue(false);
                loadFailed.setValue(true);
                contacts.setValue(Collections.emptyList());
            }
        });
    }

    public void select(EmergencyContact contact) {
        repository.saveContact(contact);
        selectedContact.setValue(contact);
    }
}
