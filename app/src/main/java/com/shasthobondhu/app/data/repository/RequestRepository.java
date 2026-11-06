package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;

import com.shasthobondhu.app.data.local.dao.RequestDao;
import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.util.AppExecutors;

import java.util.List;

import javax.inject.Inject;

public class RequestRepository {
    private final RequestDao requestDao;

    @Inject
    public RequestRepository(RequestDao requestDao) {
        this.requestDao = requestDao;
    }

    public LiveData<List<RequestEntity>> getRequests(String area) {
        return requestDao.getOpenRequestsByArea(area == null ? "" : area);
    }

    public LiveData<List<RequestEntity>> getAllOpenRequests() {
        return requestDao.getOpenRequests();
    }

    public void createRequest(RequestEntity request) {
        AppExecutors.io(() -> requestDao.insert(request));
    }

    public void createRequestAndNotify(RequestEntity request, Runnable onSaved) {
        AppExecutors.io(() -> {
            requestDao.insert(request);
            AppExecutors.main(onSaved);
        });
    }
}
