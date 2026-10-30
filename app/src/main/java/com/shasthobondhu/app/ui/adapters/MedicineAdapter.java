package com.shasthobondhu.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.databinding.ItemMedicineBinding;
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
        holder.binding.tvPharmacyName.setText(medicine.pharmacyName);
        holder.binding.tvStockStatus.setText(medicine.stockStatus);
        
        int color = medicine.stockStatus.equals("In Stock") ? 0xFF4CAF50 : 
                   (medicine.stockStatus.equals("Low") ? 0xFFFFC107 : 0xFFF44336);
        holder.binding.tvStockStatus.setTextColor(color);
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
