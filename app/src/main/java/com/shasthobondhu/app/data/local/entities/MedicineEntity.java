package com.shasthobondhu.app.data.local.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
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
    public int pharmacyId;
    public String pharmacyArea;
    public String pharmacyAddress;

    @Ignore
    public MedicineEntity(String name, String pharmacyName, String stockStatus,
                          String lastUpdated, String pharmacyPhone) {
        this(name, pharmacyName, stockStatus, lastUpdated, pharmacyPhone,
                0, "", "");
    }

    public MedicineEntity(String name, String pharmacyName, String stockStatus,
                          String lastUpdated, String pharmacyPhone, int pharmacyId,
                          String pharmacyArea, String pharmacyAddress) {
        this.name = name;
        this.pharmacyName = pharmacyName;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
        this.pharmacyPhone = pharmacyPhone;
        this.pharmacyId = pharmacyId;
        this.pharmacyArea = pharmacyArea;
        this.pharmacyAddress = pharmacyAddress;
    }
}
