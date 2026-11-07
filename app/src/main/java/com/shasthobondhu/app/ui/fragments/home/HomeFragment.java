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
import com.shasthobondhu.app.LabComponentsActivity;
import com.shasthobondhu.app.databinding.FragmentHomeBinding;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AuthViewModel authViewModel;

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

        binding.cardDonors.setOnClickListener(v -> navigate(R.id.nav_donors));
        binding.cardRequests.setOnClickListener(v -> navigate(R.id.nav_requests));
        binding.cardHospitals.setOnClickListener(v -> navigate(R.id.nav_hospitals));
        binding.cardMedicines.setOnClickListener(v -> navigate(R.id.nav_medicines));
        binding.btnQuickRequest.setOnClickListener(v -> navigate(R.id.nav_create_request));
        binding.btnQuickDonate.setOnClickListener(v -> navigate(R.id.nav_donor_register));
        binding.btnLabDemo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LabComponentsActivity.class);
            intent.putExtra(LabComponentsActivity.EXTRA_ENTRY_SOURCE, "MainActivity/HomeFragment");
            startActivity(intent);
        });

        binding.btnSignOut.setOnClickListener(v -> {
            authViewModel.logout();
            NavOptions options = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, true)
                    .setLaunchSingleTop(true)
                    .build();
            NavHostFragment.findNavController(this).navigate(R.id.nav_login, null, options);
        });
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
