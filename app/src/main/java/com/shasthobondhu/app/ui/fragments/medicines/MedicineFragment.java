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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.data.model.DrugLabel;
import com.shasthobondhu.app.data.model.Resource;
import com.shasthobondhu.app.databinding.BottomSheetMedicineInfoBinding;
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
    private BottomSheetDialog medicineInfoDialog;
    private BottomSheetMedicineInfoBinding medicineInfoBinding;

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

        adapter = new MedicineAdapter(new ArrayList<>(), this::openMedicineInfo);
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
            adapter = new MedicineAdapter(medicines == null ? new ArrayList<>() : medicines,
                    this::openMedicineInfo);
            binding.rvMedicines.setAdapter(adapter);
            boolean empty = medicines == null || medicines.isEmpty();
            binding.tvEmptyMedicines.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getDrugInfo().observe(getViewLifecycleOwner(), this::renderDrugInfo);
    }

    private void openMedicineInfo(MedicineEntity medicine) {
        medicineInfoBinding = BottomSheetMedicineInfoBinding.inflate(getLayoutInflater());
        medicineInfoBinding.tvMedicineInfoTitle.setText(medicine.name);
        medicineInfoBinding.btnRetryMedicineInfo.setOnClickListener(v ->
                viewModel.loadDrugInfo(viewModel.getSelectedMedicineName()));
        medicineInfoDialog = new BottomSheetDialog(requireContext());
        medicineInfoDialog.setContentView(medicineInfoBinding.getRoot());
        medicineInfoDialog.setOnDismissListener(dialog -> {
            medicineInfoBinding = null;
            medicineInfoDialog = null;
        });
        medicineInfoDialog.show();
        viewModel.loadDrugInfo(medicine.name);
    }

    private void renderDrugInfo(Resource<DrugLabel> resource) {
        if (medicineInfoBinding == null || resource == null) {
            return;
        }
        boolean loading = resource.status == Resource.Status.LOADING;
        medicineInfoBinding.progressMedicineInfo.setVisibility(loading ? View.VISIBLE : View.GONE);
        medicineInfoBinding.groupMedicineInfo.setVisibility(View.GONE);
        medicineInfoBinding.tvMedicineInfoMessage.setVisibility(View.GONE);
        medicineInfoBinding.btnRetryMedicineInfo.setVisibility(View.GONE);

        if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
            medicineInfoBinding.tvMedicineInfoTitle.setText(resource.data.displayName);
            medicineInfoBinding.tvMedicineIndications.setText(resource.data.indications.isEmpty()
                    ? getString(R.string.medicine_info_not_provided)
                    : resource.data.indications);
            medicineInfoBinding.tvMedicineWarnings.setText(resource.data.warnings.isEmpty()
                    ? getString(R.string.medicine_info_not_provided)
                    : resource.data.warnings);
            medicineInfoBinding.groupMedicineInfo.setVisibility(View.VISIBLE);
        } else if (resource.status == Resource.Status.EMPTY) {
            medicineInfoBinding.tvMedicineInfoMessage.setText(R.string.medicine_info_empty);
            medicineInfoBinding.tvMedicineInfoMessage.setVisibility(View.VISIBLE);
        } else if (resource.status == Resource.Status.ERROR) {
            medicineInfoBinding.tvMedicineInfoMessage.setText(errorMessage(resource.errorType));
            medicineInfoBinding.tvMedicineInfoMessage.setVisibility(View.VISIBLE);
            medicineInfoBinding.btnRetryMedicineInfo.setVisibility(View.VISIBLE);
        }
    }

    private int errorMessage(Resource.ErrorType type) {
        if (type == Resource.ErrorType.NETWORK) {
            return R.string.api_network_error;
        }
        if (type == Resource.ErrorType.MALFORMED) {
            return R.string.api_data_error;
        }
        return R.string.api_server_error;
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
        if (medicineInfoDialog != null) {
            medicineInfoDialog.dismiss();
        }
        medicineInfoBinding = null;
        medicineInfoDialog = null;
        super.onDestroyView();
        binding = null;
    }
}
