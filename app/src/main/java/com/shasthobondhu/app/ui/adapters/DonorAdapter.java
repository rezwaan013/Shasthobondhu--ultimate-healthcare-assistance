package com.shasthobondhu.app.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.databinding.ItemDonorBinding;

import java.util.ArrayList;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private List<DonorEntity> donors = new ArrayList<>();

    public DonorAdapter(List<DonorEntity> donors) {
        submit(donors);
    }

    public void submit(List<DonorEntity> newDonors) {
        this.donors = newDonors == null ? new ArrayList<>() : new ArrayList<>(newDonors);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDonorBinding binding = ItemDonorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DonorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        DonorEntity donor = donors.get(position);
        ItemDonorBinding binding = holder.binding;

        binding.tvDonorName.setText(donor.name);
        binding.tvBloodGroup.setText(donor.bloodGroup);
        binding.tvDonorLocation.setText(donor.district + ", " + donor.upazila);

        binding.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + donor.phone));
            try {
                v.getContext().startActivity(intent);
            } catch (Exception ignored) {
                // No dialer available – nothing else we can do here.
            }
        });
    }

    @Override
    public int getItemCount() {
        return donors.size();
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        public final ItemDonorBinding binding;

        public DonorViewHolder(ItemDonorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
