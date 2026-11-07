package com.shasthobondhu.app.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.databinding.ItemMedicineBinding;
import com.shasthobondhu.app.util.DisplayUtils;
import com.shasthobondhu.app.util.MapNavigationUtils;
import com.shasthobondhu.app.util.constants.AppConstants;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<MedicineEntity> medicines;

    public MedicineAdapter(List<MedicineEntity> medicines) {
        this.medicines = medicines;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMedicineBinding binding = ItemMedicineBinding.inflate(inflater, parent, false);
        return new MedicineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineEntity medicine = medicines.get(position);
        holder.binding.tvMedicineName.setText(medicine.name);

        String pharmacyLine = medicine.pharmacyName;
        if (medicine.pharmacyArea != null && !medicine.pharmacyArea.trim().isEmpty()) {
            pharmacyLine = pharmacyLine + " · " + medicine.pharmacyArea;
        }
        holder.binding.tvPharmacyName.setText(pharmacyLine);

        holder.binding.tvStockStatus.setText(DisplayUtils.stockLabel(
                holder.itemView.getContext(), medicine.stockStatus));

        int color = ContextCompat.getColor(holder.itemView.getContext(),
                AppConstants.STOCK_IN.equals(medicine.stockStatus) ? R.color.success
                        : (AppConstants.STOCK_LOW.equals(medicine.stockStatus)
                        ? R.color.warning : R.color.emergency));
        holder.binding.tvStockStatus.setTextColor(color);

        holder.binding.btnContactPharmacy.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + medicine.pharmacyPhone));
            try {
                v.getContext().startActivity(intent);
            } catch (Exception ignored) {
                // No dialer available.
            }
        });
        holder.binding.btnNavigatePharmacy.setOnClickListener(v -> {
            String destination = medicine.pharmacyAddress == null
                    || medicine.pharmacyAddress.trim().isEmpty()
                    ? medicine.pharmacyName + ", " + medicine.pharmacyArea
                    : medicine.pharmacyName + ", " + medicine.pharmacyAddress;
            MapNavigationUtils.navigate(v.getContext(), destination);
        });
    }

    @Override
    public int getItemCount() {
        return medicines == null ? 0 : medicines.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        public final ItemMedicineBinding binding;

        public MedicineViewHolder(ItemMedicineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
