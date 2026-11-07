package com.shasthobondhu.app.util;

import android.util.Patterns;

import java.util.regex.Pattern;

/** Small, dependency free validators shared by the auth screens. */
public final class ValidationUtils {

    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");

    private ValidationUtils() {
    }

    public static boolean isValidEmail(String email) {
        return email != null
                && !email.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /** Minimum 8 characters, at least one letter and one digit. */
    public static boolean isValidPassword(String password) {
        return password != null
                && password.length() >= 8
                && HAS_LETTER.matcher(password).matches()
                && HAS_DIGIT.matcher(password).matches();
    }

}
