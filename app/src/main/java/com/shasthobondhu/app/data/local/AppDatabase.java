package com.shasthobondhu.app.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.shasthobondhu.app.data.local.dao.*;
import com.shasthobondhu.app.data.local.entities.*;

@Database(
        entities = {DonorEntity.class, RequestEntity.class, HospitalEntity.class, MedicineEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DonorDao donorDao();
    public abstract RequestDao requestDao();
    public abstract HospitalDao hospitalDao();
    public abstract MedicineDao medicineDao();
}
