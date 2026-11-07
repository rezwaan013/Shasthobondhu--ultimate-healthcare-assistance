package com.shasthobondhu.app.ui.fragments.donors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentDonorsBinding;
import com.shasthobondhu.app.ui.adapters.DonorAdapter;
import com.shasthobondhu.app.viewmodel.DonorViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DonorFragment extends Fragment {

    private FragmentDonorsBinding binding;
    private DonorViewModel viewModel;
    private DonorAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDonorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DonorViewModel.class);

        adapter = new DonorAdapter(new ArrayList<>());
        binding.rvDonors.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDonors.setAdapter(adapter);

        binding.btnSearchDonor.setOnClickListener(v -> {
            viewModel.setAreaFilter(binding.etArea.getText() == null
                    ? "" : binding.etArea.getText().toString());
            viewModel.setBloodGroupFilter(binding.etBloodGroup.getText() == null
                    ? "" : binding.etBloodGroup.getText().toString());
        });

        binding.btnBecomeDonor.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_donor_register));
        binding.btnBecomeDonor.setText(R.string.manage_donor_profile);

        viewModel.getDonors().observe(getViewLifecycleOwner(), donors -> {
            adapter.submit(donors);
            boolean empty = donors == null || donors.isEmpty();
            binding.tvEmptyDonors.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
