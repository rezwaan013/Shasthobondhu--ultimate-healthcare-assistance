package com.shasthobondhu.app.data.repository;

import androidx.lifecycle.LiveData;
import com.shasthobondhu.app.data.local.dao.RequestDao;
import com.shasthobondhu.app.data.local.entities.RequestEntity;
import javax.inject.Inject;
import java.util.List;

public class RequestRepository {
    private final RequestDao requestDao;

    @Inject
    public RequestRepository(RequestDao requestDao) {
        this.requestDao = requestDao;
    }

    public LiveData<List<RequestEntity>> getRequests(String area) {
        return requestDao.getOpenRequestsByArea(area);
    }

    public void createRequest(RequestEntity request) {
        requestDao.insert(request);
    }
}
