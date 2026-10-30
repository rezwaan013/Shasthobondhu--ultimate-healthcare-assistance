package com.shasthobondhu.app.ui.fragments.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.shasthobondhu.app.databinding.FragmentRequestsBinding;
import com.shasthobondhu.app.ui.adapters.RequestAdapter;
import com.shasthobondhu.app.viewmodel.RequestViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestFragment extends Fragment {
    private FragmentRequestsBinding binding;
    private RequestViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRequestsBinding.inflate(inflater, container, false);
        
        viewModel = new ViewModelProvider(this).get(RequestViewModel.class);
        
        RequestAdapter adapter = new RequestAdapter(new java.util.ArrayList<>());
        binding.rvRequests.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvRequests.setAdapter(adapter);

        viewModel.getRequests("Dhaka").observe(getViewLifecycleOwner(), requests -> {
            if (requests != null) {
                binding.rvRequests.setAdapter(new RequestAdapter(requests));
            }
        });

        return binding.getRoot();
    }
}
