package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shasthobondhu.app.data.local.entities.PharmacyEntity;

@Dao
public interface PharmacyDao {

    @Insert
    long insert(PharmacyEntity pharmacy);

    @Update
    void update(PharmacyEntity pharmacy);

    @Query("SELECT * FROM pharmacies WHERE ownerUid = :ownerUid LIMIT 1")
    LiveData<PharmacyEntity> getByOwner(String ownerUid);

    @Query("SELECT * FROM pharmacies WHERE ownerUid = :ownerUid LIMIT 1")
    PharmacyEntity findByOwnerSync(String ownerUid);

    @Query("SELECT COUNT(*) FROM pharmacies")
    int count();
}
