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
    private final OnStatusListener statusListener;
    private final String currentUid;

    public RequestAdapter(List<RequestEntity> requests) {
        this(requests, "", null, null);
    }

    public RequestAdapter(List<RequestEntity> requests, OnAcceptListener acceptListener) {
        this(requests, "", acceptListener, null);
    }

    public RequestAdapter(List<RequestEntity> requests, String currentUid,
                          OnAcceptListener acceptListener, OnStatusListener statusListener) {
        this.requests = requests;
        this.currentUid = currentUid == null ? "" : currentUid;
        this.acceptListener = acceptListener;
        this.statusListener = statusListener;
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
        holder.binding.tvPatientDetails.setText(request.patientDetails == null
                ? holder.itemView.getContext().getString(R.string.no_patient_details)
                : request.patientDetails);
        holder.binding.tvNeededAt.setText(holder.itemView.getContext().getString(
                R.string.needed_at,
                request.neededDate == null ? "--" : request.neededDate,
                request.neededTime == null ? "--" : request.neededTime));
        holder.binding.tvUrgency.setText(DisplayUtils.urgencyLabel(
                holder.itemView.getContext(), request.urgency));
        holder.binding.tvStatus.setText(request.status);
        boolean accepted = "Accepted".equals(request.status);
        boolean owner = currentUid.equals(request.requesterUid);
        holder.binding.tvAcceptedBy.setVisibility(accepted ? View.VISIBLE : View.GONE);
        if (accepted) {
            holder.binding.tvAcceptedBy.setText(holder.itemView.getContext().getString(
                    R.string.accepted_by, request.acceptedByName == null
                            ? holder.itemView.getContext().getString(R.string.general_donor)
                            : request.acceptedByName));
        }
        holder.binding.btnAcceptRequest.setVisibility(
                !accepted && !owner && acceptListener != null ? View.VISIBLE : View.GONE);
        holder.binding.btnAcceptRequest.setOnClickListener(v -> {
            if (acceptListener != null) {
                acceptListener.onAccept(request);
            }
        });
        holder.binding.btnFulfillRequest.setVisibility(accepted && owner && statusListener != null
                ? View.VISIBLE : View.GONE);
        holder.binding.btnCancelRequest.setVisibility(("Open".equals(request.status) || accepted)
                && owner && statusListener != null ? View.VISIBLE : View.GONE);
        holder.binding.btnFulfillRequest.setOnClickListener(v ->
                statusListener.onStatus(request, "Fulfilled"));
        holder.binding.btnCancelRequest.setOnClickListener(v ->
                statusListener.onStatus(request, "Cancelled"));
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

    public interface OnStatusListener {
        void onStatus(RequestEntity request, String status);
    }
}
