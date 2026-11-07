package com.shasthobondhu.app;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.shasthobondhu.app.data.model.EmergencyContact;
import com.shasthobondhu.app.data.model.DonorPresence;
import com.shasthobondhu.app.data.model.HealthStats;
import com.shasthobondhu.app.data.model.Resource;
import com.shasthobondhu.app.data.repository.SystemStatusRepository;
import com.shasthobondhu.app.data.repository.MonitorPreferences;
import com.shasthobondhu.app.data.repository.RealtimeDatabaseRepository;
import com.shasthobondhu.app.databinding.ActivityEmergencyHubBinding;
import com.shasthobondhu.app.databinding.DialogContactPickerBinding;
import com.shasthobondhu.app.system.SystemEvent;
import com.shasthobondhu.app.system.SystemEventReceiver;
import com.shasthobondhu.app.service.HealthMonitorService;
import com.shasthobondhu.app.util.PermissionHelper;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.viewmodel.EmergencyHubViewModel;
import com.shasthobondhu.app.viewmodel.EmergencyContactViewModel;
import com.shasthobondhu.app.viewmodel.HealthInfoViewModel;
import com.shasthobondhu.app.ui.adapters.ContactAdapter;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;

/** Purpose-built emergency workspace launched from the main health-support flow. */
@AndroidEntryPoint
public class EmergencyHubActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "com.shasthobondhu.app.extra.USER_ID";
    public static final String EXTRA_USER_NAME = "com.shasthobondhu.app.extra.USER_NAME";
    public static final String EXTRA_BLOOD_GROUP = "com.shasthobondhu.app.extra.BLOOD_GROUP";
    public static final String EXTRA_DISTRICT = "com.shasthobondhu.app.extra.DISTRICT";

    private ActivityEmergencyHubBinding binding;
    private EmergencyHubViewModel viewModel;
    private EmergencyContactViewModel contactViewModel;
    private HealthInfoViewModel healthInfoViewModel;
    private PermissionHelper permissionHelper;
    private boolean changingMonitorSwitch;
    private boolean changingAvailabilitySwitch;
    private boolean donorAvailable;
    private boolean userOnline;
    private SystemEventReceiver systemEventReceiver;
    private boolean receiverRegistered;
    private SystemEvent.Type dismissedSystemEvent;

    @Inject
    SystemStatusRepository systemStatusRepository;
    private BottomSheetDialog contactDialog;
    private DialogContactPickerBinding contactDialogBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyHubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        permissionHelper = new PermissionHelper(this);
        systemEventReceiver = new SystemEventReceiver(systemStatusRepository::publish);
        systemStatusRepository.getStatus().observe(this, this::renderSystemStatus);

        viewModel = new ViewModelProvider(this).get(EmergencyHubViewModel.class);
        contactViewModel = new ViewModelProvider(this).get(EmergencyContactViewModel.class);
        healthInfoViewModel = new ViewModelProvider(this).get(HealthInfoViewModel.class);
        Intent intent = getIntent();
        viewModel.initialize(
                intent == null ? null : intent.getStringExtra(EXTRA_USER_ID),
                intent == null ? null : intent.getStringExtra(EXTRA_USER_NAME),
                intent == null ? null : intent.getStringExtra(EXTRA_BLOOD_GROUP),
                intent == null ? null : intent.getStringExtra(EXTRA_DISTRICT));

        binding.btnHubBack.setOnClickListener(v -> finish());
        binding.btnViewRequests.setOnClickListener(v -> {
            Intent main = new Intent(this, MainActivity.class);
            main.putExtra(MainActivity.EXTRA_DESTINATION, R.id.nav_requests);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(main);
        });
        binding.switchMonitor.setOnCheckedChangeListener((button, checked) -> {
            if (changingMonitorSwitch) {
                return;
            }
            if (checked) {
                requestNotificationPermission();
            } else {
                stopEmergencyMonitor();
            }
        });
        binding.btnChooseContact.setOnClickListener(v -> requestContactsPermission());
        binding.btnDismissStatus.setOnClickListener(v -> {
            SystemEvent event = systemStatusRepository.getStatus().getValue();
            dismissedSystemEvent = event == null ? null : event.type;
            binding.systemStatusBanner.setVisibility(View.GONE);
        });

        bindContext();
        observeRequests();
        showFeaturePlaceholders();
        bindEmergencyContact();
        restoreMonitorState();
        bindHealthStats();
        bindPresence();
    }

    private void renderSystemStatus(SystemEvent event) {
        if (event == null || !event.active) {
            binding.systemStatusBanner.setVisibility(View.GONE);
            dismissedSystemEvent = null;
            return;
        }
        if (event.type == dismissedSystemEvent) {
            binding.systemStatusBanner.setVisibility(View.GONE);
            return;
        }
        dismissedSystemEvent = null;
        binding.tvSystemStatus.setText(event.messageRes);
        binding.systemStatusBanner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(this, systemEventReceiver,
                    SystemEventReceiver.createIntentFilter(), ContextCompat.RECEIVER_NOT_EXPORTED);
            receiverRegistered = true;
            systemEventReceiver.publishCurrentWarnings(this);
        }
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
    protected void onDestroy() {
        if (contactDialog != null) {
            contactDialog.dismiss();
        }
        contactDialogBinding = null;
        contactDialog = null;
        super.onDestroy();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onNotificationPermissionGranted();
            return;
        }
        permissionHelper.request(Manifest.permission.POST_NOTIFICATIONS,
                R.string.notification_permission_rationale_title,
                R.string.notification_permission_rationale_message,
                new PermissionHelper.Callback() {
                    @Override
                    public void onGranted() {
                        onNotificationPermissionGranted();
                    }

                    @Override
                    public void onDenied(boolean permanentlyDenied) {
                        resetMonitorSwitch();
                        binding.tvMonitorStatus.setText(permanentlyDenied
                                ? R.string.notification_permission_settings_needed
                                : R.string.notification_permission_denied_status);
                    }
                });
    }

    private void onNotificationPermissionGranted() {
        startEmergencyMonitor();
    }

    private void resetMonitorSwitch() {
        changingMonitorSwitch = true;
        binding.switchMonitor.setChecked(false);
        changingMonitorSwitch = false;
    }

    private void restoreMonitorState() {
        boolean running = MonitorPreferences.isRunning(this);
        changingMonitorSwitch = true;
        binding.switchMonitor.setChecked(running);
        changingMonitorSwitch = false;
        binding.tvMonitorStatus.setText(running
                ? R.string.monitor_running : R.string.monitor_not_running);
    }

    private void startEmergencyMonitor() {
        Intent service = new Intent(this, HealthMonitorService.class)
                .setAction(HealthMonitorService.ACTION_START)
                .putExtra(HealthMonitorService.EXTRA_BLOOD_GROUP, viewModel.getBloodGroup())
                .putExtra(HealthMonitorService.EXTRA_DISTRICT, viewModel.getDistrict());
        try {
            ContextCompat.startForegroundService(this, service);
            MonitorPreferences.setRunning(this, true,
                    viewModel.getBloodGroup(), viewModel.getDistrict());
            binding.tvMonitorStatus.setText(R.string.monitor_running);
            Toast.makeText(this, R.string.monitor_started, Toast.LENGTH_SHORT).show();
        } catch (RuntimeException error) {
            resetMonitorSwitch();
            binding.tvMonitorStatus.setText(R.string.monitor_start_failed);
        }
    }

    private void stopEmergencyMonitor() {
        stopService(new Intent(this, HealthMonitorService.class));
        MonitorPreferences.setRunning(this, false,
                viewModel.getBloodGroup(), viewModel.getDistrict());
        binding.tvMonitorStatus.setText(R.string.monitor_not_running);
    }

    private void requestContactsPermission() {
        permissionHelper.request(Manifest.permission.READ_CONTACTS,
                R.string.contacts_permission_rationale_title,
                R.string.contacts_permission_rationale_message,
                new PermissionHelper.Callback() {
                    @Override
                    public void onGranted() {
                        openContactPicker();
                        contactViewModel.loadContacts();
                    }

                    @Override
                    public void onDenied(boolean permanentlyDenied) {
                        binding.tvEmergencyContact.setText(permanentlyDenied
                                ? R.string.contacts_permission_settings_needed
                                : R.string.contacts_permission_denied_status);
                    }
                });
    }

    private void bindEmergencyContact() {
        contactViewModel.getSelectedContact().observe(this, contact -> {
            boolean hasContact = contact != null && !contact.phone.isEmpty();
            binding.tvEmergencyContact.setText(hasContact
                    ? getString(R.string.emergency_contact_value, contact.name, contact.phone)
                    : getString(R.string.no_emergency_contact));
            binding.btnCallContact.setEnabled(hasContact);
            binding.btnShareSms.setEnabled(hasContact);
        });
        contactViewModel.getLoading().observe(this, loading -> {
            if (contactDialogBinding != null) {
                contactDialogBinding.progressContacts.setVisibility(
                        Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            }
        });
        contactViewModel.getContacts().observe(this, contacts -> {
            if (contactDialogBinding == null || contacts == null) {
                return;
            }
            contactDialogBinding.rvContacts.setAdapter(new ContactAdapter(contacts, contact -> {
                contactViewModel.select(contact);
                if (contactDialog != null) {
                    contactDialog.dismiss();
                }
            }));
            contactDialogBinding.tvContactsEmpty.setText(R.string.no_phone_contacts);
            contactDialogBinding.tvContactsEmpty.setVisibility(
                    contacts.isEmpty() ? View.VISIBLE : View.GONE);
        });
        contactViewModel.getLoadFailed().observe(this, failed -> {
            if (contactDialogBinding != null && Boolean.TRUE.equals(failed)) {
                contactDialogBinding.tvContactsEmpty.setText(R.string.contacts_load_failed);
                contactDialogBinding.tvContactsEmpty.setVisibility(View.VISIBLE);
            }
        });
        binding.btnCallContact.setOnClickListener(v -> callEmergencyContact());
        binding.btnShareSms.setOnClickListener(v -> shareEmergencySms());
    }

    private void openContactPicker() {
        if (contactDialog != null && contactDialog.isShowing()) {
            return;
        }
        contactDialogBinding = DialogContactPickerBinding.inflate(getLayoutInflater());
        contactDialogBinding.rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contactDialog = new BottomSheetDialog(this);
        contactDialog.setContentView(contactDialogBinding.getRoot());
        contactDialog.setOnDismissListener(dialog -> {
            contactDialogBinding = null;
            contactDialog = null;
        });
        contactDialog.show();
    }

    private void callEmergencyContact() {
        EmergencyContact contact = contactViewModel.getSelectedContact().getValue();
        if (contact == null || contact.phone.isEmpty()) {
            return;
        }
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.phone));
        if (dial.resolveActivity(getPackageManager()) != null) {
            startActivity(dial);
        } else {
            Toast.makeText(this, R.string.no_dialer_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareEmergencySms() {
        EmergencyContact contact = contactViewModel.getSelectedContact().getValue();
        if (contact == null || contact.phone.isEmpty()) {
            return;
        }
        String name = viewModel.getUserName().isEmpty()
                ? getString(R.string.app_name) : viewModel.getUserName();
        String bloodGroup = viewModel.getBloodGroup().isEmpty()
                ? getString(R.string.not_shared) : viewModel.getBloodGroup();
        String district = viewModel.getDistrict().isEmpty()
                ? getString(R.string.not_shared) : viewModel.getDistrict();
        Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contact.phone));
        sms.putExtra("sms_body", getString(
                R.string.emergency_sms_body, name, bloodGroup, district));
        if (sms.resolveActivity(getPackageManager()) != null) {
            startActivity(sms);
        } else {
            Toast.makeText(this, R.string.no_sms_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void bindHealthStats() {
        healthInfoViewModel.getStats().observe(this, this::renderHealthStats);
        binding.btnRetryHealthStats.setOnClickListener(v -> healthInfoViewModel.loadStats());
        if (healthInfoViewModel.getStats().getValue() == null) {
            healthInfoViewModel.loadStats();
        }
    }

    private void renderHealthStats(Resource<HealthStats> resource) {
        if (resource == null) {
            return;
        }
        binding.progressHealthStats.setVisibility(
                resource.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
        binding.btnRetryHealthStats.setVisibility(View.GONE);
        if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
            NumberFormat number = NumberFormat.getIntegerInstance();
            String updated = resource.data.updatedAt <= 0 ? getString(R.string.not_shared)
                    : DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(new Date(resource.data.updatedAt));
            binding.tvHealthStats.setText(getString(R.string.health_stats_value,
                    number.format(resource.data.cases), number.format(resource.data.deaths),
                    number.format(resource.data.tests), number.format(resource.data.population),
                    updated));
        } else if (resource.status == Resource.Status.EMPTY) {
            binding.tvHealthStats.setText(R.string.health_stats_empty);
        } else if (resource.status == Resource.Status.ERROR) {
            binding.tvHealthStats.setText(apiErrorMessage(resource.errorType));
            binding.btnRetryHealthStats.setVisibility(View.VISIBLE);
        }
    }

    private int apiErrorMessage(Resource.ErrorType type) {
        if (type == Resource.ErrorType.NETWORK) {
            return R.string.api_network_error;
        }
        if (type == Resource.ErrorType.MALFORMED) {
            return R.string.api_data_error;
        }
        return R.string.api_server_error;
    }

    private void bindPresence() {
        boolean usable = viewModel.isPresenceConfigured()
                && !SessionManager.isLocalMode(this)
                && !viewModel.getUserId().isEmpty();
        binding.switchAvailability.setEnabled(false);
        if (!viewModel.isPresenceConfigured()) {
            binding.tvPresenceStatus.setText(R.string.presence_not_configured);
            return;
        }
        if (!usable) {
            binding.tvPresenceStatus.setText(R.string.presence_firebase_sign_in_required);
            return;
        }
        viewModel.getPresence().observe(this, this::renderOwnPresence);
        viewModel.getDonorProfile().observe(this, donor -> {
            binding.switchAvailability.setEnabled(donor != null);
            if (donor == null) {
                donorAvailable = false;
                binding.tvPresenceStatus.setText(R.string.presence_donor_profile_required);
                return;
            }
            donorAvailable = donor != null && donor.isAvailable;
            changingAvailabilitySwitch = true;
            binding.switchAvailability.setChecked(donorAvailable);
            changingAvailabilitySwitch = false;
            renderAvailabilityStatus();
        });
        binding.switchAvailability.setOnCheckedChangeListener((button, checked) -> {
            if (changingAvailabilitySwitch) {
                return;
            }
            binding.switchAvailability.setEnabled(false);
            binding.tvPresenceStatus.setText(R.string.presence_updating);
            viewModel.setAvailableNow(checked, (success, failure) -> runOnUiThread(() -> {
                binding.switchAvailability.setEnabled(true);
                if (success) {
                    donorAvailable = checked;
                    renderAvailabilityStatus();
                } else {
                    changingAvailabilitySwitch = true;
                    binding.switchAvailability.setChecked(!checked);
                    changingAvailabilitySwitch = false;
                    binding.tvPresenceStatus.setText(presenceError(failure));
                }
            }));
        });
    }

    private void renderOwnPresence(Map<String, DonorPresence> values) {
        DonorPresence presence = values == null ? null : values.get(viewModel.getUserId());
        userOnline = presence != null && presence.online;
        renderAvailabilityStatus();
    }

    private void renderAvailabilityStatus() {
        if (donorAvailable && userOnline) {
            binding.tvPresenceStatus.setText(R.string.presence_available_now);
        } else if (donorAvailable) {
            binding.tvPresenceStatus.setText(R.string.presence_available_offline);
        } else {
            binding.tvPresenceStatus.setText(R.string.presence_not_available);
        }
    }

    private int presenceError(RealtimeDatabaseRepository.Failure failure) {
        if (failure == RealtimeDatabaseRepository.Failure.NOT_CONFIGURED) {
            return R.string.presence_not_configured;
        }
        if (failure == RealtimeDatabaseRepository.Failure.UNAUTHENTICATED) {
            return R.string.presence_firebase_sign_in_required;
        }
        return R.string.presence_update_failed;
    }

    private void bindContext() {
        String name = viewModel.getUserName();
        binding.tvHubGreeting.setText(name.isEmpty()
                ? getString(R.string.emergency_hub_title)
                : getString(R.string.emergency_hub_greeting, name));

        String bloodGroup = viewModel.getBloodGroup();
        String district = viewModel.getDistrict();
        if (bloodGroup.isEmpty() && district.isEmpty()) {
            binding.tvHubContext.setText(R.string.emergency_context_missing);
        } else {
            binding.tvHubContext.setText(getString(R.string.emergency_context_value,
                    bloodGroup.isEmpty() ? getString(R.string.not_shared) : bloodGroup,
                    district.isEmpty() ? getString(R.string.not_shared) : district));
        }
    }

    private void observeRequests() {
        viewModel.getOpenRequestCount().observe(this, count ->
                binding.tvHubSosCount.setText(getResources().getQuantityString(
                        R.plurals.open_request_count, count == null ? 0 : count,
                        count == null ? 0 : count)));
    }

    private void showFeaturePlaceholders() {
        binding.tvEmergencyContact.setText(R.string.no_emergency_contact);
        binding.btnCallContact.setEnabled(false);
        binding.btnShareSms.setEnabled(false);
        binding.tvMonitorStatus.setText(R.string.monitor_not_running);
        binding.tvPresenceStatus.setText(R.string.presence_setup_pending);
        binding.tvHealthStats.setText(R.string.health_stats_ready);
        binding.progressHealthStats.setVisibility(View.GONE);
        binding.btnRetryHealthStats.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
