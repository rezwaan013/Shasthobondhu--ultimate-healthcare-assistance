package com.shasthobondhu.app.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.databinding.ItemHospitalBinding;
import com.shasthobondhu.app.util.MapNavigationUtils;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {
    private List<HospitalEntity> hospitals;

    public HospitalAdapter(List<HospitalEntity> hospitals) {
        this.hospitals = hospitals;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHospitalBinding binding = ItemHospitalBinding.inflate(inflater, parent, false);
        return new HospitalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        HospitalEntity hospital = hospitals.get(position);
        holder.binding.tvHospitalName.setText(hospital.name);
        holder.binding.tvHospitalAddress.setText(hospital.address);
        holder.binding.tvHospitalServices.setText(hospital.services);
        boolean hasDoctors = hospital.doctors != null && !hospital.doctors.trim().isEmpty();
        holder.binding.tvHospitalDoctors.setVisibility(hasDoctors ? View.VISIBLE : View.GONE);
        if (hasDoctors) {
            holder.binding.tvHospitalDoctors.setText(holder.itemView.getContext().getString(
                    com.shasthobondhu.app.R.string.doctors_public_label,
                    hospital.doctors.replace("||", "\n").replace("~~", " — ")));
        }
        holder.binding.tvHospitalPhone.setText(hospital.phone);
        holder.binding.btnCallHospital.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + hospital.phone));
            try {
                v.getContext().startActivity(intent);
            } catch (Exception ignored) {
                // No dialer available.
            }
        });
        holder.binding.btnNavigateHospital.setOnClickListener(v ->
                MapNavigationUtils.navigate(v.getContext(),
                        hospital.name + ", " + hospital.address));
    }

    @Override
    public int getItemCount() {
        return hospitals == null ? 0 : hospitals.size();
    }

    public static class HospitalViewHolder extends RecyclerView.ViewHolder {
        public final ItemHospitalBinding binding;

        public HospitalViewHolder(ItemHospitalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
