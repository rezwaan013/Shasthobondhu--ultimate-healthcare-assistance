package com.shasthobondhu.app.ui.fragments.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shasthobondhu.app.databinding.FragmentNotificationsBinding;
import com.shasthobondhu.app.ui.adapters.NotificationAdapter;
import com.shasthobondhu.app.viewmodel.NotificationViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationFragment extends Fragment {
    private FragmentNotificationsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NotificationViewModel viewModel = new ViewModelProvider(this)
                .get(NotificationViewModel.class);
        NotificationAdapter adapter = new NotificationAdapter(viewModel::markRead);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(adapter);
        viewModel.getNotifications().observe(getViewLifecycleOwner(), values -> {
            adapter.submit(values);
            binding.tvNotificationsEmpty.setVisibility(values == null || values.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
