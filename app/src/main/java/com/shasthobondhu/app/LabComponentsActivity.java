package com.shasthobondhu.app;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.shasthobondhu.app.data.remote.model.WeatherResponse;
import com.shasthobondhu.app.data.repository.FirebaseDemoRepository;
import com.shasthobondhu.app.data.repository.PublicHealthRepository;
import com.shasthobondhu.app.databinding.ActivityLabComponentsBinding;
import com.shasthobondhu.app.service.HealthMonitorService;
import com.shasthobondhu.app.system.SystemEventReceiver;
import com.shasthobondhu.app.util.MapNavigationUtils;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Second Activity dedicated to demonstrable Android lab requirements.
 * MainActivity launches it with an explicit Intent.
 */
@AndroidEntryPoint
public class LabComponentsActivity extends AppCompatActivity
        implements SystemEventReceiver.Listener {

    public static final String EXTRA_ENTRY_SOURCE = "entry_source";
    private static final String PREFS = "lab_demo_prefs";
    private static final String KEY_CONTACT = "selected_contact";
    private static final String STATE_API = "api_status";
    private static final String STATE_FIREBASE = "firebase_status";

    @Inject
    PublicHealthRepository publicHealthRepository;

    @Inject
    FirebaseDemoRepository firebaseDemoRepository;

    private ActivityLabComponentsBinding binding;
    private SystemEventReceiver systemEventReceiver;
    private boolean receiverRegistered;
    private boolean startServiceAfterPermission;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                updatePermissionStatus();
                Toast.makeText(this,
                        granted ? R.string.notification_permission_granted
                                : R.string.notification_permission_denied,
                        Toast.LENGTH_SHORT).show();
                if (startServiceAfterPermission) {
                    startServiceAfterPermission = false;
                    startHealthMonitor();
                }
            });

    private final ActivityResultLauncher<Intent> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {
                    readSelectedContact(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLabComponentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        systemEventReceiver = new SystemEventReceiver(this);
        bindActions();
        showInitialState(savedInstanceState);
    }

    private void bindActions() {
        binding.btnLabBack.setOnClickListener(v -> finish());
        binding.btnOpenMapDemo.setOnClickListener(v ->
                MapNavigationUtils.navigate(this, "Dhaka Medical College Hospital, Dhaka"));
        binding.btnAirplaneSettings.setOnClickListener(v ->
                openSettings(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
        binding.btnBatterySaverSettings.setOnClickListener(v ->
                openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS));
        binding.btnNotificationPermission.setOnClickListener(v -> requestNotificationPermission(false));
        binding.btnStartMonitor.setOnClickListener(v -> requestNotificationPermission(true));
        binding.btnStopMonitor.setOnClickListener(v -> stopHealthMonitor());
        binding.btnPickContact.setOnClickListener(v -> pickEmergencyContact());
        binding.btnLoadHealthApi.setOnClickListener(v -> loadHealthApi());
        binding.btnRunFirebaseDemo.setOnClickListener(v -> runFirebaseDemo());
    }

    private void showInitialState(Bundle savedInstanceState) {
        String source = getIntent().getStringExtra(EXTRA_ENTRY_SOURCE);
        binding.tvArchitectureStatus.setText(getString(R.string.lab_architecture_status,
                source == null ? "MainActivity" : source));
        updatePermissionStatus();
        updateServiceStatus();

        String contact = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_CONTACT, getString(R.string.no_contact_selected));
        binding.tvContactStatus.setText(contact);

        if (savedInstanceState != null) {
            binding.tvApiStatus.setText(savedInstanceState.getString(
                    STATE_API, getString(R.string.api_ready)));
            binding.tvFirebaseStatus.setText(savedInstanceState.getString(
                    STATE_FIREBASE, getString(R.string.firebase_ready)));
        } else {
            binding.tvApiStatus.setText(R.string.api_ready);
            binding.tvFirebaseStatus.setText(R.string.firebase_ready);
        }
    }

    private void requestNotificationPermission(boolean startService) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            startServiceAfterPermission = startService;
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        if (startService) {
            startHealthMonitor();
        } else {
            Toast.makeText(this, R.string.notification_permission_already_granted,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePermissionStatus() {
        boolean granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        binding.tvPermissionStatus.setText(granted
                ? R.string.permission_status_granted : R.string.permission_status_needed);
    }

    private void startHealthMonitor() {
        Intent intent = new Intent(this, HealthMonitorService.class);
        intent.setAction(HealthMonitorService.ACTION_START);
        ContextCompat.startForegroundService(this, intent);
        binding.getRoot().postDelayed(this::updateServiceStatus, 300L);
    }

    private void stopHealthMonitor() {
        Intent intent = new Intent(this, HealthMonitorService.class);
        intent.setAction(HealthMonitorService.ACTION_STOP);
        startService(intent);
        binding.getRoot().postDelayed(this::updateServiceStatus, 300L);
    }

    private void updateServiceStatus() {
        boolean running = HealthMonitorService.isRunning(this);
        binding.tvServiceStatus.setText(running
                ? R.string.monitor_status_running : R.string.monitor_status_stopped);
        binding.btnStartMonitor.setEnabled(!running);
        binding.btnStopMonitor.setEnabled(running);
    }

    private void pickEmergencyContact() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    private void readSelectedContact(Uri contactUri) {
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        try (Cursor cursor = getContentResolver().query(
                contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER);
                String name = nameIndex < 0 ? getString(R.string.unknown_contact)
                        : cursor.getString(nameIndex);
                String number = numberIndex < 0 ? "" : cursor.getString(numberIndex);
                String contact = getString(R.string.selected_contact, name, number);
                binding.tvContactStatus.setText(contact);
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_CONTACT, contact)
                        .apply();
                return;
            }
        } catch (SecurityException error) {
            binding.tvContactStatus.setText(R.string.contact_access_failed);
            return;
        }
        binding.tvContactStatus.setText(R.string.contact_access_failed);
    }

    private void loadHealthApi() {
        binding.btnLoadHealthApi.setEnabled(false);
        binding.apiProgress.setVisibility(View.VISIBLE);
        binding.tvApiStatus.setText(R.string.api_loading);
        publicHealthRepository.loadDhakaConditions(new PublicHealthRepository.ResultCallback() {
            @Override
            public void onSuccess(WeatherResponse.CurrentConditions conditions) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                binding.btnLoadHealthApi.setEnabled(true);
                binding.apiProgress.setVisibility(View.GONE);
                binding.tvApiStatus.setText(getString(R.string.api_result,
                        value(conditions.getTemperature()),
                        conditions.getHumidity() == null ? "--"
                                : String.valueOf(conditions.getHumidity()),
                        value(conditions.getWindSpeed()),
                        conditions.getTime() == null ? "--" : conditions.getTime()));
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                binding.btnLoadHealthApi.setEnabled(true);
                binding.apiProgress.setVisibility(View.GONE);
                binding.tvApiStatus.setText(getString(R.string.api_error, message));
            }
        });
    }

    private String value(Double value) {
        return value == null ? "--" : String.format(Locale.US, "%.1f", value);
    }

    private void runFirebaseDemo() {
        binding.btnRunFirebaseDemo.setEnabled(false);
        binding.firebaseProgress.setVisibility(View.VISIBLE);
        binding.tvFirebaseStatus.setText(R.string.firebase_running);
        firebaseDemoRepository.runCloudCheck(new FirebaseDemoRepository.ResultCallback() {
            @Override
            public void onSuccess(String firebaseUid) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                finishFirebaseAction();
                binding.tvFirebaseStatus.setText(
                        getString(R.string.firebase_success, firebaseUid));
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                finishFirebaseAction();
                binding.tvFirebaseStatus.setText(getString(R.string.firebase_error, message));
            }
        });
    }

    private void finishFirebaseAction() {
        binding.btnRunFirebaseDemo.setEnabled(true);
        binding.firebaseProgress.setVisibility(View.GONE);
    }

    private void openSettings(String action) {
        Intent intent = new Intent(action);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException error) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    @Override
    public void onSystemEvent(String action, boolean enabled, int batteryPercentage) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            binding.tvBroadcastStatus.setText(enabled
                    ? R.string.broadcast_airplane_on : R.string.broadcast_airplane_off);
        } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(action)) {
            binding.tvBroadcastStatus.setText(enabled
                    ? R.string.broadcast_power_save_on : R.string.broadcast_power_save_off);
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            binding.tvBroadcastStatus.setText(
                    getString(R.string.broadcast_battery, batteryPercentage));
        } else if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            binding.tvBroadcastStatus.setText(R.string.broadcast_battery_low);
        } else if (Intent.ACTION_BATTERY_OKAY.equals(action)) {
            binding.tvBroadcastStatus.setText(R.string.broadcast_battery_okay);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_BATTERY_LOW);
            filter.addAction(Intent.ACTION_BATTERY_OKAY);
            filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(systemEventReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(systemEventReceiver, filter);
            }
            receiverRegistered = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
        updateServiceStatus();
    }

    @Override
    protected void onStop() {
        if (receiverRegistered) {
            unregisterReceiver(systemEventReceiver);
            receiverRegistered = false;
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_API, binding.tvApiStatus.getText().toString());
        outState.putString(STATE_FIREBASE, binding.tvFirebaseStatus.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
