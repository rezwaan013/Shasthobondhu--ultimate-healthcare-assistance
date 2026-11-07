package com.shasthobondhu.app.data.model;

import java.util.Objects;

/** Minimal local contact value; the full device address book is never persisted or uploaded. */
public final class EmergencyContact {
    public final String name;
    public final String phone;

    public EmergencyContact(String name, String phone) {
        this.name = name == null ? "" : name.trim();
        this.phone = phone == null ? "" : phone.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof EmergencyContact)) return false;
        EmergencyContact contact = (EmergencyContact) other;
        return name.equals(contact.name) && phone.equals(contact.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone);
    }
}
