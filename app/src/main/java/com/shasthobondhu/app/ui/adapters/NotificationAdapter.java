package com.shasthobondhu.app.ui.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.model.AppNotification;
import com.shasthobondhu.app.databinding.ItemNotificationBinding;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<AppNotification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<AppNotification> values) {
        notifications = values == null ? new ArrayList<>() : new ArrayList<>(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationViewHolder(ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification item = notifications.get(position);
        holder.binding.tvNotificationTitle.setText(item.title);
        holder.binding.tvNotificationMessage.setText(item.message);
        holder.binding.tvNotificationTitle.setTypeface(null,
                item.read ? Typeface.NORMAL : Typeface.BOLD);
        holder.binding.tvNotificationUnread.setVisibility(item.read
                ? android.view.View.GONE : android.view.View.VISIBLE);
        holder.binding.tvNotificationTime.setText(item.createdAt <= 0
                ? holder.itemView.getContext().getString(R.string.not_shared)
                : DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(item.createdAt)));
        holder.binding.getRoot().setOnClickListener(v -> listener.onNotificationClick(item));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;

        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnNotificationClickListener {
        void onNotificationClick(AppNotification notification);
    }
}
