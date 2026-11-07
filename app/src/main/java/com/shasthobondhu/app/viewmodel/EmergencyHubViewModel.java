package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.local.entities.RequestEntity;
import com.shasthobondhu.app.data.model.DonorPresence;
import com.shasthobondhu.app.data.repository.PresenceRepository;
import com.shasthobondhu.app.data.repository.RequestRepository;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Holds emergency context and the live open-request count across configuration changes. */
@HiltViewModel
public class EmergencyHubViewModel extends ViewModel {

    private static final String KEY_USER_ID = "hub_user_id";
    private static final String KEY_USER_NAME = "hub_user_name";
    private static final String KEY_BLOOD_GROUP = "hub_blood_group";
    private static final String KEY_DISTRICT = "hub_district";

    private final SavedStateHandle state;
    private final LiveData<List<RequestEntity>> openRequests;
    private final LiveData<Integer> openRequestCount;
    private final PresenceRepository presenceRepository;

    @Inject
    public EmergencyHubViewModel(RequestRepository requestRepository,
                                 PresenceRepository presenceRepository,
                                 SavedStateHandle state) {
        this.state = state;
        this.presenceRepository = presenceRepository;
        openRequests = requestRepository.getAllOpenRequests();
        openRequestCount = Transformations.map(openRequests,
                requests -> requests == null ? 0 : requests.size());
    }

    public void initialize(String userId, String userName, String bloodGroup, String district) {
        if (state.contains(KEY_USER_ID)) {
            return;
        }
        state.set(KEY_USER_ID, safe(userId));
        state.set(KEY_USER_NAME, safe(userName));
        state.set(KEY_BLOOD_GROUP, safe(bloodGroup));
        state.set(KEY_DISTRICT, safe(district));
    }

    public String getUserId() {
        return value(KEY_USER_ID);
    }

    public String getUserName() {
        return value(KEY_USER_NAME);
    }

    public String getBloodGroup() {
        return value(KEY_BLOOD_GROUP);
    }

    public String getDistrict() {
        return value(KEY_DISTRICT);
    }

    public LiveData<List<RequestEntity>> getOpenRequests() {
        return openRequests;
    }

    public LiveData<Integer> getOpenRequestCount() {
        return openRequestCount;
    }

    public boolean isPresenceConfigured() {
        return presenceRepository.isConfigured();
    }

    public LiveData<Map<String, DonorPresence>> getPresence() {
        return presenceRepository.observePresence();
    }

    public void setAvailableNow(boolean available, PresenceRepository.Callback callback) {
        presenceRepository.setAvailability(getUserId(), available, callback);
    }

    private String value(String key) {
        String value = state.get(key);
        return safe(value);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
