package com.shasthobondhu.app.util;

import android.content.Context;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.util.constants.AppConstants;

/** Keeps the signed-in account and its role available to every screen. */
public final class SessionManager {

    private static final String PREFS = "shastho_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_LOCAL_MODE = "local_mode";
    private static final String ROLE_CACHE_PREFIX = "role_for_";

    private SessionManager() {
    }

    public static void signIn(Context context, String uid, String name, String email,
                              String phone, String role, boolean localMode) {
        String safeRole = normalizeRole(role);
        prefs(context).edit()
                .putString(KEY_UID, uid)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PHONE, phone)
                .putString(KEY_ROLE, safeRole)
                .putBoolean(KEY_LOCAL_MODE, localMode)
                .putString(ROLE_CACHE_PREFIX + (email == null ? "" : email.toLowerCase()), safeRole)
                .apply();
    }

    public static void signOut(Context context) {
        prefs(context).edit()
                .remove(KEY_UID)
                .remove(KEY_NAME)
                .remove(KEY_EMAIL)
                .remove(KEY_PHONE)
                .remove(KEY_ROLE)
                .remove(KEY_LOCAL_MODE)
                .apply();
    }

    public static boolean isSignedIn(Context context) {
        String uid = getUid(context);
        return uid != null && !uid.isEmpty();
    }

    public static boolean isLocalMode(Context context) {
        return prefs(context).getBoolean(KEY_LOCAL_MODE, false);
    }

    public static String getUid(Context context) {
        return prefs(context).getString(KEY_UID, "");
    }

    public static String getName(Context context) {
        return prefs(context).getString(KEY_NAME, "");
    }

    public static String getEmail(Context context) {
        return prefs(context).getString(KEY_EMAIL, "");
    }

    public static String getPhone(Context context) {
        return prefs(context).getString(KEY_PHONE, "");
    }

    public static String getRole(Context context) {
        return normalizeRole(prefs(context).getString(KEY_ROLE, AppConstants.ROLE_USER));
    }

    public static void cacheRole(Context context, String email, String role) {
        if (email == null) {
            return;
        }
        prefs(context).edit()
                .putString(ROLE_CACHE_PREFIX + email.toLowerCase(), normalizeRole(role))
                .apply();
    }

    public static String getCachedRole(Context context, String email) {
        if (email == null) {
            return AppConstants.ROLE_USER;
        }
        return normalizeRole(prefs(context)
                .getString(ROLE_CACHE_PREFIX + email.toLowerCase(), AppConstants.ROLE_USER));
    }

    public static boolean isPharmacyOwner(Context context) {
        return AppConstants.ROLE_PHARMACY.equals(getRole(context));
    }

    public static boolean isHospitalOwner(Context context) {
        return AppConstants.ROLE_HOSPITAL.equals(getRole(context));
    }

    public static String roleLabel(Context context, String role) {
        String safeRole = normalizeRole(role);
        if (AppConstants.ROLE_PHARMACY.equals(safeRole)) {
            return context.getString(R.string.role_pharmacy);
        }
        if (AppConstants.ROLE_HOSPITAL.equals(safeRole)) {
            return context.getString(R.string.role_hospital);
        }
        return context.getString(R.string.role_user);
    }

    public static String normalizeRole(String role) {
        if (AppConstants.ROLE_PHARMACY.equals(role) || AppConstants.ROLE_HOSPITAL.equals(role)) {
            return role;
        }
        return AppConstants.ROLE_USER;
    }

    private static android.content.SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
