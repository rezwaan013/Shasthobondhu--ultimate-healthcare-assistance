package com.shasthobondhu.app.ui.fragments.hospitals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.shasthobondhu.app.databinding.FragmentHospitalsBinding;
import com.shasthobondhu.app.ui.adapters.HospitalAdapter;
import com.shasthobondhu.app.viewmodel.HospitalViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HospitalFragment extends Fragment {
    private FragmentHospitalsBinding binding;
    private HospitalViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHospitalsBinding.inflate(inflater, container, false);
        
        viewModel = new ViewModelProvider(this).get(HospitalViewModel.class);
        
        HospitalAdapter adapter = new HospitalAdapter(new java.util.ArrayList<>());
        binding.rvHospitals.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvHospitals.setAdapter(adapter);

        binding.etHospitalSearch.setOnKeyListener((v, keyCode, event) -> {
            // Simplified search trigger
            return false;
        });

        viewModel.searchHospitals("Dhaka").observe(getViewLifecycleOwner(), hospitals -> {
            if (hospitals != null) {
                binding.rvHospitals.setAdapter(new HospitalAdapter(hospitals));
            }
        });

        return binding.getRoot();
    }
}
