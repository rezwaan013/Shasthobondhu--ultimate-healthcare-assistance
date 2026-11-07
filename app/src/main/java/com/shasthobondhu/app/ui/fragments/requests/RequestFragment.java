package com.shasthobondhu.app.ui.fragments.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.data.local.entities.RequestEntity;

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

        adapter = createAdapter(new ArrayList<>());
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRequests.setAdapter(adapter);

        binding.btnCreateRequest.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_create_request));

        viewModel.getAllOpenRequests().observe(getViewLifecycleOwner(), requests -> {
            adapter = createAdapter(requests == null ? new ArrayList<>() : requests);
            binding.rvRequests.setAdapter(adapter);
            boolean empty = requests == null || requests.isEmpty();
            binding.tvEmptyRequests.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    private RequestAdapter createAdapter(java.util.List<RequestEntity> requests) {
        return new RequestAdapter(requests, request -> {
            String uid = SessionManager.getUid(requireContext());
            if (uid.equals(request.requesterUid)) {
                Toast.makeText(requireContext(), R.string.cannot_accept_own_request,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.acceptRequest(request.id, uid,
                    SessionManager.getName(requireContext()), accepted -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), accepted
                                            ? R.string.request_accepted
                                            : R.string.request_already_accepted,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
