package com.shasthobondhu.app.ui.fragments.medicines;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shasthobondhu.app.databinding.FragmentMedicinesBinding;
import com.shasthobondhu.app.ui.adapters.MedicineAdapter;
import com.shasthobondhu.app.viewmodel.MedicineViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MedicineFragment extends Fragment {

    private FragmentMedicinesBinding binding;
    private MedicineViewModel viewModel;
    private MedicineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);

        adapter = new MedicineAdapter(new ArrayList<>());
        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedicines.setAdapter(adapter);

        binding.etMedicineSearch.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setQuery(s == null ? "" : s.toString());
            }
        });
        binding.etPharmacyArea.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setArea(s == null ? "" : s.toString());
            }
        });

        viewModel.getMedicines().observe(getViewLifecycleOwner(), medicines -> {
            adapter = new MedicineAdapter(medicines == null ? new ArrayList<>() : medicines);
            binding.rvMedicines.setAdapter(adapter);
            boolean empty = medicines == null || medicines.isEmpty();
            binding.tvEmptyMedicines.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
