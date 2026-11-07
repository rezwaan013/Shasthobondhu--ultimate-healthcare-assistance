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
            + "availableAfterEpoch <= :now AND "
            + "(:bloodGroup = '' OR bloodGroup = :bloodGroup) AND "
            + "(:division = '' OR LOWER(division) = LOWER(:division)) AND "
            + "(:area = '' OR LOWER(district) = LOWER(:area) OR LOWER(upazila) = LOWER(:area)) "
            + "ORDER BY name ASC")
    LiveData<List<DonorEntity>> searchDonors(String bloodGroup, String division, String area,
                                             long now);

    @Query("SELECT * FROM donors WHERE isAvailable = 1 AND availableAfterEpoch <= :now "
            + "ORDER BY name ASC")
    LiveData<List<DonorEntity>> getAllDonors(long now);

    @Query("SELECT * FROM donors WHERE ownerUid = :ownerUid LIMIT 1")
    DonorEntity getByOwnerUid(String ownerUid);

    @Query("SELECT * FROM donors WHERE ownerUid = :ownerUid LIMIT 1")
    LiveData<DonorEntity> observeByOwnerUid(String ownerUid);

    @Query("SELECT COUNT(*) FROM donors")
    int count();
}
