package com.shasthobondhu.app.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicines")
public class MedicineEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String pharmacyName;
    public String stockStatus; // In Stock, Low, Out of Stock
    public String lastUpdated;
    public String pharmacyPhone;

    public MedicineEntity(String name, String pharmacyName, String stockStatus, String lastUpdated, String pharmacyPhone) {
        this.name = name;
        this.pharmacyName = pharmacyName;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
        this.pharmacyPhone = pharmacyPhone;
    }
}
