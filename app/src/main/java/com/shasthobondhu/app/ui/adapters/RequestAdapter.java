package com.shasthobondhu.app.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.databinding.ItemRequestBinding;
import com.shasthobondhu.app.util.DisplayUtils;
import com.shasthobondhu.app.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<RequestEntity> requests;
    private final OnAcceptListener acceptListener;

    public RequestAdapter(List<RequestEntity> requests) {
        this(requests, null);
    }

    public RequestAdapter(List<RequestEntity> requests, OnAcceptListener acceptListener) {
        this.requests = requests;
        this.acceptListener = acceptListener;
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
        holder.binding.tvHospital.setText(holder.itemView.getContext().getString(
                com.shasthobondhu.app.R.string.request_hospital_location,
                request.hospitalName, request.location));
        holder.binding.tvUrgency.setText(DisplayUtils.urgencyLabel(
                holder.itemView.getContext(), request.urgency));
        holder.binding.tvStatus.setText(request.status);
        boolean accepted = "Accepted".equals(request.status);
        holder.binding.tvAcceptedBy.setVisibility(accepted ? View.VISIBLE : View.GONE);
        if (accepted) {
            holder.binding.tvAcceptedBy.setText(holder.itemView.getContext().getString(
                    R.string.accepted_by, request.acceptedByName == null
                            ? holder.itemView.getContext().getString(R.string.general_donor)
                            : request.acceptedByName));
        }
        holder.binding.btnAcceptRequest.setVisibility(
                !accepted && acceptListener != null ? View.VISIBLE : View.GONE);
        holder.binding.btnAcceptRequest.setOnClickListener(v -> {
            if (acceptListener != null) {
                acceptListener.onAccept(request);
            }
        });
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

    public interface OnAcceptListener {
        void onAccept(RequestEntity request);
    }
}
