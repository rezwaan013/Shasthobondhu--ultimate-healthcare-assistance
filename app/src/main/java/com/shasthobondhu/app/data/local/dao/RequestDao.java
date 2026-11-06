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

    @Query("SELECT * FROM blood_requests WHERE status = 'Open' AND "
            + "(location LIKE '%' || :area || '%' OR hospitalName LIKE '%' || :area || '%') "
            + "ORDER BY id DESC")
    LiveData<List<RequestEntity>> getOpenRequestsByArea(String area);

    @Query("SELECT * FROM blood_requests ORDER BY id DESC")
    LiveData<List<RequestEntity>> getAllRequests();

    @Query("SELECT * FROM blood_requests WHERE status = 'Open' ORDER BY id DESC")
    LiveData<List<RequestEntity>> getOpenRequests();

    @Query("SELECT COUNT(*) FROM blood_requests")
    int count();
}
