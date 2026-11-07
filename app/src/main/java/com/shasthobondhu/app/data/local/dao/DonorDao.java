package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shasthobondhu.app.data.local.entities.DonorEntity;

import java.util.List;

@Dao
public interface DonorDao {

    @Insert
    long insert(DonorEntity donor);

    @Update
    void update(DonorEntity donor);

    /**
     * Filters by blood group and by any level of the area hierarchy
     * (division, district or upazila). Empty strings match everything,
     * which keeps the search usable before the user types anything.
     */
    @Query("SELECT * FROM donors WHERE "
            + "isAvailable = 1 AND "
            + "(:bloodGroup = '' OR bloodGroup = :bloodGroup) AND "
            + "(:area = '' OR division = :area OR district = :area OR upazila = :area) "
            + "ORDER BY name ASC")
    LiveData<List<DonorEntity>> searchDonors(String bloodGroup, String area);

    @Query("SELECT * FROM donors WHERE isAvailable = 1 ORDER BY name ASC")
    LiveData<List<DonorEntity>> getAllDonors();

    @Query("SELECT * FROM donors WHERE ownerUid = :ownerUid LIMIT 1")
    DonorEntity getByOwnerUid(String ownerUid);

    @Query("SELECT * FROM donors WHERE ownerUid = :ownerUid LIMIT 1")
    LiveData<DonorEntity> observeByOwnerUid(String ownerUid);

    @Query("SELECT COUNT(*) FROM donors")
    int count();
}
