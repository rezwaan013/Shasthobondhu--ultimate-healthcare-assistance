package com.shasthobondhu.app.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pharmacies")
public class PharmacyEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String address;
    public String area;
    public String phone;
    public String openHours;
    public String ownerUid;
    public boolean isOpen;

    public PharmacyEntity(String name, String address, String area, String phone,
                          String openHours, String ownerUid, boolean isOpen) {
        this.name = name;
        this.address = address;
        this.area = area;
        this.phone = phone;
        this.openHours = openHours;
        this.ownerUid = ownerUid;
        this.isOpen = isOpen;
    }
}
