package com.shasthobondhu.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.shasthobondhu.app.databinding.ActivityMainBinding;
import com.shasthobondhu.app.data.local.AppDatabase;
import com.shasthobondhu.app.util.DataSeeder;
import com.shasthobondhu.app.util.ThemeManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    AppDatabase database;

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Seed in the background so the UI thread is never blocked by Room.
        DataSeeder.seedDatabase(database);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateThemeButton();
        binding.btnThemeMode.setOnClickListener(v -> ThemeManager.toggle(this));

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
                    || id == R.id.nav_otp_verify
                    || id == R.id.nav_forgot_password
                    || id == R.id.nav_donor_register
                    || id == R.id.nav_create_request
                    || id == R.id.nav_pharmacy_dashboard
                    || id == R.id.nav_hospital_dashboard;
            binding.bottomNavigation.setVisibility(isAuthScreen ? android.view.View.GONE
                    : android.view.View.VISIBLE);
        });
    }

    private void updateThemeButton() {
        binding.btnThemeMode.setText(ThemeManager.isDarkMode(this)
                ? R.string.light_mode
                : R.string.dark_mode);
    }
}
