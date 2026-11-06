package com.shasthobondhu.app.ui.fragments.pharmacy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.data.local.entities.PharmacyEntity;
import com.shasthobondhu.app.databinding.FragmentPharmacyDashboardBinding;
import com.shasthobondhu.app.databinding.ItemOwnerMedicineBinding;
import com.shasthobondhu.app.ui.fragments.home.HomeFragment;
import com.shasthobondhu.app.util.DisplayUtils;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.constants.AppConstants;
import com.shasthobondhu.app.viewmodel.AuthViewModel;
import com.shasthobondhu.app.viewmodel.OwnerViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PharmacyDashboardFragment extends Fragment {

    private FragmentPharmacyDashboardBinding binding;
    private OwnerViewModel viewModel;
    private AuthViewModel authViewModel;
    private int loadedPharmacyId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPharmacyDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!SessionManager.isPharmacyOwner(requireContext())) {
            redirectToCorrectArea();
            return;
        }

        viewModel = new ViewModelProvider(this).get(OwnerViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupStockSpinner();
        binding.btnCreatePharmacy.setOnClickListener(v -> createPharmacy());
        binding.btnAddMedicine.setOnClickListener(v -> addMedicine());
        binding.btnPharmacySignOut.setOnClickListener(v -> signOut());

        viewModel.getMedicines().observe(getViewLifecycleOwner(), this::renderMedicines);
        viewModel.loadPharmacy(SessionManager.getUid(requireContext()));
        viewModel.getPharmacy().observe(getViewLifecycleOwner(), pharmacy -> {
            if (pharmacy == null) {
                binding.groupCreatePharmacy.setVisibility(View.VISIBLE);
                binding.groupManagePharmacy.setVisibility(View.GONE);
                return;
            }
            binding.groupCreatePharmacy.setVisibility(View.GONE);
            binding.groupManagePharmacy.setVisibility(View.VISIBLE);
            binding.tvPharmacyName.setText(pharmacy.name);
            binding.tvPharmacyDetails.setText(getString(R.string.pharmacy_details,
                    pharmacy.area, pharmacy.phone));

            if (pharmacy.id != loadedPharmacyId) {
                loadedPharmacyId = pharmacy.id;
                viewModel.loadMedicines(pharmacy.id);
            }
        });
    }

    private void setupStockSpinner() {
        String[] labels = {
                getString(R.string.stock_in),
                getString(R.string.stock_low),
                getString(R.string.stock_out)
        };
        binding.spinnerStockStatus.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, labels));
        binding.spinnerStockStatus.setText(labels[0], false);
    }

    private void createPharmacy() {
        String name = text(binding.etPharmacyName);
        String area = text(binding.etPharmacyArea);
        String address = text(binding.etPharmacyAddress);
        String phone = text(binding.etPharmacyPhone);
        String hours = text(binding.etPharmacyHours);
        hideError();

        if (name.isEmpty() || area.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            showError(getString(R.string.error_required));
            return;
        }
        if (phone.length() < 6) {
            showError(getString(R.string.error_invalid_phone));
            return;
        }

        PharmacyEntity pharmacy = new PharmacyEntity(name, address, area, phone,
                hours.isEmpty() ? "Not shared" : hours,
                SessionManager.getUid(requireContext()), true);
        binding.btnCreatePharmacy.setEnabled(false);
        viewModel.createPharmacy(pharmacy, () -> {
            if (!isAdded()) {
                return;
            }
            binding.btnCreatePharmacy.setEnabled(true);
            Toast.makeText(requireContext(), R.string.pharmacy_created, Toast.LENGTH_SHORT).show();
        });
    }

    private void addMedicine() {
        PharmacyEntity pharmacy = viewModel.getPharmacy().getValue();
        if (pharmacy == null) {
            showError(getString(R.string.error_required));
            return;
        }
        String name = text(binding.etMedicineName);
        if (name.isEmpty()) {
            showError(getString(R.string.error_required));
            return;
        }
        hideError();

        String stock = stockFromLabel(text(binding.spinnerStockStatus));
        MedicineEntity medicine = new MedicineEntity(name, pharmacy.name, stock, today(),
                pharmacy.phone, pharmacy.id, pharmacy.area, pharmacy.address);
        binding.btnAddMedicine.setEnabled(false);
        viewModel.addMedicine(medicine, () -> {
            if (!isAdded()) {
                return;
            }
            binding.btnAddMedicine.setEnabled(true);
            binding.etMedicineName.setText("");
            Toast.makeText(requireContext(), R.string.medicine_added, Toast.LENGTH_SHORT).show();
        });
    }

    private void renderMedicines(List<MedicineEntity> medicines) {
        if (binding == null) {
            return;
        }
        binding.containerMedicines.removeAllViews();
        boolean empty = medicines == null || medicines.isEmpty();
        binding.tvNoMedicines.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            return;
        }
        for (MedicineEntity medicine : medicines) {
            ItemOwnerMedicineBinding item = ItemOwnerMedicineBinding.inflate(
                    getLayoutInflater(), binding.containerMedicines, false);
            item.tvOwnerMedicineName.setText(medicine.name);
            item.tvOwnerStock.setText(getString(R.string.stock_label,
                    DisplayUtils.stockLabel(requireContext(), medicine.stockStatus)));
            item.tvOwnerStock.setTextColor(ContextCompat.getColor(requireContext(),
                    AppConstants.STOCK_IN.equals(medicine.stockStatus) ? R.color.success
                            : (AppConstants.STOCK_LOW.equals(medicine.stockStatus)
                            ? R.color.warning : R.color.emergency)));

            item.btnCycleStock.setText(R.string.change_stock);
            item.btnCycleStock.setOnClickListener(v -> {
                medicine.stockStatus = DisplayUtils.nextStock(medicine.stockStatus);
                medicine.lastUpdated = today();
                viewModel.updateMedicine(medicine);
                Toast.makeText(requireContext(), R.string.stock_updated, Toast.LENGTH_SHORT).show();
            });
            item.btnRemoveMedicine.setOnClickListener(v -> {
                viewModel.deleteMedicine(medicine);
                Toast.makeText(requireContext(), R.string.medicine_removed, Toast.LENGTH_SHORT).show();
            });
            binding.containerMedicines.addView(item.getRoot());
        }
    }

    private void signOut() {
        authViewModel.logout();
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_home, true)
                .setLaunchSingleTop(true)
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.nav_home, null, options);
    }

    private void redirectToCorrectArea() {
        if (SessionManager.isHospitalOwner(requireContext())) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_hospital_dashboard);
            return;
        }
        Bundle args = new Bundle();
        args.putString(HomeFragment.ARG_OWNER_ROLE, AppConstants.ROLE_PHARMACY);
        NavHostFragment.findNavController(this).navigate(R.id.nav_login, args);
    }

    private String stockFromLabel(String label) {
        if (getString(R.string.stock_low).equals(label)) {
            return AppConstants.STOCK_LOW;
        }
        if (getString(R.string.stock_out).equals(label)) {
            return AppConstants.STOCK_OUT;
        }
        return AppConstants.STOCK_IN;
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private String text(MaterialAutoCompleteTextView field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String text(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void showError(String message) {
        binding.tvPharmacyError.setVisibility(View.VISIBLE);
        binding.tvPharmacyError.setText(message);
    }

    private void hideError() {
        binding.tvPharmacyError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
