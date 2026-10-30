package com.shasthobondhu.app.util;

import android.content.Context;
import com.shasthobondhu.app.data.local.AppDatabase;
import com.shasthobondhu.app.data.local.entities.*;
import java.util.ArrayList;
import java.util.List;

public class DataSeeder {
    public static void seedDatabase(Context context) {
        AppDatabase db = androidx.room.Room.databaseBuilder(context, AppDatabase.class, "shastho_bondhu_db").build();
        
        // Seed Donors
        List<DonorEntity> donors = new ArrayList<>();
        donors.add(new DonorEntity("Rahim Ahmed", "01711000000", "A+", "Dhaka", "Dhaka", "Mirpur", "2026-01-10", true));
        donors.add(new DonorEntity("Karim Uddin", "01811000000", "O+", "Dhaka", "Dhaka", "Uttara", "2026-03-15", true));
        donors.add(new DonorEntity("Sumi Akter", "01911000000", "B-", "Chattogram", "Chattogram", "Pahartali", "2026-05-20", true));
        
        // Seed Requests
        List<RequestEntity> requests = new ArrayList<>();
        requests.add(new RequestEntity("Patient X", "A+", 2, "Dhaka Medical College", "Dhaka", "Critical", "01511000000", "Open"));
        requests.add(new RequestEntity("Patient Y", "O-", 1, "Evercare Hospital", "Dhaka", "Urgent", "01611000000", "Open"));
        
        // Seed Hospitals
        List<HospitalEntity> hospitals = new ArrayList<>();
        hospitals.add(new HospitalEntity("Dhaka Medical College", "Bakshibazar, Dhaka", "02-11111", "Dhaka", "General, Emergency, ICU"));
        hospitals.add(new HospitalEntity("Evercare Hospital", "Bashundhara, Dhaka", "02-22222", "Dhaka", "Cardiology, Pediatrics, MRI"));
        
        // Seed Medicines
        List<MedicineEntity> medicines = new ArrayList<>();
        medicines.add(new MedicineEntity("Napa Extra", "Lazzah Pharmacy", "In Stock", "2026-10-29", "01700000000"));
        medicines.add(new MedicineEntity("Omeprazole", "Pharmacy Care", "Low", "2026-10-28", "01800000000"));
        medicines.add(new MedicineEntity("Insulin", "Health Plus", "Out of Stock", "2026-10-25", "01900000000"));

        // Note: In a real app, we'd use a background thread and Room's DAO. 
        // This is a simplified conceptual seeder for first-run.
    }
}
