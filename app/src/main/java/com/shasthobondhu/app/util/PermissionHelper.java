package com.shasthobondhu.app.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shasthobondhu.app.R;

/** Reusable runtime-permission flow with rationale, denial, and app-settings handling. */
public final class PermissionHelper {

    private static final String PREFS = "permission_requests";
    private static final String KEY_PREFIX = "requested_";

    private final ComponentActivity activity;
    private final SharedPreferences preferences;
    private final ActivityResultLauncher<String> launcher;
    private PendingRequest pendingRequest;

    public PermissionHelper(ComponentActivity activity) {
        this.activity = activity;
        preferences = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), this::onPermissionResult);
    }

    public boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void request(String permission, @StringRes int rationaleTitle,
                        @StringRes int rationaleMessage, Callback callback) {
        if (isGranted(permission)) {
            callback.onGranted();
            return;
        }
        pendingRequest = new PendingRequest(permission, rationaleTitle, rationaleMessage, callback);
        if (activity.shouldShowRequestPermissionRationale(permission)) {
            showRationale();
        } else {
            launchPermission();
        }
    }

    private void showRationale() {
        if (pendingRequest == null) {
            return;
        }
        new MaterialAlertDialogBuilder(activity)
                .setTitle(pendingRequest.rationaleTitle)
                .setMessage(pendingRequest.rationaleMessage)
                .setPositiveButton(R.string.permission_continue,
                        (dialog, which) -> launchPermission())
                .setNegativeButton(R.string.not_now, (dialog, which) -> deny(false))
                .show();
    }

    private void launchPermission() {
        if (pendingRequest == null) {
            return;
        }
        preferences.edit().putBoolean(KEY_PREFIX + pendingRequest.permission, true).apply();
        launcher.launch(pendingRequest.permission);
    }

    private void onPermissionResult(boolean granted) {
        if (pendingRequest == null) {
            return;
        }
        if (granted) {
            Callback callback = pendingRequest.callback;
            pendingRequest = null;
            callback.onGranted();
            return;
        }
        boolean requestedBefore = preferences.getBoolean(
                KEY_PREFIX + pendingRequest.permission, false);
        boolean permanentlyDenied = requestedBefore
                && !activity.shouldShowRequestPermissionRationale(pendingRequest.permission);
        if (permanentlyDenied) {
            showSettingsDialog();
        } else {
            showDeniedDialog();
        }
    }

    private void showDeniedDialog() {
        if (pendingRequest == null) {
            return;
        }
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.permission_denied_title)
                .setMessage(R.string.permission_denied_message)
                .setPositiveButton(R.string.try_again, (dialog, which) -> showRationale())
                .setNegativeButton(R.string.not_now, (dialog, which) -> deny(false))
                .show();
    }

    private void showSettingsDialog() {
        if (pendingRequest == null) {
            return;
        }
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.permission_settings_title)
                .setMessage(R.string.permission_settings_message)
                .setPositiveButton(R.string.open_app_settings, (dialog, which) -> {
                    openAppSettings();
                    deny(true);
                })
                .setNegativeButton(R.string.not_now, (dialog, which) -> deny(true))
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }

    private void deny(boolean permanentlyDenied) {
        if (pendingRequest == null) {
            return;
        }
        Callback callback = pendingRequest.callback;
        pendingRequest = null;
        callback.onDenied(permanentlyDenied);
    }

    public interface Callback {
        void onGranted();

        void onDenied(boolean permanentlyDenied);
    }

    private static final class PendingRequest {
        final String permission;
        final int rationaleTitle;
        final int rationaleMessage;
        final Callback callback;

        PendingRequest(String permission, int rationaleTitle, int rationaleMessage,
                       Callback callback) {
            this.permission = permission;
            this.rationaleTitle = rationaleTitle;
            this.rationaleMessage = rationaleMessage;
            this.callback = callback;
        }
    }
}
