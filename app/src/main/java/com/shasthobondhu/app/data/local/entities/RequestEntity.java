package com.shasthobondhu.app.data.local.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "blood_requests")
public class RequestEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String patientName;
    public String bloodGroup;
    public int units;
    public String hospitalName;
    public String location;
    public String urgency;
    public String contactPhone;
    public String status; // Open, Accepted, Fulfilled
    public String requesterUid;
    public String acceptedByUid;
    public String acceptedByName;

    @Ignore
    public RequestEntity(String patientName, String bloodGroup, int units,
                         String hospitalName, String location, String urgency,
                         String contactPhone, String status) {
        this(patientName, bloodGroup, units, hospitalName, location, urgency,
                contactPhone, status, null);
    }

    public RequestEntity(String patientName, String bloodGroup, int units,
                         String hospitalName, String location, String urgency,
                         String contactPhone, String status, String requesterUid) {
        this.patientName = patientName;
        this.bloodGroup = bloodGroup;
        this.units = units;
        this.hospitalName = hospitalName;
        this.location = location;
        this.urgency = urgency;
        this.contactPhone = contactPhone;
        this.status = status;
        this.requesterUid = requesterUid;
    }
}
