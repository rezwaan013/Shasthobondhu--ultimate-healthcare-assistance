package com.shasthobondhu.app.ui.fragments.donors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.shasthobondhu.app.databinding.FragmentDonorsBinding;
import com.shasthobondhu.app.ui.adapters.DonorAdapter;
import com.shasthobondhu.app.viewmodel.DonorViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DonorFragment extends Fragment {
    private FragmentDonorsBinding binding;
    private DonorViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDonorsBinding.inflate(inflater, container, false);
        
        viewModel = new ViewModelProvider(this).get(DonorViewModel.class);
        
        DonorAdapter adapter = new DonorAdapter(new java.util.ArrayList<>());
        binding.rvDonors.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvDonors.setAdapter(adapter);

        binding.btnSearchDonor.setOnClickListener(v -> {
            String bloodGroup = binding.etBloodGroup.getText().toString();
            // In a real implementation, we would call viewModel.setFilters() 
            // and observe the LiveData. For this step, we demonstrate the flow.
            viewModel.setFilters(bloodGroup, "Dhaka", "Dhaka");
        });

        viewModel.getDonors().observe(getViewLifecycleOwner(), donors -> {
            if (donors != null) {
                adapter = new DonorAdapter(donors);
                binding.rvDonors.setAdapter(adapter);
            }
        });

        return binding.getRoot();
    }
}
