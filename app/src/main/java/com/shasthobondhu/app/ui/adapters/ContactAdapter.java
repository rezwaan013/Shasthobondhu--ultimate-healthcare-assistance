package com.shasthobondhu.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shasthobondhu.app.data.model.EmergencyContact;
import com.shasthobondhu.app.databinding.ItemContactBinding;

import java.util.Collections;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final List<EmergencyContact> contacts;
    private final OnContactClickListener listener;

    public ContactAdapter(List<EmergencyContact> contacts, OnContactClickListener listener) {
        this.contacts = contacts == null ? Collections.emptyList() : contacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding binding = ItemContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        holder.binding.tvContactName.setText(contact.name.isEmpty()
                ? holder.itemView.getContext().getString(
                com.shasthobondhu.app.R.string.unknown_contact) : contact.name);
        holder.binding.tvContactPhone.setText(contact.phone);
        holder.binding.getRoot().setOnClickListener(v -> listener.onContactClick(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        final ItemContactBinding binding;

        ContactViewHolder(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnContactClickListener {
        void onContactClick(EmergencyContact contact);
    }
}
