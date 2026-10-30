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

    @Query("SELECT * FROM donors WHERE bloodGroup = :bloodGroup AND (division = :division OR district = :district)")
    LiveData<List<DonorEntity>> searchDonors(String bloodGroup, String division, String district);

    @Query("SELECT * FROM donors")
    LiveData<List<DonorEntity>> getAllDonors();
}
