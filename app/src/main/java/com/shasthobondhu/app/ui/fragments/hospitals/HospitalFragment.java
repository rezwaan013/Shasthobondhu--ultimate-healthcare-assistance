package com.shasthobondhu.app.ui.fragments.hospitals;

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

import com.shasthobondhu.app.databinding.FragmentHospitalsBinding;
import com.shasthobondhu.app.ui.adapters.HospitalAdapter;
import com.shasthobondhu.app.viewmodel.HospitalViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HospitalFragment extends Fragment {

    private FragmentHospitalsBinding binding;
    private HospitalViewModel viewModel;
    private HospitalAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHospitalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HospitalViewModel.class);

        adapter = new HospitalAdapter(new ArrayList<>());
        binding.rvHospitals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHospitals.setAdapter(adapter);

        binding.etHospitalSearch.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setQuery(s == null ? "" : s.toString());
            }
        });

        viewModel.getHospitals().observe(getViewLifecycleOwner(), hospitals -> {
            adapter = new HospitalAdapter(hospitals == null ? new ArrayList<>() : hospitals);
            binding.rvHospitals.setAdapter(adapter);
            boolean empty = hospitals == null || hospitals.isEmpty();
            binding.tvEmptyHospitals.setVisibility(empty ? View.VISIBLE : View.GONE);
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
