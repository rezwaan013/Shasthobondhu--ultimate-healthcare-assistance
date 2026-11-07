package com.shasthobondhu.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shasthobondhu.app.databinding.ActivityEmergencyHubBinding;
import com.shasthobondhu.app.viewmodel.EmergencyHubViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/** Purpose-built emergency workspace launched from the main health-support flow. */
@AndroidEntryPoint
public class EmergencyHubActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "com.shasthobondhu.app.extra.USER_ID";
    public static final String EXTRA_USER_NAME = "com.shasthobondhu.app.extra.USER_NAME";
    public static final String EXTRA_BLOOD_GROUP = "com.shasthobondhu.app.extra.BLOOD_GROUP";
    public static final String EXTRA_DISTRICT = "com.shasthobondhu.app.extra.DISTRICT";

    private ActivityEmergencyHubBinding binding;
    private EmergencyHubViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyHubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EmergencyHubViewModel.class);
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

        bindContext();
        observeRequests();
        showFeaturePlaceholders();
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
        binding.systemStatusBanner.setVisibility(View.GONE);
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
