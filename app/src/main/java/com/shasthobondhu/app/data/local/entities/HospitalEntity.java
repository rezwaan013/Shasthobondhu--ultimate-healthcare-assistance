package com.shasthobondhu.app.data.local.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "hospitals")
public class HospitalEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String address;
    public String phone;
    public String area;
    public String services;
    public String doctors;
    public String ownerUid;

    @Ignore
    public HospitalEntity(String name, String address, String phone, String area,
                          String services) {
        this(name, address, phone, area, services, null);
    }

    public HospitalEntity(String name, String address, String phone, String area,
                          String services, String ownerUid) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.area = area;
        this.services = services;
        this.ownerUid = ownerUid;
    }
}
