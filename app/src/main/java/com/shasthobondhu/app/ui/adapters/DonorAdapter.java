package com.shasthobondhu.app.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.data.model.DonorPresence;
import com.shasthobondhu.app.data.repository.DonorAvailabilityPolicy;
import com.shasthobondhu.app.databinding.ItemDonorBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private List<DonorEntity> donors = new ArrayList<>();
    private final OnDonorClickListener donorClickListener;
    private Map<String, DonorPresence> presence = Collections.emptyMap();

    public DonorAdapter(List<DonorEntity> donors) {
        this(donors, null);
    }

    public DonorAdapter(List<DonorEntity> donors, OnDonorClickListener donorClickListener) {
        this.donorClickListener = donorClickListener;
        submit(donors);
    }

    public void submit(List<DonorEntity> newDonors) {
        this.donors = newDonors == null ? new ArrayList<>() : new ArrayList<>(newDonors);
        notifyDataSetChanged();
    }

    public void setPresence(Map<String, DonorPresence> newPresence) {
        presence = newPresence == null ? Collections.emptyMap() : newPresence;
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
        binding.tvDonorLocation.setText(binding.getRoot().getContext().getString(
                com.shasthobondhu.app.R.string.donor_location_format,
                donor.district, donor.upazila));
        binding.tvLastDonation.setText(binding.getRoot().getContext().getString(
                com.shasthobondhu.app.R.string.last_donated,
                donor.lastDonationDate == null || "Not shared".equals(donor.lastDonationDate)
                        ? binding.getRoot().getContext().getString(
                        com.shasthobondhu.app.R.string.not_shared)
                        : donor.lastDonationDate));
        DonorPresence donorPresence = donor.ownerUid == null
                ? null : presence.get(donor.ownerUid);
        binding.tvAvailableNow.setVisibility(DonorAvailabilityPolicy.isAvailableNow(
                donor.isAvailable, donorPresence != null && donorPresence.online)
                ? android.view.View.VISIBLE : android.view.View.GONE);

        binding.getRoot().setOnClickListener(v -> {
            if (donorClickListener != null) {
                donorClickListener.onDonorClick(donor);
            }
        });

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

    public interface OnDonorClickListener {
        void onDonorClick(DonorEntity donor);
    }
}
