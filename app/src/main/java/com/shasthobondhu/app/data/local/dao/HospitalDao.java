package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shasthobondhu.app.data.local.entities.HospitalEntity;

import java.util.List;

@Dao
public interface HospitalDao {

    @Insert
    long insert(HospitalEntity hospital);

    @Update
    void update(HospitalEntity hospital);

    @Query("SELECT * FROM hospitals WHERE "
            + "name LIKE '%' || :query || '%' OR "
            + "area LIKE '%' || :query || '%' OR "
            + "address LIKE '%' || :query || '%' OR "
            + "services LIKE '%' || :query || '%' "
            + "ORDER BY name ASC")
    LiveData<List<HospitalEntity>> searchHospitals(String query);

    @Query("SELECT * FROM hospitals WHERE id = :id LIMIT 1")
    HospitalEntity getById(int id);

    @Query("SELECT * FROM hospitals WHERE ownerUid = :ownerUid LIMIT 1")
    LiveData<HospitalEntity> getByOwner(String ownerUid);

    @Query("SELECT COUNT(*) FROM hospitals")
    int count();
}
