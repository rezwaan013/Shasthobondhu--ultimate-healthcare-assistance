package com.shasthobondhu.app.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentHomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

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
        binding.btnLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_login));
        binding.btnRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_register));
        binding.btnForgotPassword.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_forgot_password));
        binding.cardDonors.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_donors));
        binding.cardRequests.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_requests));
        binding.cardHospitals.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_hospitals));
        binding.cardMedicines.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_medicines));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
