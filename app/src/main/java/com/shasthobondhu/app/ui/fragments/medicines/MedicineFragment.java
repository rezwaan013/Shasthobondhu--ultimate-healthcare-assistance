package com.shasthobondhu.app.ui.fragments.medicines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.shasthobondhu.app.databinding.FragmentMedicinesBinding;
import com.shasthobondhu.app.ui.adapters.MedicineAdapter;
import com.shasthobondhu.app.viewmodel.MedicineViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MedicineFragment extends Fragment {
    private FragmentMedicinesBinding binding;
    private MedicineViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMedicinesBinding.inflate(inflater, container, false);
        
        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);
        
        MedicineAdapter adapter = new MedicineAdapter(new java.util.ArrayList<>());
        binding.rvMedicines.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvMedicines.setAdapter(adapter);

        viewModel.searchMedicines("Napa").observe(getViewLifecycleOwner(), medicines -> {
            if (medicines != null) {
                binding.rvMedicines.setAdapter(new MedicineAdapter(medicines));
            }
        });

        return binding.getRoot();
    }
}
