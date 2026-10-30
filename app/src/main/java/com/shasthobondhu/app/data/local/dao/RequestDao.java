package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.shasthobondhu.app.data.local.entities.RequestEntity;
import java.util.List;

@Dao
public interface RequestDao {
    @Insert
    void insert(RequestEntity request);

    @Query("SELECT * FROM blood_requests WHERE status = 'Open' AND location LIKE '%' || :area || '%'")
    LiveData<List<RequestEntity>> getOpenRequestsByArea(String area);

    @Query("SELECT * FROM blood_requests ORDER BY id DESC")
    LiveData<List<RequestEntity>> getAllRequests();
}
