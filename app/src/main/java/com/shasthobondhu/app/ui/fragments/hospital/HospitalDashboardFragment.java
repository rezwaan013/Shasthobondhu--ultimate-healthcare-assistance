package com.shasthobondhu.app.ui.fragments.hospital;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.databinding.FragmentHospitalDashboardBinding;
import com.shasthobondhu.app.ui.fragments.home.HomeFragment;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.constants.AppConstants;
import com.shasthobondhu.app.viewmodel.AuthViewModel;
import com.shasthobondhu.app.viewmodel.OwnerViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HospitalDashboardFragment extends Fragment {

    private FragmentHospitalDashboardBinding binding;
    private OwnerViewModel viewModel;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHospitalDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!SessionManager.isHospitalOwner(requireContext())) {
            redirectToCorrectArea();
            return;
        }

        viewModel = new ViewModelProvider(this).get(OwnerViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnCreateHospital.setOnClickListener(v -> createHospital());
        binding.btnAddService.setOnClickListener(v -> addService());
        binding.btnHospitalSignOut.setOnClickListener(v -> signOut());

        viewModel.loadHospital(SessionManager.getUid(requireContext()));
        viewModel.getHospital().observe(getViewLifecycleOwner(), hospital -> {
            if (hospital == null) {
                binding.groupCreateHospital.setVisibility(View.VISIBLE);
                binding.groupManageHospital.setVisibility(View.GONE);
                return;
            }
            binding.groupCreateHospital.setVisibility(View.GONE);
            binding.groupManageHospital.setVisibility(View.VISIBLE);
            binding.tvHospitalName.setText(hospital.name);
            binding.tvHospitalDetails.setText(getString(R.string.facility_created_as,
                    hospital.area, hospital.phone));
            renderServices(hospital);
        });
    }

    private void createHospital() {
        String name = text(binding.etHospitalName);
        String area = text(binding.etHospitalArea);
        String address = text(binding.etHospitalAddress);
        String phone = text(binding.etHospitalPhone);
        hideError();

        if (name.isEmpty() || area.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            showError(getString(R.string.error_required));
            return;
        }
        if (phone.length() < 6) {
            showError(getString(R.string.error_invalid_phone));
            return;
        }

        HospitalEntity hospital = new HospitalEntity(name, address, phone, area, "",
                SessionManager.getUid(requireContext()));
        binding.btnCreateHospital.setEnabled(false);
        viewModel.createHospital(hospital, () -> {
            if (!isAdded()) {
                return;
            }
            binding.btnCreateHospital.setEnabled(true);
            Toast.makeText(requireContext(), R.string.hospital_created, Toast.LENGTH_SHORT).show();
        });
    }

    private void addService() {
        HospitalEntity hospital = viewModel.getHospital().getValue();
        if (hospital == null) {
            showError(getString(R.string.error_required));
            return;
        }
        String service = text(binding.etServiceName);
        if (service.isEmpty()) {
            showError(getString(R.string.error_required));
            return;
        }
        hideError();

        List<String> services = splitServices(hospital.services);
        for (String existing : services) {
            if (existing.equalsIgnoreCase(service)) {
                Toast.makeText(requireContext(), R.string.service_added, Toast.LENGTH_SHORT).show();
                binding.etServiceName.setText("");
                return;
            }
        }
        services.add(service);
        hospital.services = joinServices(services);
        binding.btnAddService.setEnabled(false);
        viewModel.updateHospital(hospital, () -> {
            if (!isAdded()) {
                return;
            }
            binding.btnAddService.setEnabled(true);
            binding.etServiceName.setText("");
            Toast.makeText(requireContext(), R.string.service_added, Toast.LENGTH_SHORT).show();
        });
    }

    private void removeService(String service) {
        HospitalEntity hospital = viewModel.getHospital().getValue();
        if (hospital == null) {
            return;
        }
        List<String> services = splitServices(hospital.services);
        for (int i = services.size() - 1; i >= 0; i--) {
            if (services.get(i).equalsIgnoreCase(service)) {
                services.remove(i);
            }
        }
        hospital.services = joinServices(services);
        viewModel.updateHospital(hospital, () ->
                Toast.makeText(requireContext(), R.string.service_removed, Toast.LENGTH_SHORT).show());
    }

    private void renderServices(HospitalEntity hospital) {
        if (binding == null) {
            return;
        }
        binding.chipServices.removeAllViews();
        List<String> services = splitServices(hospital.services);
        boolean empty = services.isEmpty();
        binding.tvNoServices.setVisibility(empty ? View.VISIBLE : View.GONE);
        for (String service : services) {
            Chip chip = new Chip(requireContext());
            chip.setText(service);
            chip.setCheckable(false);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(
                    requireContext(), R.color.primary_container)));
            chip.setTextColor(ContextCompat.getColor(requireContext(),
                    R.color.on_primary_container));
            chip.setOnCloseIconClickListener(v -> removeService(service));
            binding.chipServices.addView(chip);
        }
    }

    private List<String> splitServices(String raw) {
        List<String> services = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return services;
        }
        for (String part : raw.split(",")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                services.add(value);
            }
        }
        return services;
    }

    private String joinServices(List<String> services) {
        StringBuilder builder = new StringBuilder();
        for (String service : services) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(service);
        }
        return builder.toString();
    }

    private void signOut() {
        authViewModel.logout();
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_home, true)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.nav_home, null, options);
    }

    private void redirectToCorrectArea() {
        if (SessionManager.isPharmacyOwner(requireContext())) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_pharmacy_dashboard);
            return;
        }
        Bundle args = new Bundle();
        args.putString(HomeFragment.ARG_OWNER_ROLE, AppConstants.ROLE_HOSPITAL);
        NavHostFragment.findNavController(this).navigate(R.id.nav_login, args);
    }

    private String text(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void showError(String message) {
        binding.tvHospitalError.setVisibility(View.VISIBLE);
        binding.tvHospitalError.setText(message);
    }

    private void hideError() {
        binding.tvHospitalError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
