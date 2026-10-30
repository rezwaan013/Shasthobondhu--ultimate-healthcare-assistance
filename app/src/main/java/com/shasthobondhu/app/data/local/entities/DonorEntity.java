package com.shasthobondhu.app.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "donors")
public class DonorEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String phone;
    public String bloodGroup;
    public String division;
    public String district;
    public String upazila;
    public String lastDonationDate;
    public boolean isAvailable;

    public DonorEntity(String name, String phone, String bloodGroup, String division, String district, String upazila, String lastDonationDate, boolean isAvailable) {
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.division = division;
        this.district = district;
        this.upazila = upazila;
        this.lastDonationDate = lastDonationDate;
        this.isAvailable = isAvailable;
    }
}
