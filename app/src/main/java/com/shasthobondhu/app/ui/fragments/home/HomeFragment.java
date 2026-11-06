package com.shasthobondhu.app.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentHomeBinding;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.constants.AppConstants;
import com.shasthobondhu.app.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    public static final String ARG_OWNER_ROLE = "ownerRole";

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

        binding.btnLogin.setOnClickListener(v -> openLogin(null));
        binding.btnRegister.setOnClickListener(v -> openRegister(null));
        binding.btnForgotPassword.setOnClickListener(v -> navigate(R.id.nav_forgot_password));

        binding.btnPharmacyOwner.setOnClickListener(v ->
                openOwnerArea(AppConstants.ROLE_PHARMACY));
        binding.btnHospitalOwner.setOnClickListener(v ->
                openOwnerArea(AppConstants.ROLE_HOSPITAL));

        binding.btnOpenDashboard.setOnClickListener(v -> openDashboard());
        binding.btnSignOut.setOnClickListener(v -> {
            authViewModel.logout();
            refreshAccount();
            Toast.makeText(requireContext(), R.string.sign_out, Toast.LENGTH_SHORT).show();
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
        boolean owner = SessionManager.isPharmacyOwner(requireContext())
                || SessionManager.isHospitalOwner(requireContext());

        binding.btnLogin.setVisibility(signedIn ? View.GONE : View.VISIBLE);
        binding.btnRegister.setVisibility(signedIn ? View.GONE : View.VISIBLE);
        binding.btnForgotPassword.setVisibility(signedIn ? View.GONE : View.VISIBLE);
        binding.btnOpenDashboard.setVisibility(signedIn && owner ? View.VISIBLE : View.GONE);
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
            binding.tvAccountTitle.setText(R.string.account_guest_title);
            binding.tvAccountNote.setText(R.string.account_guest_note);
        }
    }

    private void openOwnerArea(String role) {
        if (!SessionManager.isSignedIn(requireContext())) {
            openLogin(role);
            return;
        }
        String currentRole = SessionManager.getRole(requireContext());
        if (role.equals(currentRole)) {
            openDashboard();
            return;
        }
        if (SessionManager.isPharmacyOwner(requireContext())
                || SessionManager.isHospitalOwner(requireContext())) {
            Toast.makeText(requireContext(),
                    getString(R.string.owner_role_changed,
                            SessionManager.roleLabel(requireContext(), currentRole)),
                    Toast.LENGTH_LONG).show();
            openDashboard();
            return;
        }
        // A general user cannot own a facility; sign in with an owner account.
        openLogin(role);
    }

    private void openDashboard() {
        String role = SessionManager.getRole(requireContext());
        if (AppConstants.ROLE_PHARMACY.equals(role)) {
            navigate(R.id.nav_pharmacy_dashboard);
        } else if (AppConstants.ROLE_HOSPITAL.equals(role)) {
            navigate(R.id.nav_hospital_dashboard);
        } else {
            Toast.makeText(requireContext(), R.string.error_owner_only, Toast.LENGTH_LONG).show();
        }
    }

    private void openLogin(String role) {
        Bundle args = new Bundle();
        if (role != null) {
            args.putString(ARG_OWNER_ROLE, role);
        }
        NavHostFragment.findNavController(this).navigate(R.id.nav_login, args);
    }

    private void openRegister(String role) {
        Bundle args = new Bundle();
        if (role != null) {
            args.putString(ARG_OWNER_ROLE, role);
        }
        NavHostFragment.findNavController(this).navigate(R.id.nav_register, args);
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
