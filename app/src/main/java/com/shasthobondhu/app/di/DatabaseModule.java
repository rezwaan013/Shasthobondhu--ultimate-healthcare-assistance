package com.shasthobondhu.app.di;

import android.content.Context;

import androidx.room.Room;

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

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "shastho_bondhu_db")
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
