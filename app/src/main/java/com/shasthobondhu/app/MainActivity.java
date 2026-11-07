package com.shasthobondhu.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.shasthobondhu.app.databinding.ActivityMainBinding;
import com.shasthobondhu.app.data.local.AppDatabase;
import com.shasthobondhu.app.data.repository.SystemStatusRepository;
import com.shasthobondhu.app.system.SystemEvent;
import com.shasthobondhu.app.system.SystemEventReceiver;
import com.shasthobondhu.app.util.DataSeeder;
import com.shasthobondhu.app.util.ThemeManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_DESTINATION = "com.shasthobondhu.app.extra.DESTINATION";

    @Inject
    AppDatabase database;

    @Inject
    SystemStatusRepository systemStatusRepository;

    private ActivityMainBinding binding;
    private NavController navController;
    private SystemEventReceiver systemEventReceiver;
    private boolean receiverRegistered;
    private SystemEvent.Type dismissedSystemEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Seed in the background so the UI thread is never blocked by Room.
        DataSeeder.seedDatabase(database);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateThemeButton();
        binding.btnThemeMode.setOnClickListener(v -> ThemeManager.toggle(this));
        binding.btnDismissStatus.setOnClickListener(v -> {
            SystemEvent event = systemStatusRepository.getStatus().getValue();
            dismissedSystemEvent = event == null ? null : event.type;
            binding.systemStatusBanner.setVisibility(View.GONE);
        });
        systemStatusRepository.getStatus().observe(this, this::renderSystemStatus);
        systemEventReceiver = new SystemEventReceiver(systemStatusRepository::publish);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return;
        }
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean isAuthScreen = id == R.id.nav_login
                    || id == R.id.nav_register
                    || id == R.id.nav_forgot_password
                    || id == R.id.nav_donor_register
                    || id == R.id.nav_create_request
                    || id == R.id.nav_pharmacy_dashboard
                    || id == R.id.nav_hospital_dashboard;
            binding.bottomNavigation.setVisibility(isAuthScreen ? android.view.View.GONE
                    : android.view.View.VISIBLE);
        });
        handleDestinationIntent(getIntent());
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDestinationIntent(intent);
    }

    private void handleDestinationIntent(Intent intent) {
        if (navController == null || intent == null) {
            return;
        }
        int destination = intent.getIntExtra(EXTRA_DESTINATION, 0);
        if (destination == R.id.nav_requests) {
            navController.navigate(R.id.nav_requests);
            intent.removeExtra(EXTRA_DESTINATION);
        }
    }

    private void updateThemeButton() {
        binding.btnThemeMode.setText(ThemeManager.isDarkMode(this)
                ? R.string.light_mode
                : R.string.dark_mode);
    }
}
