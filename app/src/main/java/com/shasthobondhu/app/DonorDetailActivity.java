package com.shasthobondhu.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import com.shasthobondhu.app.data.local.entities.DonorEntity;
import com.shasthobondhu.app.databinding.ActivityDonorDetailBinding;
import com.shasthobondhu.app.util.MapNavigationUtils;

/** Full donor context with safe call/navigation actions and a result for the caller. */
public class DonorDetailActivity extends AppCompatActivity {

    public static final String RESULT_MARKED_CONTACTED = "result_marked_contacted";
    private static final String EXTRA_NAME = "donor_name";
    private static final String EXTRA_PHONE = "donor_phone";
    private static final String EXTRA_BLOOD_GROUP = "donor_blood_group";
    private static final String EXTRA_LOCATION = "donor_location";
    private static final String EXTRA_LAST_DONATION = "donor_last_donation";
    private static final String STATE_CONTACTED = "state_contacted";

    private ActivityDonorDetailBinding binding;
    private String phone;
    private String location;
    private boolean markedContacted;

    public static Intent createIntent(Context context, DonorEntity donor) {
        Intent intent = new Intent(context, DonorDetailActivity.class);
        if (donor == null) {
            return intent;
        }
        intent.putExtra(EXTRA_NAME, donor.name);
        intent.putExtra(EXTRA_PHONE, donor.phone);
        intent.putExtra(EXTRA_BLOOD_GROUP, donor.bloodGroup);
        String district = donor.district == null ? "" : donor.district;
        String area = donor.upazila == null ? "" : donor.upazila;
        intent.putExtra(EXTRA_LOCATION, district + (area.isEmpty() ? "" : ", " + area));
        intent.putExtra(EXTRA_LAST_DONATION, donor.lastDonationDate);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonorDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        markedContacted = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_CONTACTED, false);
        Intent intent = getIntent();
        String name = safe(intent == null ? null : intent.getStringExtra(EXTRA_NAME));
        phone = safe(intent == null ? null : intent.getStringExtra(EXTRA_PHONE));
        String bloodGroup = safe(intent == null ? null : intent.getStringExtra(EXTRA_BLOOD_GROUP));
        location = safe(intent == null ? null : intent.getStringExtra(EXTRA_LOCATION));
        String lastDonation = safe(intent == null ? null
                : intent.getStringExtra(EXTRA_LAST_DONATION));

        binding.tvDonorDetailName.setText(name.isEmpty()
                ? getString(R.string.donor_unknown_name) : name);
        binding.tvDonorDetailBlood.setText(bloodGroup.isEmpty() ? "—" : bloodGroup);
        binding.tvDonorDetailLocation.setText(location.isEmpty()
                ? getString(R.string.location_not_shared) : location);
        binding.tvDonorDetailLastDonation.setText(getString(R.string.last_donated,
                lastDonation.isEmpty() || "Not shared".equals(lastDonation)
                        ? getString(R.string.not_shared) : lastDonation));
        binding.tvDonorDetailPhone.setText(phone.isEmpty()
                ? getString(R.string.phone_not_shared) : phone);

        binding.btnDonorDetailBack.setOnClickListener(v -> finishWithResult());
        binding.btnDonorDetailCall.setEnabled(!phone.isEmpty());
        binding.btnDonorDetailCall.setOnClickListener(v -> dial());
        binding.btnDonorDetailMap.setEnabled(!location.isEmpty());
        binding.btnDonorDetailMap.setOnClickListener(v ->
                MapNavigationUtils.navigate(this, location));
        binding.btnMarkContacted.setOnClickListener(v -> {
            markedContacted = true;
            finishWithResult();
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithResult();
            }
        });
    }

    private void dial() {
        Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        if (dial.resolveActivity(getPackageManager()) != null) {
            startActivity(dial);
        }
    }

    private void finishWithResult() {
        Intent result = new Intent().putExtra(RESULT_MARKED_CONTACTED, markedContacted);
        setResult(markedContacted ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishWithResult();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_CONTACTED, markedContacted);
        super.onSaveInstanceState(outState);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
