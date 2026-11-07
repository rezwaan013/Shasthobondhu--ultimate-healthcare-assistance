package com.shasthobondhu.app.ui.fragments.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.EmergencyHubActivity;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.databinding.FragmentHomeBinding;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.viewmodel.AuthViewModel;
import com.shasthobondhu.app.viewmodel.DonorViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AuthViewModel authViewModel;
    private String donorBloodGroup = "";
    private String donorDistrict = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        DonorViewModel donorViewModel = new ViewModelProvider(this).get(DonorViewModel.class);

        binding.cardDonors.setOnClickListener(v -> navigate(R.id.nav_donors));
        binding.cardRequests.setOnClickListener(v -> navigate(R.id.nav_requests));
        binding.cardHospitals.setOnClickListener(v -> navigate(R.id.nav_hospitals));
        binding.cardMedicines.setOnClickListener(v -> navigate(R.id.nav_medicines));
        binding.btnQuickRequest.setOnClickListener(v -> navigate(R.id.nav_create_request));
        binding.btnQuickDonate.setOnClickListener(v -> navigate(R.id.nav_donor_register));
        binding.btnEmergencyHub.setOnClickListener(v -> openEmergencyHub());
        binding.btnSignOut.setOnClickListener(v -> {
            authViewModel.logout();
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, true)
                    .setLaunchSingleTop(true)
                    .build();
            NavHostFragment.findNavController(this).navigate(R.id.nav_login, null, options);
        });

        String uid = SessionManager.getUid(requireContext());
        donorViewModel.getMyProfile(uid).observe(getViewLifecycleOwner(), this::cacheDonorContext);
    }

    private void cacheDonorContext(DonorEntity donor) {
        if (donor == null) {
            donorBloodGroup = "";
            donorDistrict = "";
            return;
        }
        donorBloodGroup = donor.bloodGroup == null ? "" : donor.bloodGroup;
        donorDistrict = donor.district == null ? "" : donor.district;
    }

    private void openEmergencyHub() {
        Intent intent = new Intent(requireContext(), EmergencyHubActivity.class);
        intent.putExtra(EmergencyHubActivity.EXTRA_USER_ID,
                SessionManager.getUid(requireContext()));
        intent.putExtra(EmergencyHubActivity.EXTRA_USER_NAME,
                SessionManager.getName(requireContext()));
        intent.putExtra(EmergencyHubActivity.EXTRA_BLOOD_GROUP, donorBloodGroup);
        intent.putExtra(EmergencyHubActivity.EXTRA_DISTRICT, donorDistrict);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAccount();
    }

    private void refreshAccount() {
        if (binding == null || !isAdded()) {
            return;
        }
        boolean signedIn = SessionManager.isSignedIn(requireContext());
        binding.btnSignOut.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        binding.tvLocalModeNotice.setVisibility(
                signedIn && SessionManager.isLocalMode(requireContext())
                        ? View.VISIBLE : View.GONE);

        if (signedIn) {
            String name = SessionManager.getName(requireContext());
            if (name == null || name.trim().isEmpty()) {
                name = SessionManager.getEmail(requireContext());
            }
            binding.tvAccountTitle.setText(getString(R.string.signed_in_as, name));
            binding.tvAccountNote.setText(getString(R.string.signed_in_role,
                    SessionManager.roleLabel(requireContext(),
                            SessionManager.getRole(requireContext()))));
        } else {
            NavHostFragment.findNavController(this).navigate(R.id.nav_login);
        }
    }

    private void navigate(int destination) {
        NavHostFragment.findNavController(this).navigate(destination);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
