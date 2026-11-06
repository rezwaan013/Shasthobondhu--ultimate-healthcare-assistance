package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.shasthobondhu.app.data.local.entities.DonorEntity;

import java.util.List;

@Dao
public interface DonorDao {

    @Insert
    void insert(DonorEntity donor);

    /**
     * Filters by blood group and by any level of the area hierarchy
     * (division, district or upazila). Empty strings match everything,
     * which keeps the search usable before the user types anything.
     */
    @Query("SELECT * FROM donors WHERE "
            + "(:bloodGroup = '' OR bloodGroup = :bloodGroup) AND "
            + "(:area = '' OR division = :area OR district = :area OR upazila = :area) "
            + "ORDER BY name ASC")
    LiveData<List<DonorEntity>> searchDonors(String bloodGroup, String area);

    @Query("SELECT * FROM donors ORDER BY name ASC")
    LiveData<List<DonorEntity>> getAllDonors();

    @Query("SELECT COUNT(*) FROM donors")
    int count();
}
