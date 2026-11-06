package com.shasthobondhu.app.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.databinding.ItemRequestBinding;
import com.shasthobondhu.app.util.DisplayUtils;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<RequestEntity> requests;

    public RequestAdapter(List<RequestEntity> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemRequestBinding binding = ItemRequestBinding.inflate(inflater, parent, false);
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestEntity request = requests.get(position);
        holder.binding.tvPatientName.setText(request.patientName);
        holder.binding.tvBloodGroup.setText(request.bloodGroup);
        holder.binding.tvHospital.setText(request.hospitalName + " · " + request.location);
        holder.binding.tvUrgency.setText(DisplayUtils.urgencyLabel(
                holder.itemView.getContext(), request.urgency));
        holder.binding.tvStatus.setText(request.status);
        holder.binding.btnCallRequester.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + request.contactPhone));
            try {
                v.getContext().startActivity(intent);
            } catch (Exception ignored) {
                // No dialer available.
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests == null ? 0 : requests.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        public final ItemRequestBinding binding;

        public RequestViewHolder(ItemRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
