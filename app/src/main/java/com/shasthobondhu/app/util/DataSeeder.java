package com.shasthobondhu.app.util;

import com.shasthobondhu.app.data.local.AppDatabase;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.data.local.entities.LocalUserEntity;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.data.local.entities.PharmacyEntity;
import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.util.constants.AppConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Locale;

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

                db.donorDao().insert(new DonorEntity(
                        "Rahim Ahmed", "+8801711000000", "A+", "Dhaka", "Dhaka", "Mirpur",
                        "10/01/2026", true));
                db.donorDao().insert(new DonorEntity(
                        "Karim Uddin", "+8801811000000", "O+", "Dhaka", "Dhaka", "Uttara",
                        "15/03/2026", true));
                db.donorDao().insert(new DonorEntity(
                        "Sumi Akter", "+8801911000000", "B-", "Chattogram", "Chattogram",
                        "Pahartali", "20/05/2026", true));

                db.requestDao().insert(new RequestEntity(
                        "Patient X", "A+", 2, "Dhaka Medical College", "Dhaka", "Critical",
                        "+8801511000000", "Open"));
                db.requestDao().insert(new RequestEntity(
                        "Patient Y", "O-", 1, "Evercare Hospital", "Dhaka", "Urgent",
                        "+8801611000000", "Open"));

                db.hospitalDao().insert(new HospitalEntity(
                        "Dhaka Medical College", "Bakshibazar, Dhaka", "+880211111",
                        "Dhaka", "General, Emergency, ICU, CBC, X-Ray"));
                db.hospitalDao().insert(new HospitalEntity(
                        "Evercare Hospital", "Bashundhara, Dhaka", "+880222222",
                        "Dhaka", "Cardiology, Pediatrics, MRI, Blood test, CT scan"));
                db.hospitalDao().insert(new HospitalEntity(
                        "Chattogram General Hospital", "Anderkilla, Chattogram", "+880233333",
                        "Chattogram", "General, Emergency, Pathology, Ultrasound"));

                long lazzahId = db.pharmacyDao().insert(new PharmacyEntity(
                        "Lazzah Pharmacy", "Mirpur 10, Dhaka", "Dhaka", "+8801700000000",
                        "8:00 AM – 11:00 PM", "seed_pharmacy_1", true));
                long healthPlusId = db.pharmacyDao().insert(new PharmacyEntity(
                        "Health Plus", "Uttara Sector 7, Dhaka", "Dhaka", "+8801800000000",
                        "24 hours", "seed_pharmacy_2", true));
                long pharmacyCareId = db.pharmacyDao().insert(new PharmacyEntity(
                        "Pharmacy Care", "Pahartali, Chattogram", "Chattogram", "+8801900000000",
                        "9:00 AM – 10:00 PM", "seed_pharmacy_3", true));

                db.medicineDao().insert(new MedicineEntity(
                        "Napa Extra", "Lazzah Pharmacy", AppConstants.STOCK_IN, "2026-10-29",
                        "+8801700000000", (int) lazzahId, "Dhaka",
                        "Mirpur 10, Dhaka"));
                db.medicineDao().insert(new MedicineEntity(
                        "Omeprazole", "Pharmacy Care", AppConstants.STOCK_LOW, "2026-10-28",
                        "+8801900000000", (int) pharmacyCareId, "Chattogram",
                        "Pahartali, Chattogram"));
                db.medicineDao().insert(new MedicineEntity(
                        "Insulin", "Health Plus", AppConstants.STOCK_OUT, "2026-10-25",
                        "+8801800000000", (int) healthPlusId, "Dhaka",
                        "Uttara Sector 7, Dhaka"));
                db.medicineDao().insert(new MedicineEntity(
                        "Salbutamol Inhaler", "Health Plus", AppConstants.STOCK_IN,
                        "2026-11-01", "+8801800000000", (int) healthPlusId, "Dhaka",
                        "Uttara Sector 7, Dhaka"));
                db.medicineDao().insert(new MedicineEntity(
                        "Paracetamol", "Lazzah Pharmacy", AppConstants.STOCK_IN,
                        "2026-11-02", "+8801700000000", (int) lazzahId, "Dhaka",
                        "Mirpur 10, Dhaka"));

                seedDemoAccounts(db);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** Offline demo accounts so the three roles can be reviewed before Firebase is enabled. */
    private static void seedDemoAccounts(AppDatabase db) {
        if (db.localUserDao().count() > 0) {
            return;
        }
        insertLocalUser(db, "Demo General User", "user@shasthobondhu.app", "+8801700000001",
                AppConstants.ROLE_USER, "User1234");
        insertLocalUser(db, "Demo Pharmacy Owner", "owner.pharmacy@shasthobondhu.app",
                "+8801700000002", AppConstants.ROLE_PHARMACY, "Pharmacy123");
        insertLocalUser(db, "Demo Hospital Owner", "owner.hospital@shasthobondhu.app",
                "+8801700000003", AppConstants.ROLE_HOSPITAL, "Hospital123");
    }

    private static void insertLocalUser(AppDatabase db, String name, String email,
                                        String phone, String role, String password) {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash(password, salt);
        db.localUserDao().insert(new LocalUserEntity(
                "local_" + role.toLowerCase(Locale.ROOT) + "_demo",
                name, email, phone, role, hash, salt));
    }
}
