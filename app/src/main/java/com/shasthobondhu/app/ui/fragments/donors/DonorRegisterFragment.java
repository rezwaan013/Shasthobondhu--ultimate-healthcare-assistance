package com.shasthobondhu.app.ui.fragments.donors;

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
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DonorRegisterFragment extends Fragment {

    public static final String ARG_ONBOARDING = "donorOnboarding";

    private FragmentDonorRegisterBinding binding;
    private DonorViewModel viewModel;
    private String selectedDivision = "";
    private String selectedDistrict = "";
    private DonorEntity existingProfile;
    private boolean onboarding;
    private List<AreaDataset.Area> areaDivisions;

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
            selectedDistrict = "";
            binding.spinnerDistrict.setText("", false);
            List<AreaDataset.Area> districts = areaDivisions.get(position).children;
            List<String> districtNames = new ArrayList<>();
            for (AreaDataset.Area district : districts) {
                districtNames.add(district.name);
            }
            binding.spinnerDistrict.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, districtNames));
        });
        binding.spinnerDistrict.setOnItemClickListener((parent, view, position, id) ->
                selectedDistrict = binding.spinnerDistrict.getText().toString().trim());
    }

    private void populateProfile(DonorEntity profile) {
        binding.etDonorName.setText(profile.name);
        binding.etDonorPhone.setText(profile.phone);
        binding.spinnerBloodGroup.setText(profile.bloodGroup, false);
        binding.spinnerDivision.setText(profile.division, false);
        selectedDivision = profile.division == null ? "" : profile.division;
        selectedDistrict = profile.district == null ? "" : profile.district;

        List<String> districtNames = new ArrayList<>();
        for (AreaDataset.Area division : areaDivisions) {
            if (division.name.equals(selectedDivision)) {
                for (AreaDataset.Area district : division.children) {
                    districtNames.add(district.name);
                }
                break;
            }
        }
        binding.spinnerDistrict.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, districtNames));
        binding.spinnerDistrict.setText(profile.district, false);
        binding.etUpazila.setText(profile.upazila);
        binding.etLastDonation.setText("Not shared".equals(profile.lastDonationDate)
                ? "" : profile.lastDonationDate);
        binding.switchAvailable.setChecked(profile.isAvailable);
        binding.btnSaveDonor.setText(R.string.action_update_donor);
    }

    private void saveDonor() {
        String name = text(binding.etDonorName);
        String phone = text(binding.etDonorPhone);
        String bloodGroup = text(binding.spinnerBloodGroup);
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
        if (selectedDivision.isEmpty() || selectedDistrict.isEmpty() || upazila.isEmpty()) {
            showError(getString(R.string.error_required));
            return;
        }

        String ownerUid = SessionManager.isSignedIn(requireContext())
                ? SessionManager.getUid(requireContext()) : null;
        DonorEntity donor = new DonorEntity(name, phone, bloodGroup, selectedDivision,
                selectedDistrict, upazila, lastDonation.isEmpty() ? "Not shared" : lastDonation,
                binding.switchAvailable.isChecked(), ownerUid);
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
