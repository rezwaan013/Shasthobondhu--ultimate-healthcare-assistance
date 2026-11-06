package com.shasthobondhu.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.shasthobondhu.app.data.local.entities.LocalUserEntity;

@Dao
public interface LocalUserDao {

    @Insert
    long insert(LocalUserEntity user);

    @Query("SELECT * FROM local_users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    LocalUserEntity findByEmail(String email);

    @Query("SELECT COUNT(*) FROM local_users")
    int count();
}
