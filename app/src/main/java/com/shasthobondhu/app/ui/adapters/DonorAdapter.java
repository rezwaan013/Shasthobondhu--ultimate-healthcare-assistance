package com.shasthobondhu.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.databinding.ItemDonorBinding;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {
    private List<DonorEntity> donors;

    public DonorAdapter(List<DonorEntity> donors) {
        this.donors = donors;
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDonorBinding binding = ItemDonorBinding.inflate(inflater, parent, false);
        return new DonorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        DonorEntity donor = donors.get(position);
        holder.binding.tvDonorName.setText(donor.name);
        holder.binding.tvDonorBloodGroup.setText(donor.bloodGroup);
        holder.binding.tvDonorLocation.setText(donor.district + ", " + donor.upazila);
    }

    @Override
    public int getItemCount() {
        return donors == null ? 0 : donors.size();
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        public final ItemDonorBinding binding;
        public DonorViewHolder(ItemDonorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
