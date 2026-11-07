package com.shasthobondhu.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.shasthobondhu.app.data.model.EmergencyContact;
import com.shasthobondhu.app.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/** Reads phone contacts off-main and persists only the one explicitly selected contact. */
@Singleton
public class EmergencyContactRepository {

    private static final String PREFS = "emergency_contact";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";

    private final Context context;
    private final SharedPreferences preferences;

    @Inject
    public EmergencyContactRepository(@ApplicationContext Context context) {
        this.context = context.getApplicationContext();
        preferences = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public EmergencyContact getSavedContact() {
        String phone = preferences.getString(KEY_PHONE, "");
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        return new EmergencyContact(preferences.getString(KEY_NAME, ""), phone);
    }

    public void saveContact(EmergencyContact contact) {
        if (contact == null || contact.phone.isEmpty()) {
            return;
        }
        preferences.edit()
                .putString(KEY_NAME, contact.name)
                .putString(KEY_PHONE, contact.phone)
                .apply();
    }

    public void loadPhoneContacts(Callback callback) {
        AppExecutors.io(() -> {
            List<EmergencyContact> contacts = new ArrayList<>();
            Set<String> seenNumbers = new HashSet<>();
            String[] projection = {
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            try (Cursor cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC")) {
                if (cursor != null) {
                    int nameColumn = cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int phoneColumn = cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER);
                    while (cursor.moveToNext()) {
                        String phone = phoneColumn < 0 ? "" : cursor.getString(phoneColumn);
                        String normalized = phone == null ? ""
                                : phone.replaceAll("[^+0-9]", "");
                        if (normalized.isEmpty() || !seenNumbers.add(normalized)) {
                            continue;
                        }
                        String name = nameColumn < 0 ? "" : cursor.getString(nameColumn);
                        contacts.add(new EmergencyContact(name, phone));
                    }
                }
                AppExecutors.main(() -> callback.onLoaded(contacts));
            } catch (RuntimeException error) {
                AppExecutors.main(callback::onError);
            }
        });
    }

    public interface Callback {
        void onLoaded(List<EmergencyContact> contacts);

        void onError();
    }
}
