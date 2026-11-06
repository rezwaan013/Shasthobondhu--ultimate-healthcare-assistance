package com.shasthobondhu.app.util;

import com.shasthobondhu.app.data.local.AppDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Inserts demo data the first time the app runs so every screen has
 * something to show. Runs off the main thread and is idempotent.
 */
public class DataSeeder {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private DataSeeder() {
    }

    public static void seedDatabase(AppDatabase db) {
        if (db == null) {
            return;
        }
        EXECUTOR.execute(() -> {
            try {
                if (db.donorDao().count() > 0) {
                    return;
                }

                db.donorDao().insert(new com.shasthobondhu.app.data.local.entities.DonorEntity(
                        "Rahim Ahmed", "+8801711000000", "A+", "Dhaka", "Dhaka", "Mirpur",
                        "2026-01-10", true));
                db.donorDao().insert(new com.shasthobondhu.app.data.local.entities.DonorEntity(
                        "Karim Uddin", "+8801811000000", "O+", "Dhaka", "Dhaka", "Uttara",
                        "2026-03-15", true));
                db.donorDao().insert(new com.shasthobondhu.app.data.local.entities.DonorEntity(
                        "Sumi Akter", "+8801911000000", "B-", "Chattogram", "Chattogram",
                        "Pahartali", "2026-05-20", true));

                db.requestDao().insert(new com.shasthobondhu.app.data.local.entities.RequestEntity(
                        "Patient X", "A+", 2, "Dhaka Medical College", "Dhaka", "Critical",
                        "+8801511000000", "Open"));
                db.requestDao().insert(new com.shasthobondhu.app.data.local.entities.RequestEntity(
                        "Patient Y", "O-", 1, "Evercare Hospital", "Dhaka", "Urgent",
                        "+8801611000000", "Open"));

                db.hospitalDao().insert(new com.shasthobondhu.app.data.local.entities.HospitalEntity(
                        "Dhaka Medical College", "Bakshibazar, Dhaka", "+880211111",
                        "Dhaka", "General, Emergency, ICU"));
                db.hospitalDao().insert(new com.shasthobondhu.app.data.local.entities.HospitalEntity(
                        "Evercare Hospital", "Bashundhara, Dhaka", "+880222222",
                        "Dhaka", "Cardiology, Pediatrics, MRI"));

                db.medicineDao().insert(new com.shasthobondhu.app.data.local.entities.MedicineEntity(
                        "Napa Extra", "Lazzah Pharmacy", "In Stock", "2026-10-29",
                        "+8801700000000"));
                db.medicineDao().insert(new com.shasthobondhu.app.data.local.entities.MedicineEntity(
                        "Omeprazole", "Pharmacy Care", "Low", "2026-10-28",
                        "+8801800000000"));
                db.medicineDao().insert(new com.shasthobondhu.app.data.local.entities.MedicineEntity(
                        "Insulin", "Health Plus", "Out of Stock", "2026-10-25",
                        "+8801900000000"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
