package com.shasthobondhu.app.di;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import com.shasthobondhu.app.data.local.AppDatabase;
import com.shasthobondhu.app.data.local.dao.*;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE blood_requests ADD COLUMN acceptedByUid TEXT");
            database.execSQL("ALTER TABLE blood_requests ADD COLUMN acceptedByName TEXT");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE donors ADD COLUMN availableAfterEpoch INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE hospitals ADD COLUMN doctors TEXT");
            database.execSQL("ALTER TABLE blood_requests ADD COLUMN patientDetails TEXT");
            database.execSQL("ALTER TABLE blood_requests ADD COLUMN neededDate TEXT");
            database.execSQL("ALTER TABLE blood_requests ADD COLUMN neededTime TEXT");
            database.execSQL("ALTER TABLE blood_requests ADD COLUMN remoteId TEXT");
        }
    };

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "shastho_bondhu_db")
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public DonorDao provideDonorDao(AppDatabase db) { return db.donorDao(); }

    @Provides
    public RequestDao provideRequestDao(AppDatabase db) { return db.requestDao(); }

    @Provides
    public HospitalDao provideHospitalDao(AppDatabase db) { return db.hospitalDao(); }

    @Provides
    public MedicineDao provideMedicineDao(AppDatabase db) { return db.medicineDao(); }

    @Provides
    public PharmacyDao providePharmacyDao(AppDatabase db) { return db.pharmacyDao(); }

    @Provides
    public LocalUserDao provideLocalUserDao(AppDatabase db) { return db.localUserDao(); }
}
