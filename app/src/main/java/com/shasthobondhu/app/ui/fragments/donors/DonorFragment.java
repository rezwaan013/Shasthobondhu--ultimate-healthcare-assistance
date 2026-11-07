package com.shasthobondhu.app.ui.fragments.donors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.DonorDetailActivity;
import com.shasthobondhu.app.databinding.FragmentDonorsBinding;
import com.shasthobondhu.app.ui.adapters.DonorAdapter;
import com.shasthobondhu.app.viewmodel.DonorViewModel;
import com.shasthobondhu.app.util.AreaDataset;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DonorFragment extends Fragment {

    private FragmentDonorsBinding binding;
    private DonorViewModel viewModel;
    private DonorAdapter adapter;
    private String selectedDivision = "";

    private final ActivityResultLauncher<Intent> donorDetailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null
                        && result.getData().getBooleanExtra(
                        DonorDetailActivity.RESULT_MARKED_CONTACTED, false)) {
                    Toast.makeText(requireContext(), R.string.donor_marked_contacted,
                            Toast.LENGTH_SHORT).show();
                }
            });

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

        adapter = new DonorAdapter(new ArrayList<>(), this::openDonorDetail);
        binding.rvDonors.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDonors.setAdapter(adapter);

        List<String> divisionNames = new ArrayList<>();
        for (AreaDataset.Area division : AreaDataset.getDivisions()) {
            divisionNames.add(division.name);
        }
        binding.spinnerSearchDivision.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, divisionNames));
        binding.spinnerSearchDivision.setOnItemClickListener((parent, itemView, position, id) ->
                selectedDivision = divisionNames.get(position));

        binding.btnSearchDonor.setOnClickListener(v -> {
            String exactArea = binding.etArea.getText() == null
                    ? "" : binding.etArea.getText().toString().trim();
            if (selectedDivision.isEmpty()) {
                Toast.makeText(requireContext(), R.string.choose_division,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (exactArea.isEmpty()) {
                Toast.makeText(requireContext(), R.string.enter_exact_district_area,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setDivisionFilter(selectedDivision);
            viewModel.setAreaFilter(exactArea);
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
        viewModel.getPresence().observe(getViewLifecycleOwner(), adapter::setPresence);
    }

    private void openDonorDetail(com.shasthobondhu.app.data.local.entities.DonorEntity donor) {
        donorDetailLauncher.launch(DonorDetailActivity.createIntent(requireContext(), donor));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
