package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import java.util.List;

@Dao
public interface HospitalDao {
    @Insert
    void insert(HospitalEntity hospital);

    @Query("SELECT * FROM hospitals WHERE name LIKE '%' || :query || '%' OR area LIKE '%' || :query || '%'")
    LiveData<List<HospitalEntity>> searchHospitals(String query);

    @Query("SELECT COUNT(*) FROM hospitals")
    int count();
}
