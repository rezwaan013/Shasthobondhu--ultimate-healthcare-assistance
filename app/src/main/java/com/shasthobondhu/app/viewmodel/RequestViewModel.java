package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.data.repository.RequestRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RequestViewModel extends ViewModel {

    private final RequestRepository repository;

    @Inject
    public RequestViewModel(RequestRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<RequestEntity>> getRequests(String area) {
        return repository.getRequests(area);
    }

    public LiveData<List<RequestEntity>> getAllOpenRequests() {
        return repository.getAllOpenRequests();
    }

    public void createRequest(RequestEntity request, Runnable onSaved) {
        repository.createRequestAndNotify(request, onSaved);
    }
}
