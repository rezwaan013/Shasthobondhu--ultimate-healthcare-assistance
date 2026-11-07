package com.shasthobondhu.app.ui.fragments.donors;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.databinding.FragmentDonorRegisterBinding;
import com.shasthobondhu.app.util.AreaDataset;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.viewmodel.DonorViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DonorRegisterFragment extends Fragment {

    public static final String ARG_ONBOARDING = "donorOnboarding";

    private FragmentDonorRegisterBinding binding;
    private DonorViewModel viewModel;
    private String selectedDivision = "";
    private DonorEntity existingProfile;
    private boolean onboarding;
    private List<AreaDataset.Area> areaDivisions;
    private long availableAfterEpoch;
    private boolean willingWhenEligible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDonorRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DonorViewModel.class);
        onboarding = getArguments() != null
                && getArguments().getBoolean(ARG_ONBOARDING, false);

        setupBloodGroups();
        setupAreas();
        binding.etLastDonation.setOnClickListener(v -> showDatePicker());
        binding.etDonorName.setText(SessionManager.getName(requireContext()));
        binding.etDonorPhone.setText(SessionManager.getPhone(requireContext()));

        if (SessionManager.isSignedIn(requireContext())) {
            viewModel.getMyProfile(SessionManager.getUid(requireContext()))
                    .observe(getViewLifecycleOwner(), profile -> {
                        if (profile != null && existingProfile == null) {
                            existingProfile = profile;
                            populateProfile(profile);
                        }
                    });
        }

        binding.btnSaveDonor.setOnClickListener(v -> saveDonor());
        viewModel.getSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved != null && saved && isAdded()) {
                Toast.makeText(requireContext(), R.string.donor_saved, Toast.LENGTH_SHORT).show();
                if (onboarding) {
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_login, true)
                            .setLaunchSingleTop(true)
                            .build();
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.nav_home, null, options);
                } else {
                    NavHostFragment.findNavController(this).popBackStack();
                }
            }
        });
    }

    private void setupBloodGroups() {
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, bloodGroups);
        binding.spinnerBloodGroup.setAdapter(adapter);
        binding.spinnerBloodGroup.setThreshold(0);
        binding.spinnerBloodGroup.setOnClickListener(v ->
                binding.spinnerBloodGroup.showDropDown());
        binding.spinnerBloodGroup.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.spinnerBloodGroup.showDropDown();
            }
        });
    }

    private void setupAreas() {
        areaDivisions = AreaDataset.getDivisions();
        List<String> divisionNames = new ArrayList<>();
        for (AreaDataset.Area division : areaDivisions) {
            divisionNames.add(division.name);
        }
        binding.spinnerDivision.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, divisionNames));
        binding.spinnerDivision.setOnItemClickListener((parent, view, position, id) -> {
            selectedDivision = divisionNames.get(position);
        });
    }

    private void populateProfile(DonorEntity profile) {
        binding.etDonorName.setText(profile.name);
        binding.etDonorPhone.setText(profile.phone);
        binding.spinnerBloodGroup.setText(profile.bloodGroup, false);
        binding.spinnerDivision.setText(profile.division, false);
        selectedDivision = profile.division == null ? "" : profile.division;
        binding.etDistrict.setText(profile.district);
        binding.etUpazila.setText(profile.upazila);
        binding.etLastDonation.setText(formatStoredDate(profile.lastDonationDate));
        binding.switchAvailable.setChecked(profile.isAvailable);
        willingWhenEligible = profile.isAvailable;
        availableAfterEpoch = profile.availableAfterEpoch;
        if (availableAfterEpoch == 0L) {
            Calendar donation = parseDonationDate(profile.lastDonationDate);
            if (donation != null) {
                donation.add(Calendar.MONTH, 2);
                availableAfterEpoch = donation.getTimeInMillis();
            }
        }
        updateAvailabilityControls();
        binding.btnSaveDonor.setText(R.string.action_update_donor);
    }

    private void showDatePicker() {
        Calendar initial = Calendar.getInstance();
        String current = text(binding.etLastDonation);
        if (current.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] parts = current.split("/");
            initial.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[0]));
        }

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (picker, year, month, day) -> {
                    willingWhenEligible = binding.switchAvailable.isEnabled()
                            ? binding.switchAvailable.isChecked() : willingWhenEligible;
                    binding.etLastDonation.setText(String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", day, month + 1, year));
                    Calendar available = Calendar.getInstance();
                    available.clear();
                    available.set(year, month, day);
                    available.add(Calendar.MONTH, 2);
                    availableAfterEpoch = available.getTimeInMillis();
                    updateAvailabilityControls();
                },
                initial.get(Calendar.YEAR), initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH));
        dialog.setTitle(R.string.choose_last_donation_date);
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void updateAvailabilityControls() {
        boolean deferred = availableAfterEpoch > System.currentTimeMillis();
        if (deferred) {
            binding.switchAvailable.setChecked(false);
            binding.switchAvailable.setEnabled(false);
            Calendar available = Calendar.getInstance();
            available.setTimeInMillis(availableAfterEpoch);
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    available.get(Calendar.DAY_OF_MONTH),
                    available.get(Calendar.MONTH) + 1,
                    available.get(Calendar.YEAR));
            binding.tvAvailabilityNote.setText(
                    getString(R.string.auto_unavailable_until, date));
            binding.tvAvailabilityNote.setVisibility(View.VISIBLE);
        } else {
            binding.switchAvailable.setEnabled(true);
            binding.switchAvailable.setChecked(willingWhenEligible);
            binding.tvAvailabilityNote.setVisibility(View.GONE);
        }
    }

    private Calendar parseDonationDate(String date) {
        if (date == null) {
            return null;
        }
        String normalized = formatStoredDate(date);
        if (!normalized.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return null;
        }
        String[] parts = normalized.split("/");
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1,
                Integer.parseInt(parts[0]));
        return calendar;
    }

    private String formatStoredDate(String storedDate) {
        if (storedDate == null || storedDate.isEmpty() || "Not shared".equals(storedDate)) {
            return "";
        }
        if (storedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = storedDate.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return storedDate;
    }

    private void saveDonor() {
        String name = text(binding.etDonorName);
        String phone = text(binding.etDonorPhone);
        String bloodGroup = text(binding.spinnerBloodGroup);
        String district = text(binding.etDistrict);
        String upazila = text(binding.etUpazila);
        String lastDonation = text(binding.etLastDonation);

        hideError();
        if (name.isEmpty()) {
            showError(getString(R.string.hint_full_name));
            return;
        }
        if (phone.length() < 11) {
            showError(getString(R.string.error_invalid_phone));
            return;
        }
        if (bloodGroup.isEmpty()) {
            showError(getString(R.string.choose_blood_group));
            return;
        }
        if (selectedDivision.isEmpty() || district.isEmpty() || upazila.isEmpty()) {
            showError(getString(R.string.error_required));
            return;
        }

        String ownerUid = SessionManager.isSignedIn(requireContext())
                ? SessionManager.getUid(requireContext()) : null;
        DonorEntity donor = new DonorEntity(name, phone, bloodGroup, selectedDivision,
                district, upazila, lastDonation.isEmpty() ? "Not shared" : lastDonation,
                availableAfterEpoch > System.currentTimeMillis()
                        ? willingWhenEligible : binding.switchAvailable.isChecked(), ownerUid);
        donor.availableAfterEpoch = availableAfterEpoch;
        if (existingProfile != null) {
            donor.id = existingProfile.id;
        }
        binding.btnSaveDonor.setEnabled(false);
        viewModel.registerDonor(donor);
    }

    private String text(MaterialAutoCompleteTextView field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String text(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void showError(String message) {
        binding.tvDonorError.setVisibility(View.VISIBLE);
        binding.tvDonorError.setText(message);
    }

    private void hideError() {
        binding.tvDonorError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
