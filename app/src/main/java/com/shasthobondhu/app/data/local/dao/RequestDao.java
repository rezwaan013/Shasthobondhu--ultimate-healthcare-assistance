package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shasthobondhu.app.data.local.entities.RequestEntity;

import java.util.List;

@Dao
public interface RequestDao {

    @Insert
    long insert(RequestEntity request);

    @Update
    void update(RequestEntity request);

    @Query("SELECT * FROM blood_requests WHERE status != 'Fulfilled' AND "
            + "(location LIKE '%' || :area || '%' OR hospitalName LIKE '%' || :area || '%') "
            + "ORDER BY id DESC")
    LiveData<List<RequestEntity>> getOpenRequestsByArea(String area);

    @Query("SELECT * FROM blood_requests ORDER BY id DESC")
    LiveData<List<RequestEntity>> getAllRequests();

    @Query("SELECT * FROM blood_requests WHERE status != 'Fulfilled' ORDER BY id DESC")
    LiveData<List<RequestEntity>> getOpenRequests();

    @Query("UPDATE blood_requests SET status = 'Accepted', acceptedByUid = :uid, "
            + "acceptedByName = :name WHERE id = :requestId AND status = 'Open'")
    int acceptRequest(int requestId, String uid, String name);

    @Query("SELECT * FROM blood_requests WHERE remoteId = :remoteId LIMIT 1")
    RequestEntity getByRemoteId(String remoteId);

    @Query("SELECT * FROM blood_requests WHERE id = :requestId LIMIT 1")
    RequestEntity getRequestById(int requestId);

    @Query("SELECT COUNT(*) FROM blood_requests")
    int count();

    @Query("UPDATE blood_requests SET status = :status WHERE id = :requestId")
    int updateStatus(int requestId, String status);
}
