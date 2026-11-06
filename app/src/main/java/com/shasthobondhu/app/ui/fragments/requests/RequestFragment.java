package com.shasthobondhu.app.ui.fragments.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shasthobondhu.app.R;
import com.shasthobondhu.app.databinding.FragmentRequestsBinding;
import com.shasthobondhu.app.ui.adapters.RequestAdapter;
import com.shasthobondhu.app.viewmodel.RequestViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestFragment extends Fragment {

    private FragmentRequestsBinding binding;
    private RequestViewModel viewModel;
    private RequestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RequestViewModel.class);

        adapter = new RequestAdapter(new ArrayList<>());
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRequests.setAdapter(adapter);

        binding.btnCreateRequest.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_create_request));

        viewModel.getAllOpenRequests().observe(getViewLifecycleOwner(), requests -> {
            adapter = new RequestAdapter(requests == null ? new ArrayList<>() : requests);
            binding.rvRequests.setAdapter(adapter);
            boolean empty = requests == null || requests.isEmpty();
            binding.tvEmptyRequests.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
