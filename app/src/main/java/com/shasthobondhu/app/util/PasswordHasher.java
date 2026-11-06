package com.shasthobondhu.app.util;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

/** Salted SHA-256 hashing for the offline fallback accounts. */
public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String newSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes("UTF-8"));
            byte[] hashed = digest.digest(password.getBytes("UTF-8"));
            return Base64.encodeToString(hashed, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Hashing is unavailable", e);
        }
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        if (password == null || salt == null || expectedHash == null) {
            return false;
        }
        return expectedHash.equals(hash(password, salt));
    }
}
