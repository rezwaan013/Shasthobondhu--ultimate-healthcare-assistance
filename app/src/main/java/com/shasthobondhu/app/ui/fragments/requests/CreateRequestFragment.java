package com.shasthobondhu.app.ui.fragments.requests;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.shasthobondhu.app.R;
import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.databinding.FragmentCreateRequestBinding;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.constants.AppConstants;
import com.shasthobondhu.app.viewmodel.RequestViewModel;

import java.util.Calendar;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateRequestFragment extends Fragment {

    private FragmentCreateRequestBinding binding;
    private RequestViewModel viewModel;
    private boolean posting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RequestViewModel.class);

        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        binding.spinnerRequestBloodGroup.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, bloodGroups));

        String[] urgencyLabels = {
                getString(R.string.urgency_critical),
                getString(R.string.urgency_urgent),
                getString(R.string.urgency_normal)
        };
        binding.spinnerUrgency.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, urgencyLabels));
        binding.spinnerUrgency.setText(urgencyLabels[1], false);

        binding.etNeededDate.setOnClickListener(v -> showDatePicker());
        binding.etNeededTime.setOnClickListener(v -> showTimePicker());

        binding.btnPostRequest.setOnClickListener(v -> postRequest());
    }

    private void postRequest() {
        if (posting) {
            return;
        }
        String patient = text(binding.etPatientName);
        String patientDetails = text(binding.etPatientDetails);
        String bloodGroup = text(binding.spinnerRequestBloodGroup);
        String unitsText = text(binding.etUnits);
        String hospital = text(binding.etHospitalName);
        String location = text(binding.etRequestLocation);
        String urgency = text(binding.spinnerUrgency);
        String neededDate = text(binding.etNeededDate);
        String neededTime = text(binding.etNeededTime);
        String phone = text(binding.etRequestPhone);

        hideError();
        if (patient.isEmpty() || patientDetails.isEmpty() || hospital.isEmpty()
                || location.isEmpty() || neededDate.isEmpty() || neededTime.isEmpty()) {
            showError(getString(R.string.error_required));
            return;
        }
        if (bloodGroup.isEmpty()) {
            showError(getString(R.string.choose_blood_group));
            return;
        }
        int units;
        try {
            units = Integer.parseInt(unitsText);
        } catch (NumberFormatException e) {
            units = 0;
        }
        if (units < 1) {
            showError(getString(R.string.error_units));
            return;
        }
        if (phone.length() < 11) {
            showError(getString(R.string.error_invalid_phone));
            return;
        }

        String storedUrgency = AppConstants.URGENCY_URGENT;
        if (getString(R.string.urgency_critical).equals(urgency)) {
            storedUrgency = AppConstants.URGENCY_CRITICAL;
        } else if (getString(R.string.urgency_normal).equals(urgency)) {
            storedUrgency = AppConstants.URGENCY_NORMAL;
        }

        String requesterUid = SessionManager.isSignedIn(requireContext())
                ? SessionManager.getUid(requireContext()) : null;
        RequestEntity request = new RequestEntity(patient, bloodGroup, units, hospital,
                location, storedUrgency, phone, "Open", requesterUid);
        request.patientDetails = patientDetails;
        request.neededDate = neededDate;
        request.neededTime = neededTime;

        posting = true;
        binding.btnPostRequest.setEnabled(false);
        viewModel.createRequest(request, () -> {
            if (!isAdded()) {
                return;
            }
            Toast.makeText(requireContext(), R.string.request_posted, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (picker, year, month, day) -> binding.etNeededDate.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                day, month + 1, year)),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000L);
        dialog.show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (picker, hour, minute) -> binding.etNeededTime.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)),
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private String text(MaterialAutoCompleteTextView field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private String text(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void showError(String message) {
        binding.tvRequestError.setVisibility(View.VISIBLE);
        binding.tvRequestError.setText(message);
    }

    private void hideError() {
        binding.tvRequestError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
