package com.shasthobondhu.app.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Local account used when Firebase Email/Password sign-in has not been enabled yet.
 * It keeps the three roles usable offline; Firebase remains the primary provider.
 */
@Entity(tableName = "local_users", indices = {@Index(value = "email", unique = true)})
public class LocalUserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String uid;
    public String name;
    public String email;
    public String phone;
    public String role;
    public String passwordHash;
    public String passwordSalt;

    public LocalUserEntity(String uid, String name, String email, String phone,
                           String role, String passwordHash, String passwordSalt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }
}
